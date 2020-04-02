package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.thetrainingplan.models.GoalModel

class ReadGoalsViewModel(application : Application) : AndroidViewModel(application)  {

    fun completedGoal(goalPin : String){
        GoalModel.updateIsCompletedGoal(goalPin){ data : Boolean?, exc : Exception? ->
            if(data != null && data){

            }
            else{

            }

        }
    }

}