import github.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

class RepositoryUseCasesTest : BehaviorSpec({

    Given("RepositoryUseCases") {
        val pulls = listOf(
            RawPullRequest(id = 0, labels = labels("existing 1")),
            RawPullRequest(id = 1, labels = labels("existing 1", "existing 2")),
            RawPullRequest(id = 2, labels = labels("existing 2", "existing 3")),
            RawPullRequest(id = 3, labels = labels("existing 1", "existing 2", "unique")),
            RawPullRequest(id = 4, labels = emptyList())
        )
        val useCases = RepositoryUseCases(
            api = apiThatReturns(pulls, coordinates()),
            coordinates = coordinates()
        )

        When("pull requests with existing labels are requested") {
            val result = useCases.getPullsWithLabel("existing 1")

            Then("appropriate pulls are returned") {
                result shouldBe listOf(pulls[0], pulls[1], pulls[3])
            }
        }

        When("pull requests with unique labels are requested") {
            val result = useCases.getPullsWithLabel("non-existing")

            Then("nothing should be returned") {
                result.shouldBeEmpty()
            }
        }

        When("pull requests with non-existing labels are requested") {
            val result = useCases.getPullsWithLabel("unique")

            Then("one should be returned") {
                result shouldBe listOf(pulls[3])
            }
        }
    }
})

private fun apiThatReturns(pulls: List<RawPullRequest>, coordinates: Repository.Coordinates) = run {
    val pullsService = mockk<PullsService> {
        coEvery { getOpen(coordinates.owner, coordinates.name) } returns pulls
    }
    mockk<GithubApi> {
        every { pulls() } returns pullsService
    }
}

private fun coordinates() = Repository.Coordinates(
    owner = "octo-user",
    name = "hubbot"
)

private fun labels(vararg labels: String) = labels
    .map { RawLabel(name = it) }
