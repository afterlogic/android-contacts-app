package com.afterlogic.auroracontacts.presentation.background.sync.contacts

import android.accounts.Account
import android.content.ContentProviderClient
import com.afterlogic.auroracontacts.data.contacts.ContactsRepository
import io.reactivex.Completable
import timber.log.Timber
import javax.inject.Inject

class ContactsSyncOperation private constructor(
        private val account: Account,
        private val contentClient: ContentProviderClient,
        private val contactsRepository: ContactsRepository
) {

    fun sync() : Completable = contactsRepository.loadRemote().toCompletable()
            .doOnError(Timber::d)

    class Factory @Inject constructor(
            private val contactsRepository: ContactsRepository
    ) {

        fun create(account: Account, client: ContentProviderClient) : ContactsSyncOperation {
            return ContactsSyncOperation(account, client, contactsRepository)
        }

    }

}