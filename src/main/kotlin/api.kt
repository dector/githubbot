import github.GithubApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

fun buildApi(okHttpClient: OkHttpClient) = GithubApi(
    retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
)

fun buildOkHttpClient(authToken: String): OkHttpClient = OkHttpClient.Builder()
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
