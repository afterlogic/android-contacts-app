package com.afterlogic.auroracontacts.presentation.foreground.login

import android.databinding.Bindable
import android.os.Handler
import android.os.Looper
import com.afterlogic.auroracontacts.BuildConfig
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.application.wrappers.Resources
import com.afterlogic.auroracontacts.application.wrappers.Toaster
import com.afterlogic.auroracontacts.core.rx.*
import com.afterlogic.auroracontacts.core.util.isAnyNetworkError
import com.afterlogic.auroracontacts.presentation.FragmentScope
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.bindable
import com.afterlogic.auroracontacts.presentation.common.databinding.bindableCommand
import javax.inject.Inject

@FragmentScope
class LoginViewModel @Inject constructor(
        private val interactor: LoginInteractor,
        override val subscriber: Subscriber,
        private val resources: Resources,
        protected val toaster: Toaster
) : ObservableRxViewModel() {

    enum class FocusTarget { DOMAIN, LOGIN, PASSWORD }

    private val uiHandler = Handler(Looper.getMainLooper())

    @get:Bindable
    var focus: FocusTarget? by bindableCommand(handler = uiHandler)

    @get:Bindable
    var domain: String by bindable("") { clearErrors() }

    @get:Bindable
    var domainError: String? by bindable(null, handler = uiHandler)

    @get:Bindable
    var login: String by bindable("") { clearErrors() }

    @get:Bindable
    var loginError: String? by bindable(null, handler = uiHandler)

    @get:Bindable
    var password: String by bindable("") { clearErrors() }

    @get:Bindable
    var passwordError: String? by bindable(null, handler = uiHandler)

    @get:Bindable
    var inProgress: Boolean by bindable(false, handler = uiHandler)

    init {

        if (BuildConfig.DEBUG) {
            domain = "p7.afterlogic.com"
        }

    }

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

        synchronized(this) {
            if (inProgress) return
            else inProgress = true
        }

        interactor.checkHost(domain)
                .shadowError(::CheckHostError)
                .flatMapCompletable { interactor.login(it, login, password) }
                .shadowErrorIfNot(::LoginError)
                .defaultSchedulers()
                .doFinally { inProgress = false }
                .subscribeIt(
                        onComplete = { toaster.showShort("Success") },
                        onError = SuccessErrorHandler(this::handleLoginError)
                )

    }

    private fun handleLoginError(error: Throwable) {

        when(error) {

            is CheckHostError -> {
                domainError = resources.strings[R.string.error_unrechable_host]
                focus = FocusTarget.DOMAIN
            }

            is LoginError -> {

                if (error.cause?.isAnyNetworkError == true) {

                    toaster.showLong(resources.strings[R.string.error_connection_to_domain])

                } else {

                    loginError = resources.strings[R.string.error_pass_or_login]
                    passwordError = resources.strings[R.string.error_pass_or_login]
                    focus = FocusTarget.LOGIN

                }

            }

        }

    }

    private fun clearErrors() {
        domainError = null
        loginError = null
        passwordError = null
    }

    private class CheckHostError(cause: Throwable): ShadowedError(cause)

    private class LoginError(cause: Throwable): ShadowedError(cause)

}