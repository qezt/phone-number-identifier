package com.github.qezt.phonenumberidentifier

import android.support.annotation.WorkerThread
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection


object Requests {
    private const val BUFFER_SIZE = 16 * 1024
    private const val CHARSET = "UTF-8"
    private const val USER_AGENT = "HTTPS Client - Android Kotlin"

    /**
     * @throws java.io.IOException
     * @return null if canceled, Response if request ended.
     */
    @WorkerThread
    fun post(url : String, headers : Map<String, String>, body : InputStream) : Response? {
        if (! url.startsWith("https://")) {
            throw IllegalArgumentException("protocol not supported: $url")
        }
        val conn = URL(url).openConnection() as HttpsURLConnection

        conn.requestMethod = "POST"

        conn.setRequestProperty("Accept-Charset", CHARSET)
        conn.setRequestProperty("User-Agent", "HTTPS Client - Android Kotlin")
        conn.setRequestProperty("Connection", "Keep-Alive")  // TODO: Close might be more appropriate, but whatever...
        conn.setRequestProperty("Cache-Control", "no-cache")
        conn.setRequestProperty("Transfer-Encoding", "chunked")
        headers.forEach { (key, value) ->
            conn.setRequestProperty(key ,value)
        }

        conn.setChunkedStreamingMode(BUFFER_SIZE)

        conn.doInput = true
        conn.doOutput = true    // indicates POST / PUT method
        conn.useCaches = false

        val outputStream = conn.outputStream

        val buffer = ByteArray(BUFFER_SIZE)
        var totalSent = 0L
        while (true) {
            val length = body.read(buffer)
            if (length <= -1) {
                break
            } else if (length == 0) {
                continue
            } else {
                outputStream.write(buffer, 0, length)
            }
            totalSent += length
        }
        outputStream.close()
        val responseBody = conn.inputStream.readBytes()
        return Response(
                statusCode = conn.responseCode,
                statusMessage = conn.responseMessage,
                headers = conn.headerFields,
                data = responseBody)
    }

    @WorkerThread
    fun get(url : String, headers : Map<String, String> = mapOf()) : Response? {
        if (! url.startsWith("https://")) {
            throw IllegalArgumentException("protocol not supported: $url")
        }
        val conn = URL(url).openConnection() as HttpsURLConnection

        conn.requestMethod = "GET"

        conn.setRequestProperty("Accept-Charset", CHARSET)
        conn.setRequestProperty("User-Agent", USER_AGENT)
        conn.setRequestProperty("Connection", "Keep-Alive")  // TODO: Close might be more appropriate, but whatever...
        conn.setRequestProperty("Cache-Control", "no-cache")
        conn.setRequestProperty("Transfer-Encoding", "chunked")
        headers.forEach { (key, value) ->
            conn.setRequestProperty(key ,value)
        }

        conn.setChunkedStreamingMode(BUFFER_SIZE)

        conn.doInput = true
        conn.doOutput = true    // indicates POST / PUT method
        conn.useCaches = false

        val responseBody = conn.inputStream.readBytes()
        return Response(
                statusCode = conn.responseCode,
                statusMessage = conn.responseMessage,
                headers = conn.headerFields,
                data = responseBody)
    }
}