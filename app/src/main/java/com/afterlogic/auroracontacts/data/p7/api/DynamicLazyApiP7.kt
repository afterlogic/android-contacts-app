package com.afterlogic.auroracontacts.data.p7.api

import com.afterlogic.auroracontacts.data.api.P7
import com.afterlogic.auroracontacts.application.AppScope
import okhttp3.HttpUrl
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Provider

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */

@AppScope
class DynamicLazyApiP7<Api> @Inject constructor(
        private val clazz: Class<Api>,
        @P7 private val builder: Provider<Retrofit.Builder>
) {

    private var currentUrl: HttpUrl? = null
    private var currentApi: Api? = null

    operator fun get(baseUrl: HttpUrl): Api {

        val currentUrl = currentUrl
        val currentApi = currentApi

        return if (currentUrl == baseUrl && currentApi != null) {

            currentApi

        } else {

            builder.get()
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