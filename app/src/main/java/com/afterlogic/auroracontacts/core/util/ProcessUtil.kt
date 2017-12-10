package com.afterlogic.auroracontacts.core.util

import android.app.ActivityManager
import android.app.Application
import android.content.Context


/**
 * Created by sunny on 10.12.2017.
 * mail: mail@sunnydaydev.me
 */

val Application.processInfo: ActivityManager.RunningAppProcessInfo get() {

    val pid = android.os.Process.myPid()
    val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    return manager.runningAppProcesses.first { it.pid == pid }

}

fun Application.isMainProcess(): Boolean = processInfo.processName == packageName