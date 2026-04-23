package com.inkvite.inkviteback.storage.service

interface StorageService {
    /**
     * Uploads [bytes] under the given [key] with the specified [contentType].
     * Returns a presigned URL (1h) for the uploaded object.
     */
    fun upload(key: String, bytes: ByteArray, contentType: String): String

    /**
     * Returns a presigned GET URL (1h) for the object stored under [key].
     */
    fun getSignedUrl(key: String): String
}
