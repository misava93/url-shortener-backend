package com.skillshare.tinyurl.service

import kotlin.random.Random

/**
 * Class that is in charge of maintaining a pool of unique random keys
 *
 * This mimics what would be a Random Key Generator service using in-memory data structures
 */
class RandomKeyGenerator {
    private val characterPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    // with key of size 8 we have a total of 62^8 (218,340,105,584,896) possible strings
    private val keyLength = 8
    private val keyPoolSize = 10
    private val keyPool : MutableSet<String> by lazy { generateKeys(keyPoolSize) }

    /**
     * returns a unique random key from the existing pool of keys. It also replenishes the pool of keys
     * by generating a new one
     */
    fun getKey(): String {
        var key = ""
        // fetch a key from the existing pool if possible
        if (keyPool.size > 0) {
            // get first key from pool
            key = keyPool.first()
            // remove key from pool
            keyPool.remove(key)
        }

        // generate a new key and add it to the pool
        var newKey = generateKey()
        while (newKey in keyPool)
            newKey = generateKey()
        keyPool.add(newKey)

        return key
    }

    private fun generateKeys(n: Int) = (1..n).map { generateKey() }.toMutableSet()

    private fun generateKey() = (1..keyLength)
        .map { Random.nextInt(0, characterPool.size) }
        .map(characterPool::get)
        .joinToString("")
}
