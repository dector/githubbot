package ext

import okhttp3.MediaType
import okhttp3.RequestBody

fun Any.asRequestBody() = RequestBody.create(
    MediaType.parse("application/json; charset=utf-8"),
    toJson().also { println(it) }
)
