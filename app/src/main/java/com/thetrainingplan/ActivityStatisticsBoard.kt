package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.thetrainingplan.databinding.ActivityStatisticsBoardBinding
import com.thetrainingplan.databinding.ActivityStatsBinding
import com.thetrainingplan.viewmodels.StatsViewModel

class ActivityStatisticsBoard : AppCompatActivity() {

    private lateinit var viewModel: StatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_statistics_board)

        viewModel = ViewModelProviders.of(this).get(StatsViewModel::class.java)

        val binding: ActivityStatisticsBoardBinding = DataBindingUtil.setContentView(this, R.layout.activity_statistics_board)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

    }
}