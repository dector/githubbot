import github.*
import kotlinx.coroutines.runBlocking

class Bot(
    private val configuration: Configuration,
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
                    if (canBeMerged(repository, pull)) {
//                        client.merge(pullRequest)
                    } else {
//                        client.rebase(pullRequest)
                    }
                }
                CIResolution.FAILED -> {
                    markBlockedForMerge(repository, pull)
                    // Notify owner
                }
                CIResolution.IN_PROGRESS -> {
                    // do nothing
                }
            }
        }
    }

    private suspend fun canBeMerged(repo: Repository, pull: RawPullRequest): Boolean {
        println("Checking if can be merged... ")

        val statuses = api.commits().getStatuses(repo.owner, repo.name, pull.head.ref)
        val result = statuses.isNotEmpty() && statuses.allSucceeded

        if (result) println("Can be merged") else println("Can't be merged")

        return result
    }

    private suspend fun markBlockedForMerge(repo: Repository, pull: RawPullRequest) {
        println("Adding label '${repo.controlLabels.landingBlocked}'")

        api.issues().addLabels(
            repo.owner, repo.name,
            pull.number.toString(),
            listOf(repo.controlLabels.landingBlocked)
        )

        println("Removing label '${repo.controlLabels.requiresLanding}'")

        api.issues().removeLabel(
            repo.owner, repo.name,
            pull.number.toString(),
            repo.controlLabels.requiresLanding
        )
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
