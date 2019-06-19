package github

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface UsersService {

    @GET("user")
    suspend fun self(): RawUser
}

interface PullsService {

    @GET("repos/{user}/{repo}/pulls")
    suspend fun getAll(
        @Path("user") user: String,
        @Path("repo") repo: String,
        @QueryMap params: Map<String, String>
    ): List<RawPullRequest>
}

suspend fun PullsService.getOpen(user: String, repo: String): List<RawPullRequest> =
    getAll(user, repo, mapOf("state" to "open"))
