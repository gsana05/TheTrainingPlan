package com.thetrainingplan

import android.annotation.SuppressLint
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.app.ActivityManager
import android.content.Context
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView


open class NkaActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val FilterShopActivity = "com.nkatech.ActivityFilterShop2"
    }


    fun setupMenu(parentLayout: DrawerLayout, navView: NavigationView, navBar: Toolbar){
        val toggle = ActionBarDrawerToggle(
            this, parentLayout, navBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        parentLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        /*val headerView = navView.getHeaderView(0)

        headerView.side_bar_settings_text.setOnClickListener {
            //startActivity(Intent(this,ActivityContact::class.java))
        }*/
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

        }
        /*drawer_layout.closeDrawer(GravityCompat.START)*/
        return true
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()

        /*val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val taskInfo = am.getRunningTasks(1)
        val currentActivity = taskInfo[0].topActivity?.className

        val filter = findViewById<TextView>(R.id.title_bar_filter)*/


        /*if(currentActivity.equals(FilterShopActivity)){
            filter?.let{
                it.visibility = View.GONE
            }
        }*/

        /*filter?.let{
            it.setOnClickListener {
                //startActivity(Intent(this, ActivityFilterShop2::class.java))
            }
        }*/

        findViewById<TextView>(R.id.title_bar_change)?.let {
            it.setOnClickListener {
                //startActivity(Intent(this, ActivityLocation::class.java))
            }
        }
    }
}