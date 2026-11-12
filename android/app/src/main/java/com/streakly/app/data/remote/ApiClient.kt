package com.streakly.app.data.remote

import com.streakly.app.BuildConfig
import com.streakly.app.data.local.TokenStore
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

val StreaklyJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
}

object ApiClient {

    fun create(tokenStore: TokenStore): StreaklyApi {
        val auth = Interceptor { chain ->
            val token = tokenStore.currentToken
            val request = if (token != null) {
                chain.request().newBuilder().header("Authorization", "Bearer $token").build()
            } else chain.request()

            val response = chain.proceed(request)
            // Token vencido o invalido: se cierra la sesion y la app vuelve al login.
            if (response.code == 401 && token != null) tokenStore.clearBlocking()
            response
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(client)
            .addConverterFactory(StreaklyJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(StreaklyApi::class.java)
    }
}

/** Ejecuta una llamada y convierte los errores en mensajes legibles. */
suspend fun <T> apiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: HttpException) {
    val body = e.response()?.errorBody()?.string()
    val detail = body?.let { runCatching { StreaklyJson.decodeFromString<ProblemDetails>(it) }.getOrNull() }
    Result.failure(ApiException(detail?.detail ?: detail?.title ?: defaultMessage(e.code()), e.code()))
} catch (e: IOException) {
    Result.failure(ApiException("No se pudo conectar con el servidor. Revisa que el backend esté corriendo.", null))
} catch (e: Exception) {
    Result.failure(ApiException(e.message ?: "Ocurrió un error inesperado.", null))
}

/** Para endpoints que devuelven 204 sin cuerpo. */
suspend fun apiCallUnit(block: suspend () -> retrofit2.Response<Unit>): Result<Unit> =
    apiCall {
        val response = block()
        if (!response.isSuccessful) throw HttpException(response)
    }

class ApiException(message: String, val code: Int?) : Exception(message)

private fun defaultMessage(code: Int) = when (code) {
    401 -> "Tu sesión expiró. Inicia sesión de nuevo."
    404 -> "No se encontró el recurso."
    in 500..599 -> "El servidor tuvo un problema. Intenta de nuevo."
    else -> "Error $code"
}
