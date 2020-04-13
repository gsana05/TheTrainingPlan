package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.thetrainingplan.models.User
import com.thetrainingplan.util.LiveEvent

class MainViewModel(application : Application) : AndroidViewModel(application) {

    init {

    }

    //val listOfUser =  MutableLiveData<ArrayList<User?>>()
    val currentUser = MutableLiveData<User>()
    val numberOfOpenGoals = MutableLiveData<Int>()
    val startGoalsActivityEvent = LiveEvent<Void>()
    val startEnrollActivityEvent = LiveEvent<Void>()
    val startReadGoalsActivityEvent = LiveEvent<Void>()
    val startAddTaskActivityEvent = LiveEvent<Void>()
    val finishAddTaskActivityEvent = LiveEvent<Void>()



    var goalSelectedAddTask = MutableLiveData<String>().apply { value = "84fd28e1-36e3-404b-b201-a2736a88eb33" }

    fun setGoalSelected(id : String){
        goalSelectedAddTask.value = id
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