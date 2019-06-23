package edu.illinois.library.cantaloupe.script.engines;

import com.google.common.collect.ImmutableMap;
import edu.illinois.library.cantaloupe.script.DelegateMethod;

import java.util.Map;

public class JavaScriptMethodMapping {
        public static final String JS_DELEGATE_CLASS_NAME = "CustomDelegate";

    public static final Map<DelegateMethod, String> METHOD_NAMES =
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

    public static String getMethodname(DelegateMethod method) {
        return METHOD_NAMES.get(method);
    }
}
