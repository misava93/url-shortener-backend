package com.skillshare.tinyurl.utils

import ch.qos.logback.classic.LoggerContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class Logging {
    val log: Logger by lazy { LoggerFactory.getLogger(javaClass.enclosingClass::class.java) }
}

/**
 * Gracefully shutdowns logging
 */
fun shutdownLogging() {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    loggerContext.stop()
}
