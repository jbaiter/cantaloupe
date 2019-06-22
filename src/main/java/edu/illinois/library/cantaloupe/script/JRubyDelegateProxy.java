package edu.illinois.library.cantaloupe.script;

import com.google.common.collect.ImmutableMap;
import edu.illinois.library.cantaloupe.resource.RequestContext;
import edu.illinois.library.cantaloupe.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * <p>Proxy for a delegate object. Invokes delegate object methods, optionally
 * caching the invocations.</p>
 *
 * <p>Instances should be acquired via {@link
 * DelegateProxyService#newDelegateProxy(RequestContext)}.</p>
 *
 * <p>Method return values should not be modified as this could disrupt the
 * {@link InvocationCache}.</p>
 *
 * @see <a href="https://github.com/jruby/jruby/wiki/RedBridge">JRuby Embed</a>
 * @see <a href="https://github.com/jruby/jruby/wiki/Embedding-with-JSR-223">
 *     Embedding JRuby with JSR223 - Code Examples</a>
 */
public final class JRubyDelegateProxy extends DelegateProxy {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(JRubyDelegateProxy.class);

    /**
     * Name of the Ruby delegate class.
     */
    private static final String RUBY_DELEGATE_CLASS_NAME = "CustomDelegate";


    private static final Map<DelegateMethod, String> METHOD_NAMES =
            new ImmutableMap.Builder<DelegateMethod, String>()
                    .put(DelegateMethod.REQUEST_CONTEXT_SETTER, "context=")
                    .put(DelegateMethod.AUTHORIZE, "authorize")
                    .put(DelegateMethod.AZURESTORAGESOURCE_BLOB_KEY, "azurestoragesource_blob_key")
                    .put(DelegateMethod.EXTRA_IIIF2_INFORMATION_RESPONSE_KEYS, "extra_iiif2_information_response_keys")
                    .put(DelegateMethod.FILESYSTEMSOURCE_PATHMAME, "filesystemsource_pathname")
                    .put(DelegateMethod.HTTPSOURCE_RESOURCE_INFO, "httpsource_resource_info")
                    .put(DelegateMethod.JDBCSOURCE_DATABASE_IDENTIFIER, "jdbcsource_database_identifier")
                    .put(DelegateMethod.JDBCSOURCE_MEDIA_TYPE, "jdbcsource_media_type")
                    .put(DelegateMethod.JDBCSOURCE_LOOKUP_SQL, "jdbcsource_lookup_sql")
                    .put(DelegateMethod.METADATA, "metadata")
                    .put(DelegateMethod.OVERLAY, "overlay")
                    .put(DelegateMethod.REDACTIONS, "redactions")
                    .put(DelegateMethod.SOURCE, "source")
                    .put(DelegateMethod.S3SOURCE_OBJECT_INFO, "s3source_object_info")
                    .build();

    public static void setEngineParameters() {
        // N.B.: These must be set before the ScriptEngine is instantiated.
        System.setProperty("org.jruby.embed.compat.version", "JRuby2.3");

        // Available values are singleton, singlethread, threadsafe and
        // concurrent (JSR-223 default). See
        // https://github.com/jruby/jruby/wiki/RedBridge#Context_Instance_Type
        System.setProperty("org.jruby.embed.localcontext.scope", "concurrent");

        // Available values are transient, persistent, global (JSR-223 default)
        // and bsf. See
        // https://github.com/jruby/jruby/wiki/RedBridge#Local_Variable_Behavior_Options
        System.setProperty("org.jruby.embed.localvariable.behavior", "transient");
    }

    public JRubyDelegateProxy(RequestContext context) {
        super(context);
    }

    @Override
    public String getMethodName(DelegateMethod method) {
        return METHOD_NAMES.get(method);
    }

    @Override
    protected Object createDelegate() {
        final long stamp = lock.readLock();
        final Stopwatch watch = new Stopwatch();
        try {
            Object delegate = scriptEngine.eval(
                    "\n" + RUBY_DELEGATE_CLASS_NAME + ".new" + "\n");
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
