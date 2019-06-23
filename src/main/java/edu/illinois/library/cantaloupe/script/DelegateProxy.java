package edu.illinois.library.cantaloupe.script;

import edu.illinois.library.cantaloupe.config.Configuration;
import edu.illinois.library.cantaloupe.config.Key;
import edu.illinois.library.cantaloupe.resource.RequestContext;
import edu.illinois.library.cantaloupe.script.engines.JRubyDelegateProxy;
import edu.illinois.library.cantaloupe.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

public abstract class DelegateProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegateProxy.class);

    /**
     * JSR-223 interface to the script interpreter. Invoke methods by casting
     * this to {@link Invocable}.
     */
    protected static ScriptEngine scriptEngine;

    /**
     * Read/write lock used to maintain thread-safety.
     */
    protected static final StampedLock lock = new StampedLock();

    /**
     * Caches delegate method invocations (arguments + return values).
     */
    public static final InvocationCache invocationCache = new HeapInvocationCache();

    private RequestContext requestContext;

    /**
     * The scripting language's delegate object.
      */
    private Object delegate;

    static {
        String engineName = Configuration.getInstance()
                .getString(Key.DELEGATE_SCRIPT_ENGINE, "jruby");
        if (engineName.equals("jruby")) {
            JRubyDelegateProxy.setEngineParameters();
        }
        scriptEngine = new ScriptEngineManager().getEngineByName(engineName);
    }

    /**
     * Loads the given code into the script engine.
     */
    public static void load(String code) throws ScriptException {
        LOGGER.info("Loading script code");

        final long stamp = lock.writeLock();
        try {
            scriptEngine.eval(code);
        } finally {
            lock.unlock(stamp);
        }
    }

    private static boolean isInvocationCacheEnabled() {
        return Configuration.getInstance().
                getBoolean(Key.DELEGATE_METHOD_INVOCATION_CACHE_ENABLED, false);
    }

    public static InvocationCache getInvocationCache() {
        return invocationCache;
    }

    public DelegateProxy(RequestContext requestContext) {
        this.delegate = createDelegate();
        try {
            this.setRequestContext(requestContext);
        } catch (ScriptException e) {
            LOGGER.error(e.getMessage());
        }
    }

    protected abstract Object createDelegate();

    public abstract String getMethodName(DelegateMethod method);

    /**
     * N.B.: The returned object should not be modified, as this could disrupt
     * the invocation cache.
     *
     * @param method Method to invoke.
     * @param args   Arguments to pass to the method.
     * @return       Return value of the method.
     */
    protected Object invoke(DelegateMethod method,
                            Object... args) throws ScriptException {
        return isInvocationCacheEnabled() ?
                retrieveFromCacheOrInvoke(method, args) :
                invokeUncached(method, args);
    }

    private Object getCacheKey(DelegateMethod method, Object... args) {
        // The cache key is comprised of the method name at position 0, the
        // request context at position 1, and the arguments at succeeding
        // positions.
        List<Object> key = List.of(args);
        key = new LinkedList<>(key);
        key.add(0, getMethodName(method));
        key.add(1, requestContext);
        return key;
    }


    private Object retrieveFromCacheOrInvoke(DelegateMethod method, Object... args)
            throws ScriptException {
        final Object cacheKey = getCacheKey(method, args);
        Object returnValue = invocationCache.get(cacheKey);

        if (returnValue != null) {
            LOGGER.trace("invoke({}): cache hit (skipping invocation)", method);
        } else {
            LOGGER.trace("invoke({}): cache miss", method);
            returnValue = invokeUncached(method, args);
            if (returnValue != null) {
                invocationCache.put(cacheKey, returnValue);
            }
        }
        return returnValue;
    }

    protected Object invokeUncached(DelegateMethod method,
                                    Object... args) throws ScriptException {
        final long stamp = lock.readLock();

        String methodName = getMethodName(method);
        final String argsList = (args.length > 0) ?
                Arrays.stream(args)
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")) : "none";
        LOGGER.trace("invokeUncached(): invoking {}() with args: ({})",
                methodName, argsList);

        final Stopwatch watch = new Stopwatch();
        try {
            final Object retval = ((Invocable) scriptEngine).invokeMethod(
                    delegate, methodName, args);

            if (DelegateMethod.REQUEST_CONTEXT_SETTER != method) {
                LOGGER.trace("invokeUncached(): {}() returned {} for args: ({}) in {}",
                        methodName, retval, argsList, watch);
            }
            return retval;
        } catch (NoSuchMethodException e) {
            throw new ScriptException(e);
        } finally {
            lock.unlock(stamp);
        }
    }

    protected void setRequestContext(RequestContext context)
            throws ScriptException {
        invokeUncached(DelegateMethod.REQUEST_CONTEXT_SETTER,
                Collections.unmodifiableMap(context.toMap()));
        requestContext = context;
    }

    /**
     * @return Return value of {@link
     *         DelegateMethod#FILESYSTEMSOURCE_PATHMAME}. May be {@literal
     *         null}.
     */
    public String getFilesystemSourcePathname() throws ScriptException {
        Object result = invoke(DelegateMethod.FILESYSTEMSOURCE_PATHMAME);
        return (String) result;
    }

    /**
     * @return Map based on the return value of {@link
     *         DelegateMethod#HTTPSOURCE_RESOURCE_INFO}, or an empty map if
     *         it returned {@literal nil}.
     */
    @SuppressWarnings("unchecked")
    public Map<String,?> getHttpSourceResourceInfo()
            throws ScriptException {
        Object result = invoke(DelegateMethod.HTTPSOURCE_RESOURCE_INFO);
        if (result instanceof String) {
            Map<String,String> map = new HashMap<>();
            map.put("uri", (String) result);
            return map;
        } else if (result instanceof Map) {
            return (Map<String,Object>) result;
        }
        return Collections.emptyMap();
    }

    /**
     * @return Return value of {@link
     *         DelegateMethod#JDBCSOURCE_DATABASE_IDENTIFIER}. May be
     *         {@literal null}.
     */
    public String getJdbcSourceDatabaseIdentifier() throws ScriptException {
        Object result = invoke(DelegateMethod.JDBCSOURCE_DATABASE_IDENTIFIER);
        return (String) result;
    }

    /**
     * @return Return value of {@link DelegateMethod#JDBCSOURCE_MEDIA_TYPE}.
     */
    public String getJdbcSourceMediaType() throws ScriptException {
        Object result = invoke(DelegateMethod.JDBCSOURCE_MEDIA_TYPE);
        return (String) result;
    }

    /**
     * @return Return value of {@link DelegateMethod#JDBCSOURCE_LOOKUP_SQL}.
     */
    public String getJdbcSourceLookupSQL() throws ScriptException {
        Object result = invoke(DelegateMethod.JDBCSOURCE_LOOKUP_SQL);
        return (String) result;
    }

    /**
     * @return Return value of {@link DelegateMethod#METADATA}.
     */
    public String getMetadata() throws ScriptException {
        Object result = invoke(DelegateMethod.METADATA);
        return (String) result;
    }

    /**
     * @return Return value of {@link DelegateMethod#OVERLAY}, or an empty map
     *         if it returned {@literal nil}.
     */
    @SuppressWarnings("unchecked")
    public Map<String,Object> getOverlayProperties() throws ScriptException {
        Object result = invoke(DelegateMethod.OVERLAY);
        if (result != null) {
            return (Map<String, Object>) result;
        }
        return Collections.emptyMap();
    }

    /**
     * @return Return value of {@link DelegateMethod#REDACTIONS}, or an empty
     *         list if it returned {@literal nil}.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String,Long>> getRedactions() throws ScriptException {
        Object result = invoke(DelegateMethod.REDACTIONS);
        if (result != null) {
            return Collections.unmodifiableList((List<Map<String, Long>>) result);
        }
        return Collections.emptyList();
    }

    /**
     * @return Return value of {@link DelegateMethod#SOURCE}. May be
     *         {@literal null}.
     */
    public String getSource() throws ScriptException {
        Object result = invoke(DelegateMethod.SOURCE);
        return (String) result;
    }

    /**
     * @return Return value of {@link DelegateMethod#S3SOURCE_OBJECT_INFO},
     *         or an empty map if it returned {@literal nil}.
     */
    @SuppressWarnings("unchecked")
    public Map<String,String> getS3SourceObjectInfo() throws ScriptException {
        Object result = invoke(DelegateMethod.S3SOURCE_OBJECT_INFO);
        if (result != null) {
            return (Map<String, String>) result;
        }
        return Collections.emptyMap();
    }

    public String getAzureStorageSourceBlobKey() throws ScriptException {
        Object result = invoke(DelegateMethod.AZURESTORAGESOURCE_BLOB_KEY);
        return (String) result;
    }

    public Map<String, Object> getExtraIIIFInformationResponseKeys() throws ScriptException {
        Object result = invoke(DelegateMethod.EXTRA_IIIF2_INFORMATION_RESPONSE_KEYS);
        if (result != null) {
            return (Map<String, Object>) result;
        }
        return Collections.emptyMap();
    }

    public Object authorize() throws ScriptException {
        return invoke(DelegateMethod.AUTHORIZE);
    }
}
