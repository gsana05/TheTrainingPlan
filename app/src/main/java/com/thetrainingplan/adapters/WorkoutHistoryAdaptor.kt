package com.thetrainingplan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bnpleasing.BindableAdapter
import com.thetrainingplan.R
import com.thetrainingplan.models.User
import kotlinx.android.synthetic.main.main_recycler_view_item.view.*

class WorkoutHistoryAdaptor (): RecyclerView.Adapter <WorkoutHistoryAdaptor.ViewHolder>(), BindableAdapter<User> {
    override fun setData(items: List<User>?) {
        agreements = items ?: emptyList()
        notifyDataSetChanged()
    }

    private var agreements = emptyList<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutHistoryAdaptor.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.main_recycler_view_item, parent, false))
    }

    override fun getItemCount(): Int {
        return agreements.size
    }

    override fun onBindViewHolder(holder: WorkoutHistoryAdaptor.ViewHolder, position: Int) {
        val user = agreements[position]

        holder.itemView.recycler_view_email.text = user.email


    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

}