package edu.illinois.library.cantaloupe.script;

import com.google.common.collect.ImmutableMap;
import edu.illinois.library.cantaloupe.resource.RequestContext;
import edu.illinois.library.cantaloupe.util.Stopwatch;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GraalJsDelegateProxy extends DelegateProxy {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(JRubyDelegateProxy.class);

    private static final String JS_DELEGATE_CLASS_NAME = "CustomDelegate";

    private static final Map<DelegateMethod, String> METHOD_NAMES =
            new ImmutableMap.Builder<DelegateMethod, String>()
                    .put(DelegateMethod.REQUEST_CONTEXT_SETTER, "setRequestContext")
                    .put(DelegateMethod.AUTHORIZE, "authorize")
                    .put(DelegateMethod.AZURESTORAGESOURCE_BLOB_KEY, "getAzureStorageSourceBlobKey")
                    .put(DelegateMethod.EXTRA_IIIF2_INFORMATION_RESPONSE_KEYS, "getExtraIIIFInformationResponseKeys")
                    .put(DelegateMethod.FILESYSTEMSOURCE_PATHMAME, "getFilesystemSourcePathname")
                    .put(DelegateMethod.HTTPSOURCE_RESOURCE_INFO, "getHttpSourceResourceInfo")
                    .put(DelegateMethod.JDBCSOURCE_DATABASE_IDENTIFIER, "getJdbcSourceDatabaseIdentifier")
                    .put(DelegateMethod.JDBCSOURCE_MEDIA_TYPE, "getJdbcSourceMediaType")
                    .put(DelegateMethod.JDBCSOURCE_LOOKUP_SQL, "getJdbcSourceLookupSQL")
                    .put(DelegateMethod.METADATA, "getMetadata")
                    .put(DelegateMethod.OVERLAY, "getOverlayProperties")
                    .put(DelegateMethod.REDACTIONS, "getRedactions")
                    .put(DelegateMethod.SOURCE, "getSource")
                    .put(DelegateMethod.S3SOURCE_OBJECT_INFO, "getS3SourceObjectInfo")
                    .build();

    public GraalJsDelegateProxy(RequestContext requestContext) {
        super(requestContext);
    }

    @Override
    public String getMethodName(DelegateMethod method) {
        return METHOD_NAMES.get(method);
    }

    @Override
    protected void setRequestContext(RequestContext context) throws ScriptException {
        invokeUncached(DelegateMethod.REQUEST_CONTEXT_SETTER,
                ProxyObject.fromMap(Collections.unmodifiableMap(context.toMap())));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Long>> getRedactions() throws ScriptException {
        Object result = invoke(DelegateMethod.REDACTIONS);
        if (result != null) {
            Map<Integer, Map<String, Long>> map = (Map<Integer, Map<String, Long>>) result;
            return new ArrayList<>(map.values());
        }
        return Collections.emptyList();
    }

    @Override
    protected Object createDelegate() {
        final long stamp = lock.readLock();
        final Stopwatch watch = new Stopwatch();
        try {
            Object delegate = scriptEngine.eval("\n" +
                    "new " + JS_DELEGATE_CLASS_NAME + "()" + "\n");
            LOGGER.trace("Instantiated delegate object in {}", watch);
            return delegate;
        } catch (javax.script.ScriptException e) {
            LOGGER.error(e.getMessage());
            return null;
        } finally {
            lock.unlock(stamp);
        }
    }
}
