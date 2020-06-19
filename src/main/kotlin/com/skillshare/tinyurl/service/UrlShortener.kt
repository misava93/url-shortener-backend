package com.skillshare.tinyurl.service

import com.skillshare.tinyurl.model.ShortUrl
import com.skillshare.tinyurl.model.ShortUrlMetadata
import com.skillshare.tinyurl.model.UrlAccessRequest
import com.skillshare.tinyurl.model.UrlAccessResponse
import com.skillshare.tinyurl.model.UrlDisableRequest
import com.skillshare.tinyurl.model.UrlDisableResponse
import com.skillshare.tinyurl.model.UrlEnableRequest
import com.skillshare.tinyurl.model.UrlEnableResponse
import com.skillshare.tinyurl.model.UrlShortenRequest
import com.skillshare.tinyurl.model.UrlShortenResponse
import com.skillshare.tinyurl.model.UrlStatsRequest
import com.skillshare.tinyurl.model.UrlStatsResponse
import java.lang.RuntimeException

/**
 * Class that implements a URL Shortener service
 */
class UrlShortener(val domain: String) {
    private val keyGenerator = RandomKeyGenerator()
    private val itemsByShortUrl = mutableMapOf<String, ShortUrl>()
    private val itemsByOriginUrl = mutableMapOf<String, ShortUrl>()
    private val metadataByShortUrl = mutableMapOf<String, MutableList<ShortUrlMetadata>>()

    /**
     * shortens the provided URL
     */
    fun shortenUrl(request: UrlShortenRequest): UrlShortenResponse {
        val url = request.originalUrl
        // generate the random key for the original url
        val key = keyGenerator.getKey()

        // create short url object and update storage
        val shortUrl = generateShortUrl(url, key)
        val shortUrlObj = ShortUrl(shortUrl, url, true)
        itemsByShortUrl[shortUrl] = shortUrlObj
        itemsByOriginUrl[url] = shortUrlObj

        return UrlShortenResponse(shortUrl)
    }

    /**
     * fulfills a shortened URL access request by returning the shortened URL if it has not been disabled
     */
    fun accessShortUrl(request: UrlAccessRequest): UrlAccessResponse {
        val key = request.key
        val metadata = request.metadata
        val shortUrl = getShortUrl(key)

        // if the provided shortened URL does not exist, then throw exception
        if (!itemsByShortUrl.containsKey(shortUrl))
            throw KeyDoesNotExistException("The provided shortened URL does not exist: $shortUrl")

        // if the URL has been disabled, throw exception
        if (!itemsByShortUrl[shortUrl]!!.isEnabled)
            throw UrlDisabledException("The provided shortened URL has been disabled: $shortUrl")

        // store metadata associated with the request
        if (shortUrl in metadataByShortUrl) {
            metadataByShortUrl[shortUrl]?.add(metadata)
        }
        else {
            metadataByShortUrl[shortUrl] = mutableListOf(metadata)
        }

        return UrlAccessResponse(itemsByShortUrl[shortUrl]!!.originalUrl)
    }

    /**
     * returns statistics/metadata associated with the provided URL
     *
     * The URL can be both:
     *  - original URL
     *  - shortened URL
     */
    fun getUrlStats(request: UrlStatsRequest): UrlStatsResponse {
        val url = request.url
        var shortUrl = checkAndGetShortUrl(url)

        // return metadata associated with the shortened URL
        val metadata = metadataByShortUrl.getOrElse(shortUrl.shortUrl, {
            throw KeyDoesNotExistException("There is no metadata associated with the provided URL: $url")
        })

        return UrlStatsResponse(shortUrl.shortUrl, metadata)
    }

    /**
     * enables the provided URL
     *
     * The URL can be both:
     *  - original URL
     *  - shortened URL
     */
    fun enableUrl(request: UrlEnableRequest): UrlEnableResponse {
        var shortUrl = checkAndGetShortUrl(request.url)
        setEnabledFlag(shortUrl, true)

        return UrlEnableResponse(shortUrl.shortUrl)
    }

    /**
     * disables the provided URL
     *
     * The URL can be both:
     *  - original URL
     *  - shortened URL
     */
    fun disableUrl(request: UrlDisableRequest): UrlDisableResponse {
        var shortUrl = checkAndGetShortUrl(request.url)
        setEnabledFlag(shortUrl, false)

        return UrlDisableResponse(shortUrl.shortUrl)
    }

    private fun setEnabledFlag(shortUrl: ShortUrl, boolean: Boolean) {
        // mark as enabled
        itemsByShortUrl[shortUrl.shortUrl] = ShortUrl(shortUrl.shortUrl, shortUrl.originalUrl, boolean)
        itemsByOriginUrl[shortUrl.originalUrl] = ShortUrl(shortUrl.shortUrl, shortUrl.originalUrl, boolean)
    }

    private fun checkAndGetShortUrl(url: String) : ShortUrl {
        var shortUrl: ShortUrl

        // first, we check if it is a short url
        if (url in itemsByShortUrl)
            shortUrl = itemsByShortUrl[url]!!
        // second, we check if it is the original url
        else if (url in itemsByOriginUrl)
            shortUrl = itemsByOriginUrl[url]!!
        else
            throw KeyDoesNotExistException("There is not shortened URL associated with the provided URL: $url")

        return shortUrl
    }

    private fun generateShortUrl(originUrl: String, key: String): String {
        if (isHttps(originUrl) or isHttp(originUrl))
            return getShortUrl(key)
        else
            throw ValidationException("The provided original URL is invalid, please provide a full and valid URL." +
                    " Provided URL: $originUrl")
    }

    private fun getShortUrl(key: String): String = "http://${this.domain}/$key"

    private fun isHttp(url: String) = !url.startsWith("https") and url.startsWith("http")

    private fun isHttps(url: String) = url.startsWith("https")
}


/*
    Exceptions
 */
class ValidationException(private val msg: String): RuntimeException(msg)
class KeyDoesNotExistException(private val msg: String): RuntimeException(msg)
class UrlDisabledException(private val msg: String): RuntimeException(msg)
