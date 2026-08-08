package com.inkvite.inkviteback.storage.exception

class InvalidImageContentTypeException : RuntimeException("Image must be a JPEG, PNG, or WebP image")

class ImageTooLargeException : RuntimeException("Image must not exceed 5 MB")

class ImageUploadFailedException(cause: Throwable) : RuntimeException("Failed to upload image", cause)
