data class Repository(
    val owner: String,
    val name: String,
    val controlLabels: ControlLabels
) {
    val coordinates = Coordinates(
        owner = owner,
        name = name
    )

    data class Coordinates(
        val owner: String,
        val name: String
    )
}

data class ControlLabels(
    val requiresLanding: String,
    val landingBlocked: String
)
