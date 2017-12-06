package com.afterlogic.auroracontacts.data.account

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

class AnotherAcountExistError(user: String): Throwable()

class AccountNotExistError(): Throwable()

class AccountActionError(val action: AccountActionError.Action): Throwable() {
    enum class Action { REMOVING, ADDING }
}