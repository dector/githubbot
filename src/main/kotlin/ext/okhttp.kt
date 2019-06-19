package ext

import okhttp3.OkHttpClient

fun OkHttpClient.shutdownNow() = dispatcher().executorService().shutdownNow()
