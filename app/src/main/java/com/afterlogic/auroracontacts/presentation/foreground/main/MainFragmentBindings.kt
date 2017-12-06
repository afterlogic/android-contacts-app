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
    @BindingAdapter("main_contentHeader", "main_cards")
    fun bindContent(list: RecyclerView, vm: MainViewModel, cards: List<CardViewModel>) {

        val items =  listOf(vm) + cards

        LastAdapter(items, BR.vm)
                .type { _, i ->
                    if (i == 0) Type<MainListHeaderBinding>(R.layout.main_list_header)
                    else Type<MainListCardBinding>(R.layout.main_list_card)
                }
                .into(list)

    }

    @JvmStatic
    @BindingAdapter("main_card_items")
    fun bindCardContent(list: RecyclerView, items: List<CardItemViewModel>) {

        LastAdapter(items, BR.vm)
                .map<CardItemViewModel>(R.layout.main_list_card_item)
                .into(list)

    }

}