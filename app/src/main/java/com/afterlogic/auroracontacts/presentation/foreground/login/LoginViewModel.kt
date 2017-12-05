package com.afterlogic.auroracontacts.presentation.foreground.login

import android.databinding.Bindable
import com.afterlogic.auroracontacts.presentation.common.base.ObservableViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.bindable
import com.afterlogic.auroracontacts.presentation.common.databinding.bindableCommand
import javax.inject.Inject

class LoginViewModel @Inject constructor() : ObservableViewModel() {

    enum class FocusTarget { DOMAIN, LOGIN, PASSWORD }

    @get:Bindable
    var focus: FocusTarget? by bindableCommand()

    @get:Bindable
    var domain: String by bindable("")

    @get:Bindable
    var domainError: String? by bindable(null)

    @get:Bindable
    var login: String by bindable("")

    @get:Bindable
    var loginError: String? by bindable(null)

    @get:Bindable
    var password: String by bindable("")

    @get:Bindable
    var passwordError: String? by bindable(null)

    fun onLogin() {

    }

}