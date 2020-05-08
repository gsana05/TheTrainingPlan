package com.thetrainingplan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.thetrainingplan.R
import com.thetrainingplan.databinding.MainRecyclerViewItemBinding
import com.thetrainingplan.models.AddTask
import com.thetrainingplan.models.AddTaskModel
import com.thetrainingplan.util.RecyclerViewClickListener
import kotlinx.android.synthetic.main.main_recycler_view_item.view.*

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

        val task = users[position]

        val isCompleted = AddTaskModel.checkTaskIsCompleted(task)

        if(isCompleted){
            holder.itemView.recycler_view_tasks_layout.setBackgroundResource(R.drawable.green_border_thick)
            holder.itemView.recycler_view_button.visibility = View.GONE
            holder.itemView.recycler_view_button_tick.visibility = View.VISIBLE
        }
        else{
            holder.itemView.recycler_view_tasks_layout.setBackgroundResource(R.drawable.blue_border_thick)
            holder.itemView.recycler_view_button.visibility = View.VISIBLE
            holder.itemView.recycler_view_button_tick.visibility = View.GONE
        }

        holder.recyclerviewMovieBinding.task = task

        holder.recyclerviewMovieBinding.recyclerViewButton.setOnClickListener {
            listener.onRecyclerViewItemClick(holder.recyclerviewMovieBinding.recyclerViewButton, users[position])
        }
    }

    inner class ViewHolder(val recyclerviewMovieBinding: MainRecyclerViewItemBinding) : RecyclerView.ViewHolder(recyclerviewMovieBinding.root)
    //inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

}