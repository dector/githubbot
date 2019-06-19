import FatalErrorType.GITHUB_AUTH_TOKEN_NOT_FOUND
import ext.shutdownNow
import github.GithubApi
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.system.exitProcess

fun main() {
    val environmentConfig = try {
        parseEnvironmentConfig().apply {
            if (githubAuthToken.isNullOrEmpty()) throw fatalError(GITHUB_AUTH_TOKEN_NOT_FOUND)
        }
    } catch (e: GithubBotError) {
        println("[Fatal Error] ${e.message}")
        exitProcess(1)
    }

    val config = buildBotConfiguration(environmentConfig)
    val okHttpClient = buildOkHttpClient(config.authToken)
    val api = buildApi(okHttpClient)
    val bot = Bot(config, buildClient(config), api)

    runBlocking {
        bot.greetMe()
    }

//    bot.autoMergeReadyPullRequests()

    okHttpClient.shutdownNow()
}

private fun buildBotConfiguration(environmentConfig: EnvironmentConfig) = Bot.Configuration(
    authToken = environmentConfig.githubAuthToken ?: "",
    repositories = listOf(
        Repository(
            "dector", "test_repo",
            controlLabels = ControlLabels(
                requiresLanding = "requires landing",
                landingBlocked = "landing blocked"
            )
        )
    ),
    dryRun = true
)

private fun buildApi(okHttpClient: OkHttpClient) = GithubApi(
    retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
)

private fun buildOkHttpClient(authToken: String) = OkHttpClient.Builder()
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
