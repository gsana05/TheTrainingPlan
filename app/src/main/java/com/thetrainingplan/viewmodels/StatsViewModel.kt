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
    val openTasks = MutableLiveData<Int>()
    val completedTasks = MutableLiveData<Int>()
    val deletedTasks = MutableLiveData<Int>()

    val totalNumberOfTasksPerGoal = MutableLiveData<Int>().apply { value = 0 }
    val totalNumberOfTasksPerGoalOpenTasks = MutableLiveData<Int>().apply { value = 0 }
    val totalNumberOfTasksPerGoalCompletedTasks = MutableLiveData<Int>().apply { value = 0 }
    val totalNumberOfTasksPerGoalDeletedTasks = MutableLiveData<Int>().apply { value = 0 }

    fun startAllStatsActivity(){
        startAllStatsActivityEvent.call()
    }

}