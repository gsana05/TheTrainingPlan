package com.thetrainingplan.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.thetrainingplan.R
import com.thetrainingplan.databinding.AddTaskGoalsItemBinding
import com.thetrainingplan.models.Goal
import com.thetrainingplan.util.RecyclerViewClickListener
import kotlinx.android.synthetic.main.add_task_goals_item.view.*


@Suppress("DEPRECATION")
class AddTaskGoalsAdapter(private val goals : ArrayList<Goal>, private val listener: RecyclerViewClickListener) : RecyclerView.Adapter <AddTaskGoalsAdapter.ViewHolder>()  {

    private var selectedItem = 0;
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.add_task_goals_item,
                parent,
                false
            )
        )

    override fun getItemCount(): Int {
        return goals.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]

        holder.recyclerviewMovieBinding.goal = goal

        if(selectedItem == position){
            holder.itemView.background = holder.itemView.context.resources.getDrawable(R.drawable.blue_button)
            val white = holder.itemView.context.resources.getColor(R.color.white)
            holder.itemView.add_task_goals_item_goal.setTextColor(white)
        }
        else{
            holder.itemView.background = holder.itemView.context.resources.getDrawable(R.drawable.white_button_blue_border)
            val blue = holder.itemView.context.resources.getColor(R.color.blue)
            holder.itemView.add_task_goals_item_goal.setTextColor(blue)
        }

        holder.itemView.setOnClickListener{
            listener.onRecyclerViewItemClick(holder.itemView, goals[position])

            val previousItem: Int = selectedItem
            selectedItem = position

            notifyItemChanged(previousItem);
            notifyItemChanged(position);
        }

    }

    inner class ViewHolder(val recyclerviewMovieBinding: AddTaskGoalsItemBinding) : RecyclerView.ViewHolder(recyclerviewMovieBinding.root)


}