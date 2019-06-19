package github

import retrofit2.http.GET

interface UsersService {

    @GET("user")
    suspend fun self(): User
}
