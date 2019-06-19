package github

data class RawUser(
    val login: String,
    val name: String
)

data class RawPullRequest(
    val id: Int,
    val state: String,
    val title: String,
    val number: Int,
    val head: RawHead,
    val labels: List<RawLabel>
)

data class RawLabel(
    val id: Long,
    val name: String
)

data class RawHead(
    val ref: String,
    val sha: String
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
