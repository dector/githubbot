import github.*
import kotlinx.coroutines.runBlocking

class Bot(
    private val configuration: Configuration,
    private val useCases: GithubUseCases,

    @Deprecated("Use `useCases` instead") private val api: GithubApi
) {

    suspend fun greetMe() {
        println("Connecting to github...")

        val userName = useCases.getUserName()

        println("Hello, $userName")
    }

    fun autoMergeReadyPullRequests() {
        configuration.repositories
            .forEach(::autoMergeReadyPullRequests)
    }

    private fun autoMergeReadyPullRequests(repository: Repository) = runBlocking {
        val repositoryUseCases = useCases.via(repository.coordinates)

        val pullRequestsForLanding = repositoryUseCases
            .getPullsWithLabel(repository.controlLabels.requiresLanding)

        pullRequestsForLanding.forEach { pull ->
            val pullRequestUseCases = repositoryUseCases.via(
                pull.coordinatesOn(repository.coordinates)
            )

            println("=== ${pull.title} ===")

            print("Loading CI resolution... ")
            val ciResolution = fetchCIResolution(pull)
            println("Loaded: $ciResolution")

            when (ciResolution) {
                CIResolution.SUCCESS -> {
                    if (canBeMerged(repository, pull, pullRequestUseCases)) {
                        println("Merging...")
                        api.pulls().merge(
                            repository.owner, repository.name,
                            pull.number, pull.head.sha, MergeMethod.Rebase
                        )
                    } else {
                        println("Updating branch...")
                        api.pulls().updateBranch(
                            repository.owner, repository.name, pull.number
                        )
                    }
                }
                CIResolution.FAILED -> {
                    markBlockedForMerge(repository, pullRequestUseCases)

                    pullRequestUseCases.postComment("Attention required.")
                }
                CIResolution.IN_PROGRESS -> {
                    // do nothing
                }
            }
        }
    }

    private suspend fun canBeMerged(repo: Repository, pull: RawPullRequest, useCases: PullRequestUseCases): Boolean {
        println("Checking if can be merged... ")

        val statuses = api.commits().getStatuses(repo.owner, repo.name, pull.head.ref)
        val result = statuses.isNotEmpty() && statuses.allSucceeded

        if (result) println("Can be merged") else println("Can't be merged")

        return result
    }

    private suspend fun markBlockedForMerge(repo: Repository, useCases: PullRequestUseCases) {
        addLabel(repo.controlLabels.landingBlocked, useCases)
        removeLabel(repo.controlLabels.requiresLanding, useCases)
    }

    private suspend fun addLabel(label: String, useCases: PullRequestUseCases) {
        println("Adding label '$label'")

        useCases.addLabels(listOf(label))
    }

    private suspend fun removeLabel(label: String, useCases: PullRequestUseCases) {
        println("Removing label '$label'")

        useCases.removeLabel(label)
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

private enum class CIResolution {
    SUCCESS, FAILED, IN_PROGRESS
}
