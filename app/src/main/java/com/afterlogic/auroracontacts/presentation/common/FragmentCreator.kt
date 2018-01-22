package com.afterlogic.auroracontacts.presentation.common

import android.support.v4.app.Fragment

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */

interface FragmentCreator {

    fun create(seed: Any?): Fragment

}