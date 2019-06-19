import com.jcabi.github.*
import com.jcabi.http.response.JsonResponse
import com.jcabi.http.response.RestResponse
import java.net.HttpURLConnection

class GitHubClientWrapper(private val client: Github, val dryRun: Boolean) {

    fun canBeMerged(pullRequest: Pull): Boolean = execute("checking if can be merged") {
        val coords = pullRequest.repo().coordinates()
        val request = client.entry()
            .uri()
            .path("/repos")
            .path(coords.user())
            .path(coords.repo())
            .path("/commits")
            .path(pullRequest.base().ref())
            .path("/status")
            .back()

        val response = request.fetch()
            .`as`(RestResponse::class.java)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .`as`(JsonResponse::class.java)
            .json().readObject();

        val isSuccess = response.getString("state") == "success"

        //if (isSuccess) return@execute true

        return@execute isSuccess
    }

    fun merge(pullRequest: Pull.Smart): Boolean = executeMaybe("merging request") {
        pullRequest.merge("", pullRequest.base().sha()) == MergeState.SUCCESS
    }.orElse { true }

    fun rebase(pullRequest: Pull) {}

    fun markBlockedForMerge(pullRequest: Pull.Smart, labels: ControlLabels) {
        val prLabels = pullRequest.issue().labels()

        executeMaybe("add 'landing blocked' label") {
            prLabels.add(listOf(labels.landingBlocked))
        }
        executeMaybe("remove 'require landing' label") {
            prLabels.remove(labels.requiresLanding)
        }
    }

    private fun pullRequests(repo: Repository): Iterable<Pull.Smart> =
        client.repos()
            .get(Coordinates.Simple(repo.owner, repo.name))
            .pulls()
            .iterate(mapOf("state" to "open"))
            .map { Pull.Smart(it) }

    private fun <T> execute(actionTitle: String, action: () -> T): T {
        logAction(actionTitle)

        return action()
    }

    private fun <T> executeMaybe(actionTitle: String, action: () -> T): MaybeExecuted<T> {
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
    client = RtGithub(config.authToken),
    dryRun = config.dryRun
)

enum class CIResolution {
    SUCCESS, FAILED, IN_PROGRESS
}

private fun Pull.Smart.requiresLandingIn(repository: Repository): Boolean = issue()
    .hasLabel(repository.controlLabels.requiresLanding)

private fun Issue.hasLabel(title: String): Boolean = labels()
    .iterate()
    .any { label: Label ->
        label.name() == title
    }

private fun fetchCIResolution(pull: Pull.Smart): CIResolution = run {
    val labels = pull.issue().labels().iterate().map { it.name() }

    when {
        labels.any { it == "ci_ok" } -> CIResolution.SUCCESS
        labels.any { it == "ci_failed" } -> CIResolution.FAILED
//        labels.any { it == "ci_inprogress" || it == "ci_notstarted" } -> CIResolution.IN_PROGRESS
        else -> CIResolution.IN_PROGRESS
    }
}
