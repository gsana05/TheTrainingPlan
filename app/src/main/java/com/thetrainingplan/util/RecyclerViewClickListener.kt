package com.thetrainingplan.util

import android.view.View
import com.thetrainingplan.models.User

interface RecyclerViewClickListener {
    fun onRecyclerViewItemClick(view: View, user: User)
}