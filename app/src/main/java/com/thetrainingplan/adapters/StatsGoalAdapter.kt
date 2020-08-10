package com.thetrainingplan.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.thetrainingplan.R
import com.thetrainingplan.databinding.GoalsItemBinding
import com.thetrainingplan.databinding.StatsGoalItemBinding
import com.thetrainingplan.models.Goal
import com.thetrainingplan.util.RecyclerViewClickListener

class StatsGoalAdapter(private val goals : ArrayList<Goal>, private val listener: RecyclerViewClickListener) : RecyclerView.Adapter <StatsGoalAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.stats_goal_item,
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

        /*holder.recyclerviewMovieBinding.goalsItemButton.setOnClickListener {
            listener.onRecyclerViewItemClick(holder.recyclerviewMovieBinding.goalsItemButton, goals[position])
        }

        holder.recyclerviewMovieBinding.goalsItemButtonCompleted.setOnClickListener {
            listener.onRecyclerViewItemClick(holder.recyclerviewMovieBinding.goalsItemButtonCompleted, goals[position])
        }

        holder.recyclerviewMovieBinding.goalsItemButtonCompletedReOpen.setOnClickListener {
            listener.onRecyclerViewItemClick(holder.recyclerviewMovieBinding.goalsItemButtonCompletedReOpen, goals[position])
        }

        holder.recyclerviewMovieBinding.goalsItemButtonDeleteGoalPerm.setOnClickListener {
            listener.onRecyclerViewItemClick(holder.recyclerviewMovieBinding.goalsItemButtonDeleteGoalPerm, goals[position])
        }

        holder.recyclerviewMovieBinding.goalsItemButtonShareGoalCompleted.setOnClickListener {
            listener.onRecyclerViewItemClick(holder.recyclerviewMovieBinding.goalsItemButtonShareGoalCompleted, goals[position])
        }

        holder.recyclerviewMovieBinding.goalsItemButtonShareOpenGoal.setOnClickListener {
            listener.onRecyclerViewItemClick(holder.recyclerviewMovieBinding.goalsItemButtonShareOpenGoal, goals[position])
        }*/
    }

    inner class ViewHolder(val recyclerviewMovieBinding: StatsGoalItemBinding) : RecyclerView.ViewHolder(recyclerviewMovieBinding.root)
}