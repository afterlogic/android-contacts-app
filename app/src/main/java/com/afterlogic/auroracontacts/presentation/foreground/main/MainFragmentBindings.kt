package com.afterlogic.auroracontacts.presentation.foreground.main

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import com.afterlogic.auroracontacts.BR
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.databinding.MainListCardBinding
import com.afterlogic.auroracontacts.databinding.MainListHeaderBinding
import com.github.nitrico.lastadapter.LastAdapter
import com.github.nitrico.lastadapter.Type

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */
object MainFragmentBindings {

    @JvmStatic
    @BindingAdapter("main_contentHeader", "main_cards", "main_progress")
    fun bindContent(list: RecyclerView, vm: MainViewModel, cards: List<CardViewModel>, progress: Boolean) {

        listOf(vm)
                .let {
                    if (progress) it + progress
                    else it + cards
                }
                .let { LastAdapter(it, BR.vm) }
                .type { _, i ->
                    when {
                        i == 0 -> Type<MainListHeaderBinding>(R.layout.main_list_header)
                        progress -> Type<MainListHeaderBinding>(R.layout.main_list_progress)
                        else -> Type<MainListCardBinding>(R.layout.main_list_card)
                    }
                }
                .into(list)

    }

    @JvmStatic
    @BindingAdapter("main_card_items")
    fun bindCardContent(list: RecyclerView, items: List<ContactItemViewModel>) {

        LastAdapter(items, BR.vm)
                .map<ContactItemViewModel>(R.layout.main_list_card_item)
                .map<CalendarItemViewModel>(R.layout.main_list_card_item_colorized)
                .into(list)

    }

}