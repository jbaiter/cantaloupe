package edu.illinois.library.cantaloupe.script.engines;

import com.google.common.collect.ImmutableMap;
import edu.illinois.library.cantaloupe.resource.RequestContext;
import edu.illinois.library.cantaloupe.script.DelegateMethod;
import edu.illinois.library.cantaloupe.script.DelegateProxy;
import edu.illinois.library.cantaloupe.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JythonDelegateProxy extends DelegateProxy {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(NashornDelegateProxy.class);
    private static final String PY_DELEGATE_CLASS_NAME = "CustomDelegate";

      private static final Map<DelegateMethod, String> METHOD_NAMES =
            new ImmutableMap.Builder<DelegateMethod, String>()
                    .put(DelegateMethod.REQUEST_CONTEXT_SETTER, "set_context")
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

    public JythonDelegateProxy(RequestContext requestContext) {
        super(requestContext);
    }

    @Override
    protected Object createDelegate() {
        final long stamp = lock.readLock();
        final Stopwatch watch = new Stopwatch();
        try {
            Object delegate = scriptEngine.eval("\n" +
                    PY_DELEGATE_CLASS_NAME + "()" + "\n");
            LOGGER.trace("Instantiated delegate object in {}", watch);
            return delegate;
        } catch (javax.script.ScriptException e) {
            LOGGER.error(e.getMessage());
            return null;
        } finally {
            lock.unlock(stamp);
        }
    }

    @Override
    public String getMethodName(DelegateMethod method) {
        return METHOD_NAMES.get(method);
    }
}
