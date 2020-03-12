package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



class GoalsViewModel(application : Application) : AndroidViewModel(application)  {

    var isChecked = MutableLiveData<Boolean>().apply { value = false }

    fun switchPressed(){
        isChecked.value?.let {
            if(it){
                isChecked.value = false
            }
            else{
                isChecked.value = true
            }
        }
    }

}