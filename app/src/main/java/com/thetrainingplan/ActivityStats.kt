package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.thetrainingplan.databinding.ActivityStatsBinding
import com.thetrainingplan.viewmodels.StatsViewModel

class ActivityStats : AppCompatActivity() {

    private lateinit var viewModel: StatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_stats)

        viewModel = ViewModelProviders.of(this).get(StatsViewModel::class.java)

        val binding: ActivityStatsBinding = DataBindingUtil.setContentView(this, R.layout.activity_stats)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

    }

    private fun convertLongToTime(timeCompletion : Long){
        timeCompletion.let {
            val longVal: Long = it
            val hourss = longVal.toInt() / 3600
            val ioi = hourss
            var remainder = longVal.toInt() - hourss * 3600
            val mins = remainder / 60
            remainder = remainder - mins * 60
            val secs = remainder

            val ints = intArrayOf(hourss, mins, secs)
        }
    }
}
