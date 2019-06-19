class GithubBotError(message: String) : Error(message)

enum class FatalErrorType(val message: String) {
    SECRET_FILE_NOT_FOUND(
        "`secret.properties` file not found. Checkout `secret.properties.example` for instructions"
    ),
    GITHUB_AUTH_TOKEN_NOT_FOUND(
        "`github_auth_token` not found in `secret.properties` or it is empty"
    )
}

fun fatalError(type: FatalErrorType): GithubBotError = GithubBotError(type.message)
