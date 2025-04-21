package com.example.tsl_app.restapi

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.net.SocketTimeoutException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object UnsafeOkHttpClient {

    class RetryInterceptor(private val retryAttempts: Int) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var response: Response? = null
            var exception: Exception? = null
            for (i in 1..retryAttempts) {
                try {
                    response = chain.proceed(chain.request())
                    return response
                } catch (e: SocketTimeoutException) {
                    exception = e
                } catch (e: IOException) {
                    exception = e
                }
            }
            if (exception != null) {
                // You can throw a custom exception or handle it accordingly
                throw IOException(
                    "Failed to execute the request due to ${exception.message}", exception
                )
            } else {
                // In case no exception was caught, which is unlikely
                throw IOException("Failed to execute the request for unknown reasons")
            }
        }
    }

    class AuthInterceptor(private val token: String) : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest: Request = chain.request()
            val builder: Request.Builder = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
            val newRequest: Request = builder.build()
            Log.d("AuthInterceptor", "Adding Authorization header: Bearer $token")
            return chain.proceed(newRequest)
        }
    }

    fun createUnsafeOkHttpClient(token: String?): OkHttpClient {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate>, authType: String
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate>, authType: String
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            })


            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(
                sslSocketFactory, trustAllCerts[0] as X509TrustManager
            )
            builder.hostnameVerifier { hostname, session -> true }

//            if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            run { interceptor.setLevel(HttpLoggingInterceptor.Level.BODY) }
            builder.addNetworkInterceptor(interceptor)
//            }
            builder.addInterceptor(RetryInterceptor(2))
            builder.addInterceptor(AuthInterceptor(token.toString()))
            //OkHttpClient okHttpClient = builder.build();
            builder.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
            builder.build()

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}