package com.inkvite.inkviteback.common

import org.slf4j.LoggerFactory

abstract class AbstractControllerAdvice {

    private val logger = LoggerFactory.getLogger(javaClass)

    protected fun handleException(e: Exception, is5xx: Boolean = false) : Map<String, String?>{
        if (is5xx) logger.warn("⚠ Server-side exception ⚠", e)
        return mapOf("error" to e.message)
    }
}