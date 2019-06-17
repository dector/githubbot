data class Repository(
        val owner: String,
        val name: String,
        val controlLabels: ControlLabels
)

data class ControlLabels(
        val requiresLanding: String,
        val landingBlocked: String
)
