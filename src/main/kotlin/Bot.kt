import github.GithubApi
import github.RawPullRequest
import github.getOpen
import github.hasLabel
import kotlinx.coroutines.runBlocking

class Bot(
    private val configuration: Configuration,
    private val client: GitHubClientWrapper,
    private val api: GithubApi
) {

    suspend fun greetMe() {
        println("Connecting to github...")

        val userName = api.users().self().name

        println("Hello, $userName")
    }

    fun autoMergeReadyPullRequests() {
        configuration.repositories
            .forEach(::autoMergeReadyPullRequests)
    }

    private fun autoMergeReadyPullRequests(repository: Repository) = runBlocking {
        val pullRequestsForLanding = api.pulls()
            .getOpen(repository.owner, repository.name)
            .filter { it.hasLabel(repository.controlLabels.requiresLanding) }

        pullRequestsForLanding.forEach { pull ->
            println("=== ${pull.title} ===")

            print("Loading CI resolution... ")
            val ciResolution = fetchCIResolution(pull)
            println("Loaded: $ciResolution")

            when (ciResolution) {
                CIResolution.SUCCESS -> {
                    /*val canBeMerged = false

                    if (client.canBeMerged(pullRequest)) {
                        client.merge(pullRequest)
                    } else {
                        client.rebase(pullRequest)
                    }*/
                }
                CIResolution.FAILED -> {
                    //client.markBlockedForMerge(pullRequest, repository.controlLabels)
                    // Notify owner
                }
                CIResolution.IN_PROGRESS -> {
                    // do nothing
                }
            }
        }
    }

    data class Configuration(
        val authToken: String,
        val repositories: List<Repository>,
        val dryRun: Boolean = false
    )
}

private fun fetchCIResolution(pull: RawPullRequest): CIResolution = run {
    val labels = pull.labels.map { it.name }

    when {
        labels.any { it == "ci_ok" } -> CIResolution.SUCCESS
        labels.any { it == "ci_failed" } -> CIResolution.FAILED
//        labels.any { it == "ci_inprogress" || it == "ci_notstarted" } -> CIResolution.IN_PROGRESS
        else -> CIResolution.IN_PROGRESS
    }
}
