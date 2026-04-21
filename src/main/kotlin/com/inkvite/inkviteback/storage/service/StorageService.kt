package com.inkvite.inkviteback.storage.service

interface StorageService {
    /**
     * Uploads [bytes] under the given [key] with the specified [contentType].
     * Returns the full public URL of the uploaded object.
     */
    fun upload(key: String, bytes: ByteArray, contentType: String): String

    fun getUrl(key: String): String
}
