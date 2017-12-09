package com.afterlogic.auroracontacts.data.p7.api

import com.afterlogic.auroracontacts.BuildConfig
import com.afterlogic.auroracontacts.core.gson.registerTypeAdapter
import com.afterlogic.auroracontacts.data.api.P7
import com.afterlogic.auroracontacts.data.api.p7.converters.ApiResponseP7Deserializer
import com.afterlogic.auroracontacts.data.api.p7.util.AuthConverterFactoryP7
import com.afterlogic.auroracontacts.application.AppScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by sashka on 03.02.17.
 *
 *
 * mail: sunnyday.development@gmail.com
 */
@Module
class ApiP7Module {

    @P7
    @AppScope
    @Provides
    internal fun provideClient(): OkHttpClient {

        return OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .apply {

                    //Add logging for debug
                    if (BuildConfig.DEBUG) {
                        val interceptor = HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                        addInterceptor(interceptor)
                    }

                }
                .build()

    }

    @P7
    @AppScope
    @Provides
    internal fun provideGson(): Gson {

        fun builder(): GsonBuilder = GsonBuilder()

        return builder()
                .registerTypeAdapter(ApiResponseP7Deserializer(builder().create()))
                .create()

    }

    @P7
    @Provides
    internal fun provideRetrofit(@P7 gson: Gson,
                                 @P7 client: OkHttpClient,
                                 authConverterFactory: AuthConverterFactoryP7): Retrofit.Builder {

        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(authConverterFactory)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)

    }

    @Provides
    internal fun provideAuthApiClass(): Class<AuthApiP7> = AuthApiP7::class.java

    @Provides
    internal fun provideCalendarApiClass(): Class<CalendarApiP7> = CalendarApiP7::class.java

    @Provides
    internal fun provideContactsApiClass(): Class<ContactsApiP7> = ContactsApiP7::class.java

}
