import components.*
import ext.shutdownNow
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main() {
    // Load environment
    val environmentConfig = try {
        parseEnvironmentConfig()
            .verifyOrThrow()
    } catch (e: GithubBotError) {
        System.err.println("[Fatal Error] ${e.message}")
        exitProcess(1)
    }

    // Build dependencies
    val config = buildBotConfiguration(environmentConfig)
    val okHttpClient = buildOkHttpClient(config.authToken)
    val api = buildApi(okHttpClient)
    val useCases = GithubUseCases(api)
    val bot = Bot(config, useCases, api)

    // Run
    runBlocking {
        bot.greetMe()

        bot.autoMergeReadyPullRequests()
    }

    // Shutdown app
    okHttpClient.shutdownNow()
}
