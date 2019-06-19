package github

fun RawPullRequest.hasLabel(name: String): Boolean =
    labels.any { it.name == name }

val List<RawStatus>.allSucceeded: Boolean
    get() = all(RawStatus::isSucceeded)

val RawStatus.isSucceeded: Boolean
    get() = state == "success"
