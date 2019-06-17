import com.jcabi.github.*

class GitHubClientWrapper(private val client: Github, val dryRun: Boolean) {

    fun user(): User.Smart =
            execute("fetching user") { User.Smart(client.users().self()) }

    fun readyForLandingPullRequestsIn(repo: Repository): Sequence<Pair<Pull.Smart, CIResolution>> =
            pullRequests(repo)
                    .filter { it.requiresLandingIn(repo) }
                    .map { it to fetchCIResolution(it) }
                    .asSequence()

    private fun pullRequests(repo: Repository): Iterable<Pull.Smart> =
            execute("fetching pull requests") {
                client.repos()
                        .get(Coordinates.Simple(repo.owner, repo.name))
                        .pulls()
                        .iterate(mapOf("state" to "open"))
                        .map { Pull.Smart(it) }
            }

    fun canBeMerged(pullRequest: Pull): Boolean {
        return false // FIXME implement
    }

    fun merge(pullRequest: Pull): Boolean {
        return true // FIXME implement
    }

    fun rebase(pullRequest: Pull) {}

    fun markBlockedForMerge(pullRequest: Pull, labels: ControlLabels) {
        // Remove label
        labels.requiresLanding

        // Add label
        labels.landingBlocked
    }

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
