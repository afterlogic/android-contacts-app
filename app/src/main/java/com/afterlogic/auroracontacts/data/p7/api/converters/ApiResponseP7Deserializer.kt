package com.afterlogic.auroracontacts.data.p7.api.converters

import com.afterlogic.auroracontacts.data.p7.api.model.P7ApiResponse
import com.google.gson.*
import java.lang.reflect.Type

/**
 * Created by sashka on 17.10.16.
 *
 *
 * mail: sunnyday.development@gmail.com
 */
class ApiResponseP7Deserializer(private val gson: Gson) : JsonDeserializer<P7ApiResponse<*>> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): P7ApiResponse<*> {

        val response = json.asJsonObject

        checkAndHandleError(response)

        checkAndHandleFalseResult(response)

        if (response.has(P7ApiResponse.NAME_ERROR_CODE)) {
            response.remove(P7ApiResponse.NAME_RESULT)
        }

        return gson.fromJson(response, typeOfT)

    }

    private fun checkAndHandleError(response: JsonObject) {
        if (response.has("Error")) {
            val error = response.get("Error")
            if (error.isJsonPrimitive && error.asJsonPrimitive.isString) {
                response.addProperty(P7ApiResponse.NAME_ERROR_CODE, 999)
                response.addProperty(P7ApiResponse.NAME_ERROR_MESSAGE, "Error: " + error.asString)
            } else if (error.isJsonPrimitive && error.asJsonPrimitive.isNumber) {
                response.addProperty(P7ApiResponse.NAME_ERROR_CODE, error.asInt)
            } else {
                response.addProperty(P7ApiResponse.NAME_ERROR_CODE, 999)
            }

            response.remove("Error")
        }
    }

    private fun checkAndHandleFalseResult(response: JsonObject) {
        if (response.has("Result")) {
            val result = response.get("Result")
            if (isBoolean(result) && !result.asBoolean && !response.has(P7ApiResponse.NAME_ERROR_CODE)) {
                response.addProperty(P7ApiResponse.NAME_ERROR_CODE, 999)
            }
        }
    }

    private fun isBoolean(element: JsonElement): Boolean {
        return element.isJsonPrimitive && element.asJsonPrimitive.isBoolean
    }

}
