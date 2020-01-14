package com.thetrainingplan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bnpleasing.BindableAdapter
import com.thetrainingplan.R
import com.thetrainingplan.databinding.MainRecyclerViewItemBinding
import com.thetrainingplan.models.User
import com.thetrainingplan.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.main_recycler_view_item.view.*

class WorkoutHistoryAdaptor(val users : ArrayList<User>) : RecyclerView.Adapter <WorkoutHistoryAdaptor.ViewHolder>(){
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

    /*override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.main_recycler_view_item, parent, false))
    }*/

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.recyclerviewMovieBinding.user = users[position]
        /*val user = users[position]

        holder.itemView.recycler_view_email.text = user.name

        holder.recyclerviewMovieBinding.movie = movies[position]
        holder.recyclerviewMovieBinding.buttonBook.setOnClickListener {
            //listener.onRecyclerViewItemClick(holder.recyclerviewMovieBinding.buttonBook, movies[position])
        }
        holder.recyclerviewMovieBinding.layoutLike.setOnClickListener {
            //listener.onRecyclerViewItemClick(holder.recyclerviewMovieBinding.layoutLike, movies[position])
        }*/


    }

    inner class ViewHolder(val recyclerviewMovieBinding: MainRecyclerViewItemBinding) : RecyclerView.ViewHolder(recyclerviewMovieBinding.root)
    //inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

}