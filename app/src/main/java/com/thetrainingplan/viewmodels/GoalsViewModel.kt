package com.thetrainingplan.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.thetrainingplan.models.AddTask
import com.thetrainingplan.models.AddTaskModel
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.util.LiveEvent
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class GoalsViewModel(application : Application) : AndroidViewModel(application)  {

    var isChecked = MutableLiveData<Boolean>().apply { value = false }
    var dateOfGoalSet = MutableLiveData<String>()
    private var dateOfGoalSetInMillie = MutableLiveData<Long>()
    var goalSet = MutableLiveData<String>()
    var dateGoalDeadline = MutableLiveData<String>()
    var dateGoalDeadlineInMillie = MutableLiveData<Long>()
    var numberOfDaysToGoal = MutableLiveData<String>()
    private var numberOfDaysToGoalInMillie = MutableLiveData<Long>()
    var spinnerPosition = MutableLiveData<Int>()
    var showAlert = MutableLiveData<Boolean>()
    var isSubmittingGoal = MutableLiveData<Boolean>()
    var isDeletingGoal = MutableLiveData<Boolean>()
    var hasGoalSavedToDatabase = MutableLiveData<Boolean>()
    var saveGoalException = MutableLiveData<Exception>()
    var goal = MutableLiveData<Goal?>()
    val finishUpdateGoalsActivityEvent = LiveEvent<Void>()
    val finishUpdateGoals = LiveEvent<Void>()
    var numberOfOpenGoals = MutableLiveData<Int>()
    val resetSpinnerValues = LiveEvent<Void>()

    init {
        getCurrentDate()
    }

    fun finishUpdateGoalsActivity(){
        finishUpdateGoalsActivityEvent.call()
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

    fun completedGoal(userId: String, goalPin : String, callback : (Boolean?, Exception?) -> Unit){
        GoalModel.updateIsCompletedGoal(userId, goalPin){data : Boolean?, exc : Exception? ->
            if(data != null && data){
                callback(true, null)
            }
            else{
                callback(false, exc)
            }

        }
    }

    fun updateIsDelete(userId: String){
        goal.value?.id?.let {goalPin ->
            isDeletingGoal.value = true
            GoalModel.updateIsDeleteGoal(userId, goalPin){ data : Boolean?, _ : Exception? ->
                isDeletingGoal.value = true
                if(data != null && data){
                    hasGoalSavedToDatabase.value = true
                    finishUpdateGoals.call()
                }
                else{
                    hasGoalSavedToDatabase.value = false
                }
            }
        }
    }

    fun submitGoal(isNew : Boolean){

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

        isSubmittingGoal.value = true


        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){

            var setGoal : Goal? = null
            if(isNew){ // new goal
                // you can only update existing goals thats why isDeleted and isCompleted can be set to null
                setGoal = Goal(null, userId ,dateOfGoalSetInMillie.value, goalSet.value, spinnerPosition.value, dateGoalDeadlineInMillie.value, null, null, null)
            }
            else{ // update goal
                goal.value?.id?.let {goalPin ->
                    // you can only update existing goals thats why isDeleted and isCompleted can be set to null
                    setGoal = Goal(goalPin, userId ,dateOfGoalSetInMillie.value, goalSet.value, spinnerPosition.value, dateGoalDeadlineInMillie.value, null, null, null)
                }

            }


            setGoal?.let {
                GoalModel.addOrUpdateGoal(userId, it, isNew){ data : Boolean?, exception : Exception? ->
                    isSubmittingGoal.value = false

                    if(exception != null){
                        saveGoalException.value = exception
                        hasGoalSavedToDatabase.value = false
                    } else{
                        if(data != null && data){
                            hasGoalSavedToDatabase.value = true
                            if(isNew){
                                switchPressed()
                                goalSet.value = ""
                                dateGoalDeadlineInMillie.value = null
                                dateGoalDeadline.value = null
                                resetSpinnerValues.call()
                            }
                            else{
                                finishUpdateGoals.call()
                            }


                        } else{
                            hasGoalSavedToDatabase.value = false
                            Log.v("", "")
                        }
                    }

                }
            }
        }
        else{
            //user has not log in if this else is fired
            isSubmittingGoal.value = false
        }
    }

    fun reOpenGoal(userId: String, goalPin : String, callback : (Boolean?, Exception?) -> Unit){
        GoalModel.reOpenGoal(userId, goalPin){ data : Boolean?, exc : Exception? ->
            if(data != null && data){
                callback(true, null)
            }
            else{
                callback(false, exc)
            }
        }
    }

    fun permanentlyDeleteGoalPin(userId : String, goalPin : String, callback : (Boolean?, Exception?) -> Unit){
        GoalModel.permanentlyDeleteGoalPin(userId, goalPin){ data : Boolean?, exc : Exception? ->
            if(data != null && data){
                callback(true, null)
            }
            else{
                callback(false, exc)
            }
        }
    }

    fun permanentlyDeleteGoal(userId : String, goalPin : String, callback : (Boolean?, Exception?) -> Unit){
        GoalModel.permanentlyDeleteGoal(userId, goalPin){ data : Boolean?, exc : Exception? ->
            if(data != null && data){
                AddTaskModel.deleteAllTasksInAGoal(userId, goalPin){ data : Boolean?, exc : Exception? ->

                    if(data != null && data){
                        callback(true, null)
                    }
                    else{
                        callback(false, Exception("Goal completed but tasks have not"))
                    }

                }
            }
            else{
                callback(false, exc)
            }
        }
    }



}