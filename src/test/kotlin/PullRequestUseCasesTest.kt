import github.GithubApi
import github.IssuesService
import io.kotlintest.shouldNotThrowAny
import io.kotlintest.specs.BehaviorSpec
import io.mockk.*
import okhttp3.RequestBody

class PullRequestUseCasesTest : BehaviorSpec({

    Given("PullRequestUseCases for comments") {
        val commentMessage = "Looks fine!"
        val useCases = PullRequestUseCases(
            api = apiForMessage(commentMessage, coordinates()),
            coordinates = coordinates()
        )

        When("posting comment") {
            useCases.postComment(commentMessage)

            Then("should not crash") {
                shouldNotThrowAny { }
            }
        }
    }

    Given("PullRequestUseCases for adding labels") {
        val labels = listOf("one", "two")
        val useCases = PullRequestUseCases(
            api = apiForAddingLabels(labels, coordinates()),
            coordinates = coordinates()
        )

        When("adding few labels") {
            useCases.addLabels(labels)

            Then("should not crash") {
                shouldNotThrowAny { }
            }
        }

        When("adding no labels") {
            useCases.addLabels(emptyList())

            Then("should not crash") {
                shouldNotThrowAny { }
            }
        }
    }

    Given("PullRequestUseCases for removing labels") {
        val label = "remove-me"
        val useCases = PullRequestUseCases(
            api = apiForRemovingLabel(label, coordinates()),
            coordinates = coordinates()
        )

        When("removing a label") {
            useCases.removeLabel(label)

            Then("should not crash") {
                shouldNotThrowAny { }
            }
        }

        When("removing empty label") {
            useCases.removeLabel("")

            Then("should not crash") {
                shouldNotThrowAny { }
            }
        }
    }
})

private fun apiForMessage(commentMessage: String, coordinates: PullRequest.Coordinates) = api {
    coEvery {
        postComment(
            coordinates.repo.owner, coordinates.repo.name,
            coordinates.number,
            any<RequestBody>() // Workaround
//            commentMessage
        )
    } just runs
}

private fun apiForAddingLabels(labels: List<String>, coordinates: PullRequest.Coordinates) = api {
    coEvery {
        addLabels(
            coordinates.repo.owner, coordinates.repo.name,
            coordinates.number,
            any<RequestBody>() // Workaround
//            labels
        )
    } just runs
}

private fun apiForRemovingLabel(label: String, coordinates: PullRequest.Coordinates) = api {
    coEvery {
        removeLabel(
            coordinates.repo.owner, coordinates.repo.name,
            coordinates.number,
            label
        )
    } just runs
}

private fun api(init: IssuesService.() -> Unit) = run {
    val issuesService = mockk<IssuesService> {
        init()
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
