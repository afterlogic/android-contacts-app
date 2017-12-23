package com.afterlogic.auroracontacts.data.api

import com.google.gson.Gson
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Created by sunny on 12.12.2017.
 * mail: mail@sunnydaydev.me
 */

annotation class Json

class JsonFieldFactory(private val gson: Gson): Converter.Factory() {

    override fun stringConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<*, String>? {

        annotations.find { it is Json } ?: return null
        return Converter<Any, String> { gson.toJson(it) }

    }

}