import FatalErrorType.GITHUB_AUTH_TOKEN_NOT_FOUND
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
    val bot = Bot(config, buildClient(config))

//    bot.greetMe()

    bot.autoMergeReadyPullRequests()
}

fun buildBotConfiguration(environmentConfig: EnvironmentConfig) = Bot.Configuration(
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
