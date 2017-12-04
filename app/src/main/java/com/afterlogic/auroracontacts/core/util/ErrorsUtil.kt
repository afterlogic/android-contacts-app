package com.afterlogic.auroracontacts.core.util

import retrofit2.HttpException
import java.net.*

/**
 * Created by sunny on 13.11.2017.
 * mail: mail@sunnydaydev.me
 */

val Throwable.isAnyNetworkError: Boolean
    get() {
        return this is SocketTimeoutException ||
                this is SocketException ||
                this is ConnectException ||
                this is UnknownHostException ||
                this is PortUnreachableException ||
                this is ProtocolException ||
                this is HttpException && arrayOf(307, 308).contains(code())
    }
