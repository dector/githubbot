import java.io.File
import java.util.*

data class EnvironmentConfig(val githubAuthToken: String?)

fun parseEnvironmentConfig(): EnvironmentConfig {
    val file = File("secret.properties")
    if (!file.exists())
        throw fatalError(FatalErrorType.SECRET_FILE_NOT_FOUND)

    val properties = Properties().apply { load(file.inputStream()) }

    return EnvironmentConfig(
            githubAuthToken = properties.getProperty("github_auth_token")
    )
}
