package com.thetrainingplan.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.thetrainingplan.R
import com.thetrainingplan.databinding.MainRecyclerViewItemBinding
import com.thetrainingplan.models.AddTask
import com.thetrainingplan.util.RecyclerViewClickListener

class TasksAdaptor(private val users : ArrayList<AddTask>, private val listener: RecyclerViewClickListener) : RecyclerView.Adapter <TasksAdaptor.ViewHolder>(){
    /*override fun setData(items: List<User>?) {
        agreements = items ?: emptyList()
        notifyDataSetChanged()
    }

    private var agreements = emptyList<User>()*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.main_recycler_view_item,
                parent,
                false
            )
        )

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user = users[position]

        holder.recyclerviewMovieBinding.user = user

        holder.recyclerviewMovieBinding.recyclerViewButton.setOnClickListener {
            listener.onRecyclerViewItemClick(holder.recyclerviewMovieBinding.recyclerViewButton, users[position])
        }
    }

    inner class ViewHolder(val recyclerviewMovieBinding: MainRecyclerViewItemBinding) : RecyclerView.ViewHolder(recyclerviewMovieBinding.root)
    //inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

}