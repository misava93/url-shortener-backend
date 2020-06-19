package com.skillshare.tinyurl.model

/**
 * Module that holds Schema specification for the URL Shortener Service
 */
data class UrlShortenRequest(val originalUrl: String)
data class UrlShortenResponse(val shortUrl: String)

data class UrlAccessRequest(val key: String, val metadata: ShortUrlMetadata)
data class UrlAccessResponse(val originalUrl: String)

data class UrlStatsRequest(val url: String)
data class UrlStatsResponse(val shortUrl: String, val metadata: List<ShortUrlMetadata>)

data class UrlEnableRequest(val url: String)
data class UrlEnableResponse(val shortUrl: String)

data class UrlDisableRequest(val url: String)
data class UrlDisableResponse(val shortUrl: String)

data class ShortUrlMetadata(val ip: String, val userAgent: String,
                            val datetimeStr: String)
data class ShortUrl(val shortUrl: String, val originalUrl: String, val isEnabled: Boolean)
