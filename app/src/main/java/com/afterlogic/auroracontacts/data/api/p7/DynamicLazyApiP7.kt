package com.afterlogic.auroracontacts.data.api.p7

import com.afterlogic.auroracontacts.data.api.P7
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Singleton
class DynamicLazyApiP7<T: ApiP7> @Inject constructor(
        private val clazz: Class<T>,
        @P7 private val builder: Provider<Retrofit>
) {

    private var currentUrl: String? = null
    private var currentApi: T? = null

    operator fun get(baseUrl: String): T {

        val currentUrl = currentUrl
        val currentApi = currentApi

        return if (currentUrl == baseUrl && currentApi != null) {
            currentApi
        } else {
            builder.get()
                    .newBuilder()
                    .baseUrl(baseUrl)
                    .build()
                    .create(clazz)
                    .also {
                        this.currentUrl = baseUrl
                        this.currentApi = it
                    }
        }

    }

}