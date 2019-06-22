package edu.illinois.library.cantaloupe.script;

/**
 * Available delegate methods.
 */
public enum DelegateMethod {
    /**
     * Called by {@link DelegateProxy#setRequestContext(RequestContext)}
     */
    REQUEST_CONTEXT_SETTER,

    /**
     * Called by {@link DelegateProxy#authorize()}.
     */
    AUTHORIZE,

    /**
     * Called by {@link DelegateProxy#getAzureStorageSourceBlobKey()}.
     */
    AZURESTORAGESOURCE_BLOB_KEY,

    /**
     * Called by {@link DelegateProxy#getExtraIIIFInformationResponseKeys()}.
     */
    EXTRA_IIIF2_INFORMATION_RESPONSE_KEYS,

    /**
     * Called by {@link DelegateProxy#getFilesystemSourcePathname()}.
     */
    FILESYSTEMSOURCE_PATHMAME,

    /**
     * Called by {@link DelegateProxy#getHttpSourceResourceInfo()}.
     */
    HTTPSOURCE_RESOURCE_INFO,

    /**
     * Called by {@link DelegateProxy#getJdbcSourceDatabaseIdentifier()}.
     */
    JDBCSOURCE_DATABASE_IDENTIFIER,

    /**
     * Called by {@link DelegateProxy#getJdbcSourceMediaType()}.
     */
    JDBCSOURCE_MEDIA_TYPE,

    /**
     * Called by {@link DelegateProxy#getJdbcSourceLookupSQL()}.
     */
    JDBCSOURCE_LOOKUP_SQL,

    /**
     * Called by {@link DelegateProxy#getMetadata()}.
     */
    METADATA,

    /**
     * Called by {@link DelegateProxy#getOverlayProperties()}.
     */
    OVERLAY,

    /**
     * Called by {@link DelegateProxy#getRedactions()}.
     */
    REDACTIONS,

    /**
     * Called by {@link DelegateProxy#getSource()}.
     */
    SOURCE,

    /**
     * Called by {@link DelegateProxy#getS3SourceObjectInfo()}.
     */
    S3SOURCE_OBJECT_INFO;
}
