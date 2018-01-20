package com.afterlogic.auroracontacts.core.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by sunny on 16.01.2018.
 * mail: mail@sunnydaydev.me
 */

fun String.isDigits() : Boolean = isNotBlank() && all { it.isDigit() }

fun String.isDate(format: String) : Boolean = try {
    SimpleDateFormat(format, Locale.US).parse(format) != null
} catch (e: Throwable) {
    false
}