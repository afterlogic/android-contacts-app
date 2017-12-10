package com.afterlogic.auroracontacts.application

import com.afterlogic.auroracontacts.core.CoreModule
import com.afterlogic.auroracontacts.data.DataModule
import com.afterlogic.auroracontacts.data.loginStateDataController.LoginStateDataController
import com.afterlogic.auroracontacts.presentation.PresentationModule
import com.afterlogic.auroracontacts.presentation.navigation.AppRouter
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Scope

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Scope
annotation class AppScope

@AppScope
@Component(modules = [
    NavigationModule::class,
    CoreModule::class,
    DataModule::class,
    PresentationModule::class
])
interface AppComponent : AndroidInjector<App> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()

}

@Module
class NavigationModule {

    @AppScope
    @Provides
    fun provideCicerone(): Cicerone<AppRouter> {
        return Cicerone.create(AppRouter())
    }

    @Provides
    fun provideRouter(cicerone: Cicerone<AppRouter>): AppRouter {
        return cicerone.router
    }

    @Provides
    fun provideNavigationHolder(cicerone: Cicerone<AppRouter>): NavigatorHolder {
        return cicerone.navigatorHolder
    }

}

data class AppInjection @Inject constructor(
        val activityTracker: Provider<ActivityTracker>,
        val loginStateDataController: Provider<LoginStateDataController>
)