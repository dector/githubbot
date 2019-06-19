package github

data class RawUser(
    val login: String,
    val name: String
)

data class RawPullRequest(
    val id: Int,
    val state: String,
    val title: String,
    val labels: List<RawLabel>
)

data class RawLabel(
    val id: Long,
    val name: String
)
