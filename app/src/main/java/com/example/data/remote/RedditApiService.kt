package com.example.data.remote

import com.example.data.remote.model.RedditResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RedditApiService {

    @GET("r/{subreddits}/{sort}.json")
    suspend fun getSubredditFeed(
        @Path("subreddits") subreddits: String,
        @Path("sort") sort: String,
        @Query("limit") limit: Int,
        @Query("after") after: String?
    ): RedditResponse

    companion object {
        private const val BASE_URL = "https://rl.bloat.cat/"

        fun create(): RedditApiService {
            // Real-world browser user-agent. Required to bypass strict anti-scraping and 403 Forbidden blockers on Reddit's CDN and feed endpoints.
            val userAgentInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .build()
                chain.proceed(request)
            }

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(userAgentInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(RedditApiService::class.java)
        }
    }
}
