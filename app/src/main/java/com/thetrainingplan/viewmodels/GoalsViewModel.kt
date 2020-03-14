package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class GoalsViewModel(application : Application) : AndroidViewModel(application)  {

    var isChecked = MutableLiveData<Boolean>().apply { value = false }
    var dateOfGoalSet = MutableLiveData<String>()
    var dateOfGoalSetInMillie = MutableLiveData<Long>()
    var goalSet = MutableLiveData<String>()
    var dateGoalDeadline = MutableLiveData<String>()
    var dateGoalDeadlineInMillie = MutableLiveData<Long>()
    var numberOfDaysToGoal = MutableLiveData<String>()
    var numberOfDaysToGoalInMillie = MutableLiveData<Long>()
    var spinnerPosition = MutableLiveData<Int>()
    var showAlert = MutableLiveData<Boolean>()

    init {
        getCurrentDate()
    }

    private fun getCurrentDate(){
        val c = Calendar.getInstance()

        c.get(Calendar.YEAR)
        c.get(Calendar.MONTH)
        c.get(Calendar.DAY_OF_MONTH)

        dateOfGoalSetInMillie.value = c.timeInMillis
        dateOfGoalSet.value = SimpleDateFormat.getDateInstance(DateFormat.LONG).format(c.time)
    }

    fun switchPressed(){
        isChecked.value?.let {
            isChecked.value = !it
        }
    }

    fun printDifference(startDate : Date, endDate : Date) {
        //milliseconds
        var different = endDate.time - startDate.time
        numberOfDaysToGoalInMillie.value = different

        val secondsInMilli = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24

        val elapsedDays = different / daysInMilli
        different %= daysInMilli

        val elapsedHours = different / hoursInMilli
        different %= hoursInMilli

        //val elapsedMinutes = different / minutesInMilli
        different %= minutesInMilli

        //val elapsedSeconds = different / secondsInMilli

        numberOfDaysToGoal.value = "$elapsedDays days and $elapsedHours hours"
    }

    fun submitGoal(){

        showAlert.value = false

        if(dateOfGoalSet.value == null){
            showAlert.value = true
            return
        }


        val isGoalSetEmpty = goalSet.value?.isEmpty()

        if((goalSet.value == null) || (isGoalSetEmpty != null && isGoalSetEmpty)){
            showAlert.value = true
            return
        }

        if(dateGoalDeadline.value == null){
            showAlert.value = true
            return
        }

        spinnerPosition.value

    }

}