package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main_navigation.*
import kotlinx.android.synthetic.main.app_bar_activity_navigation.*

class MainActivityNavigation : NkaActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_navigation)

        //copy this to other activities
        setSupportActionBar(shop_navigation_toolbar)

        // hiding the label in title bar
        getSupportActionBar()?.setDisplayShowTitleEnabled(false)

        setupMenu(shop_drawer_layout, shop_nav_view, shop_navigation_toolbar)
    }
}
