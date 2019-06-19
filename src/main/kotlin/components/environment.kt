package components

import FatalErrorType.GITHUB_AUTH_TOKEN_NOT_FOUND
import FatalErrorType.SECRET_FILE_NOT_FOUND
import fatalError
import java.io.File
import java.util.*

data class EnvironmentConfig(val githubAuthToken: String?)

fun parseEnvironmentConfig(): EnvironmentConfig {
    val file = File("secret.properties")
    if (!file.exists())
        throw fatalError(SECRET_FILE_NOT_FOUND)

    val properties = Properties().apply { load(file.inputStream()) }

    return EnvironmentConfig(
        githubAuthToken = properties.getProperty("github_auth_token")
    )
}

fun EnvironmentConfig.verifyOrThrow() = apply {
    if (githubAuthToken.isNullOrEmpty())
        throw fatalError(GITHUB_AUTH_TOKEN_NOT_FOUND)
}
