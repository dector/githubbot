import FatalErrorType.GITHUB_AUTH_TOKEN_NOT_FOUND
import FatalErrorType.SECRET_FILE_NOT_FOUND
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.UserService
import java.io.File
import java.util.*
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

    bot.greetMe()

//    bot.autoMergeReadyPullRequests()
}

class Bot(private val configuration: Configuration, private val client: GitHubClientWrapper) {

    fun greetMe() {
        println("Connecting to github...")

        val user = client.user().name

        println("Hello, $user")
    }

    fun autoMergeReadyPullRequests() {
        // Find all PRs with "landing" label

        // Has CI resolution

        // Green:
        // Check if they are mergeable
        //     Yes -> is CI success?
        //         Yes -> Merge
        //         No -> remove label (add another "landing blocked") and notify owner
        //     No -> Rebase PR with master

        // Red:
        // Remove label (add another "landing blocked") and notify owner
    }

    data class Configuration(
            val authToken: String,
            val repositories: List<Repository>,
            val dryRun: Boolean = false
    )
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

class GitHubClientWrapper(private val client: GitHubClient, val dryRun: Boolean) {

    fun user(): User = execute("fetching user") { UserService(client).user }

    private fun <T> execute(actionTitle: String, action: () -> T): T {
        logAction(actionTitle)

        return action()
    }

    private fun <T> maybe(actionTitle: String, action: () -> T): MaybeExecuted<T> {
        logAction(actionTitle)

        return if (!dryRun)
            Executed(action())
        else Mocked()
    }

    private fun logAction(actionTitle: String) {
        val prefix = if (dryRun) "Dry Run" else "Executing"

        println("[$prefix] $actionTitle")
    }
}

private sealed class MaybeExecuted<T>(val result: T?)
private class Executed<T>(result: T) : MaybeExecuted<T>(result)
private class Mocked<T>() : MaybeExecuted<T>(null)

private fun <T> MaybeExecuted<T>.orElse(alternative: () -> T): T = when (this) {
    is Executed -> result!!
    is Mocked -> alternative()
}

fun buildClient(config: Bot.Configuration) = GitHubClientWrapper(
        client = GitHubClient().apply { setOAuth2Token(config.authToken) },
        dryRun = config.dryRun
)

data class EnvironmentConfig(val githubAuthToken: String?)

fun parseEnvironmentConfig(): EnvironmentConfig {
    val file = File("secret.properties")
    if (!file.exists())
        throw fatalError(SECRET_FILE_NOT_FOUND)

    val properties = Properties().apply { load(file.inputStream()) }

    return EnvironmentConfig(
            githubAuthToken = properties.getProperty("github_auth_token")
    )
}

class GithubBotError(message: String) : Error(message)

private fun fatalError(type: FatalErrorType): GithubBotError = GithubBotError(type.message)

enum class FatalErrorType(val message: String) {
    SECRET_FILE_NOT_FOUND("`secret.properties` file not found. Checkout `secret.properties.example` for instructions"),
    GITHUB_AUTH_TOKEN_NOT_FOUND("`github_auth_token` not found in `secret.properties` or it is empty")
}

data class Repository(
        val owner: String,
        val name: String,
        val controlLabels: ControlLabels
)

data class ControlLabels(
        val requiresLanding: String,
        val landingBlocked: String
)
