package com.skillshare.tinyurl.utils

/**
 * Installs a named shutdown hook with the provided [block]
 */
fun onExit(name: String, block: () -> Unit) {
    Runtime.getRuntime().addShutdownHook(object : Thread(name) {
        override fun run() {
            block()
        }
    })
}
