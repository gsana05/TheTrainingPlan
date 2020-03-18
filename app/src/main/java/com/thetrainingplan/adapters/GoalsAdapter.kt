package com.thetrainingplan.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.thetrainingplan.R
import com.thetrainingplan.databinding.GoalsItemBinding
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.User
import com.thetrainingplan.util.RecyclerViewClickListener

class GoalsAdapter(private val goals : ArrayList<Goal>, private val listener: RecyclerViewClickListener) : RecyclerView.Adapter <GoalsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.goals_item,
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

        holder.recyclerviewMovieBinding.goalsItemButton.setOnClickListener {
            listener.onRecyclerViewItemClick(holder.recyclerviewMovieBinding.goalsItemButton, goals[position])
        }

        /*holder.recyclerviewMovieBinding.goalsItemButtonCompleted.setOnClickListener {
            listener.onRecyclerViewItemClick(holder.recyclerviewMovieBinding.goalsItemButtonCompleted, goals[position])
        }*/
    }

    inner class ViewHolder(val recyclerviewMovieBinding: GoalsItemBinding) : RecyclerView.ViewHolder(recyclerviewMovieBinding.root)
}