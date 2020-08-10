package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.thetrainingplan.util.LiveEvent

class StatsViewModel(application : Application) : AndroidViewModel(application) {

    val startAllStatsActivityEvent = LiveEvent<Void>()

    fun startAllStatsActivity(){
        startAllStatsActivityEvent.call()
    }

}