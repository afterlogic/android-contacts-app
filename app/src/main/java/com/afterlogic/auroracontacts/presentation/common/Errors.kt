package com.afterlogic.auroracontacts.presentation.common

import com.afterlogic.auroracontacts.presentation.common.permissions.PermissionGrantEvent

/**
 * Created by sunny on 09.12.2017.
 * mail: mail@sunnydaydev.me
 */

class ViewNotPresentError: Throwable()

class PermissionNotGrantedError(event: PermissionGrantEvent): Throwable()