package com.thetrainingplan.adapters

import androidx.databinding.BindingAdapter
import androidx.viewpager.widget.ViewPager

interface BindableViewPagerAdapter<T> {
    fun setData(items: List<T>?)
}

@BindingAdapter("dataViewPager")
fun <T> setViewPagerProperties(viewPager: ViewPager, items: List<T>?) {
    if (viewPager.adapter is BindableViewPagerAdapter<*>) {
        @Suppress("UNCHECKED_CAST")
        (viewPager.adapter as BindableViewPagerAdapter<T>).setData(items)
    }
}
