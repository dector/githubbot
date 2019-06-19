import github.GithubApi

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
        configuration.repositories.forEach(::autoMergeReadyPullRequests)
    }

    private fun autoMergeReadyPullRequests(repository: Repository) {
        val pullRequests = client.readyForLandingPullRequestsIn(repository)

        pullRequests.forEach { (pullRequest, ciResolution) ->
            println("Processing PR: ${pullRequest.title()}")
            println("with CI resolution: $ciResolution")

            when (ciResolution) {
                CIResolution.SUCCESS -> {
                    val canBeMerged = false

                    if (client.canBeMerged(pullRequest)) {
                        client.merge(pullRequest)
                    } else {
                        client.rebase(pullRequest)
                    }
                }
                CIResolution.FAILED -> {
                    client.markBlockedForMerge(pullRequest, repository.controlLabels)
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
