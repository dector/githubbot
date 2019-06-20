import github.GithubApi

class GithubUseCases(private val api: GithubApi) {

    suspend fun getUserName(): String = api.users().self().name
}
