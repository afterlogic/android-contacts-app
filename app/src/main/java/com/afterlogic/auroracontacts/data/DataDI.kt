package com.afterlogic.auroracontacts.data

import com.afterlogic.auroracontacts.data.p7.api.ApiP7Module
import dagger.Module

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Module(includes = [
    ApiP7Module::class
])
class DataModule