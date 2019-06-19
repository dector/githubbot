package ext

import com.squareup.moshi.Moshi
import kotlin.reflect.KClass

private val moshi by lazy(LazyThreadSafetyMode.NONE) { Moshi.Builder().build() }

fun <T : Any> T?.toJson(klazz: KClass<T>): String =
    if (this != null)
        moshi.adapter(klazz.java).toJson(this)
    else "{}"

inline fun <reified T : Any> T.toJson() = toJson(T::class)
