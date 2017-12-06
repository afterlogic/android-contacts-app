package com.afterlogic.auroracontacts.presentation.background.authenticator

import android.accounts.*
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.presentation.foreground.mainActivity.MainActivity

/**
 * Created by sashka on 05.04.16.
 * mail: sunnyday.development@gmail.com
 */
internal class Authenticator// Simple constructor
(private val context: Context) : AbstractAccountAuthenticator(context) {

    // Editing properties is not supported
    override fun editProperties(
            r: AccountAuthenticatorResponse, s: String): Bundle {
        throw UnsupportedOperationException()
    }

    // Don't add additional accounts
    @Throws(NetworkErrorException::class)
    override fun addAccount(
            r: AccountAuthenticatorResponse,
            s: String,
            s2: String,
            strings: Array<String>,
            bundle: Bundle): Bundle {

        val am = AccountManager.get(context)
        val accounts = am.getAccountsByType(AccountService.ACCOUNT_TYPE)

        val result = Bundle()

        if (accounts.isNotEmpty()) {

            val handler = Handler(Looper.getMainLooper())

            handler.post {
                Toast.makeText(context,
                        R.string.error_add_more_than_one_account,
                        Toast.LENGTH_LONG).show()
            }

            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)

        } else {

            val intent = MainActivity.intent()

            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, r)

            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true)
            result.putParcelable(AccountManager.KEY_INTENT, intent)

        }

        return result
    }

    // Ignore attempts to confirm credentials
    @Throws(NetworkErrorException::class)
    override fun confirmCredentials(
            r: AccountAuthenticatorResponse,
            account: Account,
            bundle: Bundle): Bundle? {
        return null
    }

    // Getting an authentication token is not supported
    @Throws(NetworkErrorException::class)
    override fun getAuthToken(
            r: AccountAuthenticatorResponse,
            account: Account,
            s: String,
            bundle: Bundle): Bundle {
        throw UnsupportedOperationException()
    }

    // Getting a label for the auth token is not supported
    override fun getAuthTokenLabel(s: String): String {
        throw UnsupportedOperationException()
    }

    // Updating user credentials is not supported
    @Throws(NetworkErrorException::class)
    override fun updateCredentials(
            r: AccountAuthenticatorResponse,
            account: Account,
            s: String, bundle: Bundle): Bundle {
        throw UnsupportedOperationException()
    }

    // Checking features for the account is not supported
    @Throws(NetworkErrorException::class)
    override fun hasFeatures(
            r: AccountAuthenticatorResponse,
            account: Account, strings: Array<String>): Bundle {
        throw UnsupportedOperationException()
    }

}