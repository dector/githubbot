import github.GithubApi
import github.RawUser
import github.UsersService
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

class GithubUseCasesTest : BehaviorSpec({

    Given("GithubUseCases") {
        val user = RawUser(name = "octo-user")
        val useCases = GithubUseCases(
            api = buildApiThatReturns(user)
        )

        When("user name requested") {
            val userName = useCases.getUserName()

            Then("it should be fetched from api") {
                userName shouldBe user.name
            }
        }
    }
})

private fun buildApiThatReturns(user: RawUser) = run {
    val usersService = mockk<UsersService> {
        coEvery { self() } returns user
    }
    mockk<GithubApi> {
        every { users() } returns usersService
    }
}
