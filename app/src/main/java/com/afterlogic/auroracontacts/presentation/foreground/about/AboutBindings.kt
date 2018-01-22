package com.afterlogic.auroracontacts.presentation.foreground.about

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import com.afterlogic.auroracontacts.BR
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.presentation.foreground.main.Bindings
import com.github.nitrico.lastadapter.LastAdapter

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */

object AboutBindings: Bindings() {

    @JvmStatic
    @BindingAdapter("licences_items")
    fun bindItems(list: RecyclerView, items: List<LicenseItemViewModel>) {

        list.track(R.id.binding__recycler_observable_list, { it === items }) {

            items.also {

                LastAdapter(items, BR.vm)
                        .map<LicenseItemViewModel>(R.layout.licences_list_item)
                        .into(list)

            }

        }

    }

}