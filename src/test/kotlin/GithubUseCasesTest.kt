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
            api = apiThatReturns(user)
        )

        When("user name requested") {
            val userName = useCases.getUserName()

            Then("it should be fetched from api") {
                userName shouldBe user.name
            }
        }
    }
})

private fun apiThatReturns(user: RawUser) = buildApi {
    coEvery { self() } returns user
}

private fun buildApi(init: UsersService.() -> Unit) = run {
    val usersService = mockk(block = init)
    mockk<GithubApi> {
        every { users() } returns usersService
    }
}
