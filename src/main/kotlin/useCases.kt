import github.*

class GithubUseCases(private val api: GithubApi) {

    suspend fun getUserName(): String = api.users().self().name

    fun via(coordinates: Repository.Coordinates): RepositoryUseCases =
        RepositoryUseCases(api, coordinates)
}

class RepositoryUseCases(
    private val api: GithubApi,
    private val coordinates: Repository.Coordinates
) {

    suspend fun getPullsWithLabel(label: String): List<RawPullRequest> = api
        .pulls()
        .getOpen(
            user = coordinates.owner,
            repo = coordinates.name
        )
        .filter { it.hasLabel(label) }

    fun via(coordinates: PullRequest.Coordinates): PullRequestUseCases =
        PullRequestUseCases(api, coordinates)
}

class PullRequestUseCases(
    private val api: GithubApi,
    private val coordinates: PullRequest.Coordinates
) {

    suspend fun postComment(message: String) = api
        .issues()
        .postComment(
            user = coordinates.repo.owner,
            repo = coordinates.repo.name,
            issueNumber = coordinates.number,
            comment = message
        )

    suspend fun addLabels(labels: List<String>) {
        if (labels.isEmpty()) return

        api.issues()
            .addLabels(
                user = coordinates.repo.owner,
                repo = coordinates.repo.name,
                issueNumber = coordinates.number,
                labels = labels
            )
    }

    suspend fun removeLabel(label: String) {
        if (label.isEmpty()) return

        api.issues()
            .removeLabel(
                user = coordinates.repo.owner,
                repo = coordinates.repo.name,
                issueNumber = coordinates.number,
                label = label
            )
    }
}
