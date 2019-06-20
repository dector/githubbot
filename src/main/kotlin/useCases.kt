import github.GithubApi
import github.RawPullRequest
import github.getOpen
import github.hasLabel

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
        .getOpen(coordinates.owner, coordinates.name)
        .filter { it.hasLabel(label) }
}
