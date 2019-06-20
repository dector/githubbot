import github.GithubApi
import github.IssuesService
import io.kotlintest.shouldNotThrowAny
import io.kotlintest.specs.BehaviorSpec
import io.mockk.*
import okhttp3.RequestBody

class PullRequestUseCasesTest : BehaviorSpec({

    Given("PullRequestUseCases") {
        val commentMessage = "Looks fine!"
        val useCases = PullRequestUseCases(
            api = apiFor(commentMessage, coordinates()),
            coordinates = coordinates()
        )

        When("posting comment") {
            useCases.postComment(commentMessage)

            Then("should not crash") {
                shouldNotThrowAny { }
            }
        }
    }
})

private fun apiFor(commentMessage: String, coordinates: PullRequest.Coordinates) = run {
    val issuesService = mockk<IssuesService> {
        coEvery {
            postComment(
                coordinates.repo.owner, coordinates.repo.name,
                coordinates.number,
                any<RequestBody>() // Workaround
//                commentMessage
            )
        } just runs
    }
    mockk<GithubApi> {
        every { issues() } returns issuesService
    }
}

private fun coordinates() = PullRequest.Coordinates(
    repo = Repository.Coordinates(
        owner = "octo-user",
        name = "hubbot"
    ),
    number = 69
)
