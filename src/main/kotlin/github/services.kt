package github

import ext.asRequestBody
import okhttp3.RequestBody
import retrofit2.http.*

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

    @PUT("repos/{user}/{repo}/pulls/{pull_number}/merge")
    suspend fun merge(
        @Path("user") user: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int,
        @Field("sha") sha: String,
        @Field("merge_method") mergeMethod: MergeMethod
    ): RawMergePullResponse

    @PUT("repos/{user}/{repo}/pulls/{pull_number}/update-branch")
    suspend fun updateBranch(
        @Path("user") user: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int
    )
}

suspend fun PullsService.getOpen(user: String, repo: String): List<RawPullRequest> =
    getAll(user, repo, mapOf("state" to "open"))

interface IssuesService {

    @POST("/repos/{user}/{repo}/issues/{issue_number}/labels")
    suspend fun addLabels(
        @Path("user") user: String,
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int,
        @Body body: RequestBody
    )

    @DELETE("/repos/{user}/{repo}/issues/{issue_number}/labels/{label}")
    suspend fun removeLabel(
        @Path("user") user: String,
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: String,
        @Path("label") label: String
    )
}

suspend fun IssuesService.addLabels(user: String, repo: String, issueNumber: Int, labels: List<String>) =
    addLabels(user, repo, issueNumber,
        mapOf("labels" to labels).asRequestBody()
    )

interface CommitsService {

    @GET("/repos/{user}/{repo}/commits/{ref}/statuses")
    suspend fun getStatuses(
        @Path("user") user: String,
        @Path("repo") repo: String,
        @Path("ref") ref: String
    ): List<RawStatus>
}
