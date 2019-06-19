package github

import retrofit2.Retrofit
import retrofit2.create

interface GithubApi {

    fun users(): UsersService
    fun pulls(): PullsService
    fun issues(): IssuesService
    fun commits(): CommitsService
}

class RetrofitGithubApi(private val retrofit: Retrofit) : GithubApi {

    override fun users(): UsersService = create()
    override fun pulls(): PullsService = create()
    override fun issues(): IssuesService = create()
    override fun commits(): CommitsService = create()

    private inline fun <reified T> create(): T = retrofit.create()
}
