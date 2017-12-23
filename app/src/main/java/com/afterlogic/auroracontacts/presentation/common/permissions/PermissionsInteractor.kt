package com.afterlogic.auroracontacts.presentation.common.permissions

import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import com.afterlogic.auroracontacts.application.ActivityTracker
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.presentation.common.PermissionNotGrantedError
import com.afterlogic.auroracontacts.presentation.common.ViewNotPresentError
import io.reactivex.Flowable
import io.reactivex.Single
import ru.lipka.foodkeeper.presentation.common.modules.interactor.permissions.PermissionResultPublisher
import javax.inject.Inject

/**
 * Created by aleksandrcikin on 04.08.17.
 * mail: mail@sunnydaydev.me
 */

class PermissionsInteractor @Inject
internal constructor(private val publisher: PermissionResultPublisher,
                     private val activityTracker: ActivityTracker,
                     private val context: App) {

    fun requirePermission(request: PermissionRequest): Single<PermissionGrantEvent> {

        return Flowable.defer {

            val perms = request.permissions
            val code = request.requestCode

            if (context.checkSelfPermission(request)) {
                return@defer Flowable.just(PermissionGrantEvent(
                        code, perms,
                        IntArray(request.permissions.size) { PackageManager.PERMISSION_GRANTED }
                ))
            }

            val activity = activityTracker.lastCreatedActivity ?: throw ViewNotPresentError()

            ActivityCompat.requestPermissions(activity, perms, code)

            publisher.listen()

        }//--->
                .filter { event -> event.requestCode == request.requestCode }
                .firstOrError()
                .doOnSuccess { this.checkGranted(it) }

    }

    private fun checkGranted(event: PermissionGrantEvent) {
        if (!event.isAllGranted) {
            throw PermissionNotGrantedError(event)
        }
    }

}
