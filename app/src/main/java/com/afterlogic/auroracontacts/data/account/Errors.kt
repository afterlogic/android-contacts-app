package com.afterlogic.auroracontacts.data.account

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

class AnotherAccountExistError(val user: String, domain: String): Throwable()

class AccountNotExistError: Throwable()

class AccountActionError(val action: AccountActionError.Action): Throwable() {
    enum class Action { REMOVING, ADDING }
}

class AuroraAccountSessionParseError(message: String? = null): Throwable(message)