package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.TextView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class GoalsViewModel(application : Application) : AndroidViewModel(application)  {

    var isChecked = MutableLiveData<Boolean>().apply { value = false }
    var dateOfGoalSet = MutableLiveData<String>()

    init {
        getCurrentDate()
    }

    private fun getCurrentDate(){
        val c = Calendar.getInstance()

        c.get(Calendar.YEAR)
        c.get(Calendar.MONTH)
        c.get(Calendar.DAY_OF_MONTH)

        dateOfGoalSet.value = SimpleDateFormat.getDateInstance(DateFormat.LONG).format(c.time)
    }

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