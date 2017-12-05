package com.afterlogic.auroracontacts.core.gson

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

inline fun <reified T> GsonBuilder.registerTypeAdapter(serializer: JsonSerializer<T>): GsonBuilder {
    return registerTypeAdapter(T::class.java, serializer)
}

inline fun <reified T> GsonBuilder.registerTypeAdapter(serializer: JsonDeserializer<T>): GsonBuilder {
    return registerTypeAdapter(T::class.java, serializer)
}