package com.afterlogic.auroracontacts.data.p7.api.model

import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * Created by sunny on 23.12.2017.
 * mail: mail@sunnydaydev.me
 */

class JsonList<T>(private val list: List<T>) {

    class Deserializer : JsonSerializer<JsonList<*>> {

        override fun serialize(src: JsonList<*>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src.list)
        }

    }

}