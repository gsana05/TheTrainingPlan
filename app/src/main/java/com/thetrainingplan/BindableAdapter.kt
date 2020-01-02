package com.bnpleasing

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

interface BindableAdapter<T> {
    fun setData(items: List<T>?)
}

@BindingAdapter("data")
fun <T> setRecyclerViewProperties(recyclerView: RecyclerView, items: List<T>?) {
    if (recyclerView.adapter is BindableAdapter<*>) {
        @Suppress("UNCHECKED_CAST")
        (recyclerView.adapter as BindableAdapter<T>).setData(items)
    }
}
