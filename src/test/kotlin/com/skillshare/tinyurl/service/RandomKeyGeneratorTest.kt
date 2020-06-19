package com.skillshare.tinyurl.service

import org.junit.Test


class RandomKeyGeneratorTest {
    private val keyGenerator = RandomKeyGenerator()

    @Test
    fun getKeyNoCollisions() {
        val keySet = mutableSetOf<String>()

        for(i in (1..1000)) {
            keySet.add(keyGenerator.getKey())
        }

        // check that the set contains expected number of elements (i.e. no collisions occurred)
        assert(keySet.size == 1000)
    }

    @Test
    fun getKeyExpectedSize() {
        val key = keyGenerator.getKey()

        // check that keys are of size 8
        assert(key.length == 8)
    }
}
