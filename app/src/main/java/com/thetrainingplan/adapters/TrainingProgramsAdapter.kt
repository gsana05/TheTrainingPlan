package com.thetrainingplan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bnpleasing.BindableAdapter
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.R
import com.thetrainingplan.models.User
import kotlinx.android.synthetic.main.main_recycler_view_item.view.*

class TrainingProgramsAdapter : PagerAdapter(), BindableViewPagerAdapter<User> {

    private var players = emptyList<User>()

    override fun setData(items: List<User>?) {
       players = items ?: emptyList()
       notifyDataSetChanged()
   }


    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return players.size
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(container.context).inflate(R.layout.main_recycler_view_item, container, false)
        val userPlayer = players[position]

        layout.recycler_view_email.text = userPlayer!!.name

        /*Glide.with(layout.view_pager_image).clear(layout.view_pager_image)
        if(userPlayer.profileImageDownloadUrl!=null) {
            Glide.with(layout.view_pager_image).load(userPlayer.profileImageDownloadUrl).into(layout.view_pager_image)
            layout.view_pager_image.scaleType = ImageView.ScaleType.CENTER_CROP
        }
        else{
            layout.view_pager_image.setImageResource(R.drawable.ic_launcher_foreground)
            layout.view_pager_image.scaleType = ImageView.ScaleType.FIT_XY
        }*/

        container.addView(layout)
        return layout

    }
}

