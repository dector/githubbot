package github

data class RawUser(
    val login: String = "",
    val name: String = ""
)

data class RawPullRequest(
    val id: Int = -1,
    val state: String = "",
    val title: String = "",
    val number: Int = -1,
    val head: RawHead = RawHead(),
    val labels: List<RawLabel> = emptyList()
)

data class RawLabel(
    val id: Long = -1,
    val name: String = ""
)

data class RawHead(
    val ref: String = "",
    val sha: String = ""
)

data class RawStatus(
    val state: String
)

data class RawMergePullResponse(
    val merged: Boolean?,
    val message: String
)

enum class MergeMethod(private val value: String) {
    Merge("merge"),
    Rebase("rebase");

    override fun toString() = value
}

fun RawPullRequest.coordinatesOn(repo: Repository.Coordinates) = PullRequest.Coordinates(
    repo = repo,
    number = number
)
