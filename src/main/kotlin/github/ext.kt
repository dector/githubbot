package github

fun RawPullRequest.hasLabel(name: String): Boolean =
    labels.any { it.name == name }
