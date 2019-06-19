package github

import retrofit2.Retrofit
import retrofit2.create

class GithubApi(private val retrofit: Retrofit) {

    fun users(): UsersService = create()

    private inline fun <reified T> create(): T = retrofit.create()
}
