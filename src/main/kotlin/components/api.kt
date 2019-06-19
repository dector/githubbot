package components

import github.GithubApi
import github.RetrofitGithubApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

fun buildApi(okHttpClient: OkHttpClient): GithubApi = RetrofitGithubApi(
    retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
)

fun buildOkHttpClient(authToken: String): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(logInterceptor())
    .authenticator { route, response ->
        if (response.request().header("Authorization") != null) {
            // Already tried to authenticate
            return@authenticator null
        }

        response.request().newBuilder()
            .header("Authorization", "token $authToken")
            .build()
    }
    .build()

private fun logInterceptor() = HttpLoggingInterceptor { println(it) }
    .setLevel(HttpLoggingInterceptor.Level.BASIC)
//    .setLevel(HttpLoggingInterceptor.Level.BODY)
