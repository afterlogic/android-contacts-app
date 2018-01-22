package com.afterlogic.auroracontacts.presentation.foreground.mainActivity

import android.arch.lifecycle.ViewModel
import com.afterlogic.auroracontacts.presentation.common.FragmentCreator
import com.afterlogic.auroracontacts.presentation.common.base.MVVMInjection
import com.afterlogic.auroracontacts.presentation.common.databinding.ViewModelKey
import com.afterlogic.auroracontacts.presentation.foreground.FragmentScope
import com.afterlogic.auroracontacts.presentation.foreground.about.AboutDIModule
import com.afterlogic.auroracontacts.presentation.foreground.about.AboutFragment
import com.afterlogic.auroracontacts.presentation.foreground.about.LicenceFragment
import com.afterlogic.auroracontacts.presentation.foreground.about.LicencesFragment
import com.afterlogic.auroracontacts.presentation.foreground.login.LoginFragment
import com.afterlogic.auroracontacts.presentation.foreground.login.LoginFragmentModule
import com.afterlogic.auroracontacts.presentation.foreground.main.MainFragment
import com.afterlogic.auroracontacts.presentation.foreground.main.MainFragmentModule
import com.afterlogic.auroracontacts.presentation.foreground.unsuportedApi.UnsupportedApiDIModule
import com.afterlogic.auroracontacts.presentation.foreground.unsuportedApi.UnsupportedApiFragment
import com.afterlogic.auroracontacts.presentation.navigation.AppNavigator
import com.afterlogic.auroracontacts.presentation.navigation.Screens
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import ru.terrakok.cicerone.NavigatorHolder
import javax.inject.Inject

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */


@Module
internal abstract class MainActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindViewModelToMap(vm: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @StringKey(Screens.LOGIN)
    abstract fun bindLoginProvider(f: LoginFragment.Creator): FragmentCreator

    @FragmentScope
    @ContributesAndroidInjector(modules = [LoginFragmentModule::class])
    abstract fun contributeLogin(): LoginFragment

    @Binds
    @IntoMap
    @StringKey(Screens.MAIN)
    abstract fun bindMainProvider(f: MainFragment.Creator): FragmentCreator

    @FragmentScope
    @ContributesAndroidInjector(modules = [MainFragmentModule::class])
    abstract fun contributeMain(): MainFragment

    @Binds
    @IntoMap
    @StringKey(Screens.UNSUPPORTED_API)
    abstract fun bindUnsuportedApiProvider(f: UnsupportedApiFragment.Creator): FragmentCreator

    @FragmentScope
    @ContributesAndroidInjector(modules = [UnsupportedApiDIModule::class])
    abstract fun contributeUnsupportedApi(): UnsupportedApiFragment

    @Binds
    @IntoMap
    @StringKey(Screens.ABOUT)
    abstract fun bindAboutProvider(f: AboutFragment.Creator): FragmentCreator

    @FragmentScope
    @ContributesAndroidInjector(modules = [AboutDIModule::class])
    abstract fun contributeAbout(): AboutFragment

    @Binds
    @IntoMap
    @StringKey(Screens.LICENCES)
    abstract fun bindLicencesProvider(f: LicencesFragment.Creator): FragmentCreator

    @FragmentScope
    @ContributesAndroidInjector(modules = [AboutDIModule::class])
    abstract fun contributeLicences(): LicencesFragment

    @Binds
    @IntoMap
    @StringKey(Screens.LICENCE)
    abstract fun bindLicenceProvider(f: LicenceFragment.Creator): FragmentCreator

    @FragmentScope
    @ContributesAndroidInjector(modules = [AboutDIModule::class])
    abstract fun contributeLicence(): LicenceFragment

}

class MainActivityInjection @Inject constructor(
        val navigationHolder: NavigatorHolder,
        val navigatorFactory: AppNavigator.Factory,
        override val config: MVVMInjection.Config
): MVVMInjection