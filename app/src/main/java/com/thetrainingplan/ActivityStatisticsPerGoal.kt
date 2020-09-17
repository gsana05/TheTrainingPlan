package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.thetrainingplan.databinding.ActivityStatisticsPerGoalBinding
import com.thetrainingplan.databinding.ActivityStatsBinding
import com.thetrainingplan.viewmodels.StatsViewModel

class ActivityStatisticsPerGoal : AppCompatActivity() {

    private lateinit var viewModel: StatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_statistics_per_goal)

        viewModel = ViewModelProviders.of(this).get(StatsViewModel::class.java)

        val binding: ActivityStatisticsPerGoalBinding = DataBindingUtil.setContentView(this, R.layout.activity_statistics_per_goal)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }
}