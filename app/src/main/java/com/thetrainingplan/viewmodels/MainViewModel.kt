package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.thetrainingplan.models.AddTaskModel
import com.thetrainingplan.models.User
import com.thetrainingplan.util.LiveEvent

class MainViewModel(application : Application) : AndroidViewModel(application) {

    init {

    }

    //val listOfUser =  MutableLiveData<ArrayList<User?>>()
    val currentUser = MutableLiveData<User>()
    val numberOfOpenGoals = MutableLiveData<Int>()
    val numberOfTodayTasks = MutableLiveData<Int>()
    val numberOfCompletedTodayTasks = MutableLiveData<Int>()
    val startGoalsActivityEvent = LiveEvent<Void>()
    val startEnrollActivityEvent = LiveEvent<Void>()
    val startReadGoalsActivityEvent = LiveEvent<Void>()
    val startAddTaskActivityEvent = LiveEvent<Void>()
    val finishAddTaskActivityEvent = LiveEvent<Void>()

    val ADD_TASK_NEVER = MutableLiveData<Int>().apply { value = AddTaskModel.NEVER }
    val ADD_TASK_DAILY = MutableLiveData<Int>().apply { value = AddTaskModel.DAILY }
    val ADD_TASK_WEEKLY = MutableLiveData<Int>().apply { value = AddTaskModel.WEEKLY }
    val ADD_TASK_MONTHLY = MutableLiveData<Int>().apply { value = AddTaskModel.MONTHLY }
    val ADD_TASK_ANNUALLY = MutableLiveData<Int>().apply { value = AddTaskModel.ANNUALLY }

    val taskStartDate = MutableLiveData<String>()
    val taskEndDate = MutableLiveData<String>()

    val addTaskState = MutableLiveData<Int>().apply { value = ADD_TASK_NEVER.value }

    var goalSelectedAddTask = MutableLiveData<String>()

    var isTaskCompleted = MutableLiveData<Boolean>().apply { value = false }

    fun setGoalSelected(id : String){
        //goalSelectedAddTask.value = id
    }


    fun setState(state : Int){
        addTaskState.value = state
    }

    val startStatsActivityEvent = LiveEvent<Void>()
    fun startStatsActivity(){
        startStatsActivityEvent.call()
    }

    val startDiaryActivityEvent = LiveEvent<Void>()
    fun startDiaryActivity(){
        startDiaryActivityEvent.call()
    }


    /*var filteredAgreements: LiveData<List<User?>>? = Transformations.switchMap(listOfUser) { users  ->
        getFilteredList(users)
    }*/

    /*private fun getFilteredList(list : ArrayList<User?>) : LiveData<List<User?>>{
        return Transformations.map(listOfUser){ listOfAgreements ->
            val oo = listOfAgreements.toList()
            applyFilters(oo)
        }
    }*/

    /*private fun applyFilters(listOfAgreements : List<User?>) : List<User?>{

        return listOfAgreements
        *//*return listOfAgreements.filter {agreement ->

            agreement!!.name == "Gareth Sanashee"

        }*//*
    }*/

    fun startGoalsActivity(){
        startGoalsActivityEvent.call()
    }

    fun startReadGoalsActivity() {
        startReadGoalsActivityEvent.call()
    }

    fun startAddTaskActivity(){
        startAddTaskActivityEvent.call()
    }

    fun finishAddTaskActivity(){
        finishAddTaskActivityEvent.call()
    }

    fun startEnrollTrainingProgramActivity(){
        startEnrollActivityEvent.call()
    }


}