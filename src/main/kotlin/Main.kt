import ext.shutdownNow
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main() {
    val environmentConfig = try {
        parseEnvironmentConfig()
            .verifyOrThrow()
    } catch (e: GithubBotError) {
        System.err.println("[Fatal Error] ${e.message}")
        exitProcess(1)
    }

    val config = buildBotConfiguration(environmentConfig)
    val okHttpClient = buildOkHttpClient(config.authToken)
    val api = buildApi(okHttpClient)
    val bot = Bot(config, buildClient(config), api)

    runBlocking {
        // bot.greetMe()

        bot.autoMergeReadyPullRequests()
    }

    okHttpClient.shutdownNow()
}
