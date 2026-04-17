package com.example.myapplication.network

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object MusicApi {

    private const val BASE_URL = "https://www.jiosaavn.com/api.php"
    private const val DES_KEY = "38346591"

    val client: OkHttpClient = createTrustAllClient()

    private fun createTrustAllClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun search(query: String, limit: Int = 25): SaavnSearchResponse {
        return withContext(Dispatchers.IO) {
            val url = "$BASE_URL?__call=search.getResults&p=1&q=${query.encodeUrl()}" +
                "&_format=json&_marker=0&api_version=4&ctx=web6dot0&n=$limit"
            val response = executeGet(url)
            json.decodeFromString<SaavnSearchResponse>(response)
        }
    }

    suspend fun getMediaUrl(encryptedMediaUrl: String, bitrate: String = "128"): String {
        return withContext(Dispatchers.IO) {
            val url = "$BASE_URL?__call=song.generateAuthToken" +
                "&url=${encryptedMediaUrl.encodeUrl()}" +
                "&bitrate=$bitrate&api_version=4&_format=json&ctx=web6dot0&_marker=0"
            val response = executeGet(url)
            val authResponse = json.decodeFromString<SaavnAuthResponse>(response)
            authResponse.authUrl
        }
    }

    fun decryptMediaUrl(encryptedUrl: String): String {
        return try {
            val keySpec = DESKeySpec(DES_KEY.toByteArray())
            val keyFactory = SecretKeyFactory.getInstance("DES")
            val secretKey = keyFactory.generateSecret(keySpec)
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val encrypted = Base64.decode(encryptedUrl, Base64.DEFAULT)
            val decrypted = cipher.doFinal(encrypted)
            String(decrypted)
        } catch (e: Exception) {
            ""
        }
    }

    private fun executeGet(url: String): String {
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .build()
        return client.newCall(request).execute().use { response ->
            response.body?.string() ?: throw Exception("Empty response")
        }
    }

    private fun String.encodeUrl(): String {
        return java.net.URLEncoder.encode(this, "UTF-8")
    }
}
