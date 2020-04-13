package com.thetrainingplan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.thetrainingplan.databinding.ActivityAddTaskBinding
import com.thetrainingplan.viewmodels.MainViewModel

class ActivityAddTask : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_add_task)
        // reference to view model
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        val binding: ActivityAddTaskBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_task)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.finishAddTaskActivityEvent.observe(this, Observer {
            finish()
        })
    }
}
