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

    val startGoalsActivityEvent = LiveEvent<Void>()

    fun startGoalsActivity(){
        startGoalsActivityEvent.call()
    }

    val startEnrollActivityEvent = LiveEvent<Void>()

    fun startEnrollTrainingProgramActivity(){
        startEnrollActivityEvent.call()
    }


}