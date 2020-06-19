package com.skillshare.tinyurl.server

import com.fasterxml.jackson.databind.SerializationFeature
import com.skillshare.tinyurl.model.ShortUrlMetadata
import com.skillshare.tinyurl.model.UrlAccessRequest
import com.skillshare.tinyurl.model.UrlDisableRequest
import com.skillshare.tinyurl.model.UrlEnableRequest
import com.skillshare.tinyurl.model.UrlShortenRequest
import com.skillshare.tinyurl.model.UrlStatsRequest
import com.skillshare.tinyurl.service.KeyDoesNotExistException
import com.skillshare.tinyurl.service.UrlDisabledException
import com.skillshare.tinyurl.service.UrlShortener
import com.skillshare.tinyurl.service.ValidationException
import com.skillshare.tinyurl.utils.Logging
import com.skillshare.tinyurl.utils.onExit
import com.skillshare.tinyurl.utils.shutdownLogging
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.origin
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.ApplicationRequest
import io.ktor.request.receive
import io.ktor.request.userAgent
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.event.Level
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Class that exposes a server using Ktor (Kotlin framework for building server and client applications)
 */
class Server(private val port: Int, private val host: String) {
    private val server: ApplicationEngine by lazy { initServer() }
    // the URL Shortener service
    // NOTE: we use localhost because the links are opened ultimately in the local machine
    // that is running this service and is easier to do this while prototyping
    private val urlShortener = UrlShortener("localhost:$port")

    init {
        bootstrap()
    }

    /**
     * Starts the server
     */
    fun start() = server.start(wait = true)

    /**
     * Bootstraps the application
     */
    private fun bootstrap() {
        installShutdownHook()
    }

    /**
     * Installs a shutdown hook so we can gracefully shutdown the application
     */
    private fun installShutdownHook() =
        onExit("Thread-shutdown") {
            log.warn("VM is shutting down. Stopping the server...")
            try {
                server.stop(3, 5, TimeUnit.SECONDS)
            } catch (ex: Exception) {
                log.error("Error while trying to shutdown the server", ex)
            } finally {
                shutdownLogging()
            }
        }

    /**
     * Initializes and configures the server
     */
    private fun initServer() =
        embeddedServer(Netty, port, host) {
            log.info("Initializing server on port $port and host $host")
            install(DefaultHeaders)
            install(CallLogging) {
                level = Level.INFO
            }
            install(ContentNegotiation) {
                jackson {
                    // Configure Jackson's ObjectMapper here
                    enable(SerializationFeature.INDENT_OUTPUT)
                }
            }
            // here are the exposed APIs
            routing {
                /*
                    API: shorten URL
                 */
                post("/url") {
                    val request = call.receive<UrlShortenRequest>()
                    log.info("Will shorten URL: ${request.originalUrl}")

                    try {
                        val response = urlShortener.shortenUrl(request)
                        log.info("Successfully shortened URL '${request.originalUrl}' to: ${response.shortUrl}")
                        this.call.respond(HttpStatusCode.OK, response)
                    }
                    catch(ex: ValidationException) {
                        this.call.respond(HttpStatusCode.BadRequest, "${ex.message}")
                    }
                    catch(ex: Exception) {
                        this.call.respond(HttpStatusCode.InternalServerError, "${ex.message}")
                    }

                }
                /*
                    API: access/click shorten URL
                 */
                get("/{key}") {
                    val key = call.parameters["key"] ?: ""
                    try {
                        val request = UrlAccessRequest(key, getRequestMetadata(call.request))
                        val response = urlShortener.accessShortUrl(request)
                        log.info("User will be redirected to: ${response.originalUrl}")
                        this.call.respondRedirect(response.originalUrl)
                    }
                    catch (ex: KeyDoesNotExistException) {
                        this.call.respond(HttpStatusCode.BadRequest, "${ex.message}")
                    }
                    catch (ex: UrlDisabledException) {
                        this.call.respond(HttpStatusCode.NotFound, "${ex.message}")
                    }
                    catch(ex: Exception) {
                        this.call.respond(HttpStatusCode.InternalServerError, "${ex.message}")
                    }

                }
                /*
                    API: fetch statistics from URL
                 */
                get("/stats") {
                    val url = call.request.queryParameters["url"] ?: ""
                    log.info("Will fetch stats from URL: $url")

                    try {
                        val response = urlShortener.getUrlStats(UrlStatsRequest(url))
                        log.info("Successfully fetched statistics from URL: $url")
                        this.call.respond(HttpStatusCode.OK, response)
                    }
                    catch(ex: KeyDoesNotExistException) {
                        this.call.respond(HttpStatusCode.BadRequest, "${ex.message}")
                    }
                    catch(ex: Exception) {
                        this.call.respond(HttpStatusCode.InternalServerError, "${ex.message}")
                    }

                }
                /*
                    API: enable URL
                 */
                post("/enable") {
                    val request = call.receive<UrlEnableRequest>()
                    log.info("Will enable URL: {}", request.url)

                    try {
                        val response = urlShortener.enableUrl(request)
                        log.info("Successfully enabled URL: ${request.url}")
                        this.call.respond(HttpStatusCode.OK, response)
                    }
                    catch(ex: ValidationException) {
                        this.call.respond(HttpStatusCode.BadRequest, "${ex.message}")
                    }
                    catch(ex: Exception) {
                        this.call.respond(HttpStatusCode.InternalServerError, "${ex.message}")
                    }
                }
                /*
                    API: enable URL
                 */
                post("/disable") {
                    val request = call.receive<UrlDisableRequest>()
                    log.info("Will disable URL: {}", request.url)

                    try {
                        val response = urlShortener.disableUrl(request)
                        log.info("Successfully disabled URL: ${request.url}")
                        this.call.respond(HttpStatusCode.OK, response)
                    }
                    catch(ex: ValidationException) {
                        this.call.respond(HttpStatusCode.BadRequest, "${ex.message}")
                    }
                    catch(ex: Exception) {
                        this.call.respond(HttpStatusCode.InternalServerError, "${ex.message}")
                    }
                }
            }
        }

    /**
     * helper function that extracts relevant metadata information from the request
     */
    private fun getRequestMetadata(request: ApplicationRequest): ShortUrlMetadata {
        val originIp = request.origin.remoteHost
        val userAgent = request.userAgent() ?: "Not provided"
        val datetimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("M/d/y H:m:ss"))

        return ShortUrlMetadata(originIp, userAgent, datetimeStr)
    }

    companion object: Logging() {
        @JvmStatic fun main(args: Array<String>) {
            // get app configuration
            val config = getAppConfig()
            // start server
            Server(config.port, config.host).start()
        }
    }
}

fun getAppConfig(): AppConfig {
    val port = PORT_ENV_KEY.getEnvVar()?.toInt() ?: 8080
    val host = HOST_ENV_KEY.getEnvVar() ?: "localhost"

    return AppConfig(port, host)
}

/**
 * Java pojo that holds application configuration
 */
data class AppConfig(val port: Int, val host: String)

/*
    Helpful Extension Functions
 */
fun String.getEnvVar(): String? = System.getenv(this)

/*
    Constants
 */
private val PORT_ENV_KEY = "APP_PORT"
private val HOST_ENV_KEY = "APP_HOST"
