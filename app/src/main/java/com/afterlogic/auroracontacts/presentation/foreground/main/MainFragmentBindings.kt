package com.afterlogic.auroracontacts.presentation.foreground.main

import android.databinding.BindingAdapter
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.databinding.adapters.ListenerUtil
import android.support.annotation.IdRes
import android.support.v7.widget.RecyclerView
import android.view.View
import com.afterlogic.auroracontacts.BR
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.core.util.setSequentiallyFrom
import com.github.nitrico.lastadapter.LastAdapter
import com.github.nitrico.lastadapter.StableId

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */
object MainFragmentBindings: Bindings() {

    open class StableIdItem(override val stableId: Long) : StableId
    open class StableIdWrapper<T>(val vm: T, stableId: Long) : StableIdItem(stableId)

    private class HeaderWrapper(vm: MainViewModel): StableIdWrapper<MainViewModel>(vm, Long.MAX_VALUE - 1000)
    private class ProgressWrapper: StableIdItem(Long.MAX_VALUE - 1001)

    @JvmStatic
    @BindingAdapter("main_contentHeader", "main_cards", "main_progress")
    fun bindContent(list: RecyclerView, vm: MainViewModel, cards: List<CardViewModel<Any>>, progress: Boolean) {

        val observableList = list.track(R.id.binding__recycler_observable_list) {

            ObservableArrayList<StableId>().also {
                createMainAdapter(it).into(list)
            }

        }

        val vmWrapper = HeaderWrapper(vm)

        val progressWrapper = { ProgressWrapper() }

        val items = listOf<StableId>(vmWrapper) + if (progress) listOf(progressWrapper()) else cards

        observableList.setSequentiallyFrom(items)

    }

    private fun createMainAdapter(items: List<StableId>): LastAdapter {

        return LastAdapter(items, BR.vm, true)
                .map<HeaderWrapper>(R.layout.main_list_header)
                .map<ProgressWrapper>(R.layout.main_list_progress)
                .map<CardViewModel<*>>(R.layout.main_list_card)

    }

    @JvmStatic
    @BindingAdapter("main_card_items")
    fun bindCardContent(list: RecyclerView, items: List<StableId>) {

        val observableItems: ObservableList<StableId> = items as? ObservableList<StableId> ?:
                list.track(R.id.binding__recycler_observable_list) {
                    ObservableArrayList<StableId>()
                }

        if (observableItems === items) {
            ListenerUtil.trackListener(list, null, R.id.binding__recycler_observable_list)
        }

        fun setAdapter(it: ObservableList<StableId>) {

            list.itemAnimator = null

            LastAdapter(it, BR.vm, true)
                    .map<ContactItemViewModel>(R.layout.main_list_card_item)
                    .map<CalendarItemViewModel>(R.layout.main_list_card_item_colorized)
                    .into(list)

        }

        val adapterItems = list.track(
                R.id.binding__recycler_observable_adapter,
                check = { it === observableItems },
                creator = { observableItems.also { setAdapter(it) } }
        )

        adapterItems.setSequentiallyFrom(items)

    }

}

open class Bindings {

    fun <T> View.track(@IdRes resourceId: Int, check: (T) -> Boolean = { true }, creator: () -> T): T {
        return ListenerUtil.getListener<T>(this, resourceId)?.takeIf(check) ?:
                creator().also { ListenerUtil.trackListener(this, it, resourceId) }
    }

}