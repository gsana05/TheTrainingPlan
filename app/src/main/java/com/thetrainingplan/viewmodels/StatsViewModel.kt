package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.thetrainingplan.util.LiveEvent

class StatsViewModel(application : Application) : AndroidViewModel(application) {

    val startAllStatsActivityEvent = LiveEvent<Void>()
    val totalNumberOfGoals = MutableLiveData<Int>()
    val openGoals = MutableLiveData<Int>()
    val completedGoals = MutableLiveData<Int>()
    val deletedGoals = MutableLiveData<Int>()
    val totalNumberOfTasks = MutableLiveData<Int>()

    fun startAllStatsActivity(){
        startAllStatsActivityEvent.call()
    }

}