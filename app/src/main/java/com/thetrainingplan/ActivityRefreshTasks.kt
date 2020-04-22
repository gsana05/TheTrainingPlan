package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_refresh_tasks.*
import java.util.*

class ActivityRefreshTasks : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refresh_tasks)

        button_test.setOnClickListener {
            button_test.text = Calendar.getInstance().time.toString()
            finish()
        }

    }
}
