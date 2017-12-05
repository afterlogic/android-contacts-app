package com.afterlogic.auroracontacts.presentation.foreground.login

import android.databinding.Bindable
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.application.wrappers.Resources
import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.presentation.common.base.ObservableViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.bindable
import com.afterlogic.auroracontacts.presentation.common.databinding.bindableCommand
import javax.inject.Inject

class LoginViewModel @Inject constructor(
        private val subscriber: Subscriber,
        private val resources: Resources
) : ObservableViewModel() {

    enum class FocusTarget { DOMAIN, LOGIN, PASSWORD }

    @get:Bindable
    var focus: FocusTarget? by bindableCommand()

    @get:Bindable
    var domain: String by bindable("") { clearErrors() }

    @get:Bindable
    var domainError: String? by bindable(null)

    @get:Bindable
    var login: String by bindable("") { clearErrors() }

    @get:Bindable
    var loginError: String? by bindable(null)

    @get:Bindable
    var password: String by bindable("") { clearErrors() }

    @get:Bindable
    var passwordError: String? by bindable(null)

    @get:Bindable
    var isInProgress: Boolean by bindable(false)

    fun onLogin() {

        if (domain.isBlank()) {
            domainError = resources.strings[R.string.error_field_required]
            focus = FocusTarget.DOMAIN
            return
        }

        if (login.isBlank()) {
            loginError = resources.strings[R.string.error_field_required]
            focus = FocusTarget.LOGIN
            return
        }

        if (password.isBlank()) {
            passwordError = resources.strings[R.string.error_field_required]
            focus = FocusTarget.PASSWORD
            return
        }


    }

    private fun clearErrors() {
        domainError = null
        loginError = null
        passwordError = null
    }

}