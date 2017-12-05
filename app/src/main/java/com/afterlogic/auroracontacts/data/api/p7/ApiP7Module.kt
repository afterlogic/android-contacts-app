package com.afterlogic.auroracontacts.data.api.p7

import com.afterlogic.auroracontacts.BuildConfig
import com.afterlogic.auroracontacts.data.api.P7
import com.afterlogic.auroracontacts.data.api.p7.converters.ApiResponseP7Deserializer
import com.afterlogic.auroracontacts.data.api.p7.model.ApiResponseP7
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
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton

/**
 * Created by sashka on 03.02.17.
 *
 *
 * mail: sunnyday.development@gmail.com
 */
@Module
class ApiP7Module {

    @P7
    @Singleton
    @Provides
    internal fun provideClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()

        //Add logging for debug
        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            clientBuilder.addInterceptor(interceptor)
        }

        return clientBuilder
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build()
    }

    @P7
    @Singleton
    @Provides
    internal fun provideGson(): Gson {

        val gson = AtomicReference<Gson>()

        return GsonBuilder()
                .registerTypeAdapter(ApiResponseP7::class.java, ApiResponseP7Deserializer(gson))
                .create()
                .also { gson.set(it) }

    }

    @P7
    @Provides
    internal fun provideRetrofit(gson: Gson, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
    }

    @Provides
    internal fun provideApi(retrofit: Retrofit): AuthApiP7 {
        return retrofit.create(AuthApiP7::class.java)
    }

    @Provides
    internal fun provideAuthApiClass(): Class<AuthApiP7> = AuthApiP7::class.java

    @Provides
    internal fun provideCalendarApiClass(): Class<CalendarApiP7> = CalendarApiP7::class.java

    @Provides
    internal fun provideContactsApiClass(): Class<ContactsApiP7> = ContactsApiP7::class.java

}
