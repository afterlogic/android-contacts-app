package com.afterlogic.auroracontacts.presentation.common

import android.content.Context
import io.reactivex.Observable

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */
interface FragmentTitleProvider {

    fun getFragmentTitle(ctx: Context) : String

}

interface FragmentObservableTitleProvider {

    fun getFragmentTitle(): Observable<String>

}