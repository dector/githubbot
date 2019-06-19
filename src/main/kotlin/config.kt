fun buildBotConfiguration(environmentConfig: EnvironmentConfig) = Bot.Configuration(
    authToken = environmentConfig.githubAuthToken ?: "",
    repositories = listOf(
        Repository(
            "dector", "test_repo",
            controlLabels = ControlLabels(
                requiresLanding = "requires landing",
                landingBlocked = "landing blocked"
            )
        )
    ),
    dryRun = true
)
