package com.thetrainingplan.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception
import java.util.HashMap

object AddTaskModel {

    const val NEVER = 1
    const val DAILY = 2
    const val WEEKLY = 3
    const val MONTHLY = 4
    const val ANNUALLY = 5

    fun addTask(userId: String, goalId: String, addTask: AddTask, onComplete : (data : Boolean?, exc : Exception?) -> Unit){
        firebaseRefAddTask(userId, goalId).add(toMap(addTask)).addOnCompleteListener {task ->

            if(task.isSuccessful){
                onComplete(true, null)
            }
            else{
                onComplete(false, task.exception)
            }

        }
    }

    fun firebaseRefAddTask(userId : String, goalId : String) : CollectionReference {
        return FirebaseFirestore.getInstance().collection("goals").document(userId).collection("goalPins").document(goalId).collection("goalTasks")
    }

    fun toMap(task : AddTask): HashMap<String, Any?> {
        val map: HashMap<String, Any?> = HashMap()
        map["id"] = task.id
        map["requestUserId"] = task.requestUserId
        map["doneDates"] = task.doneDates
        map["name"]=task.name
        map["description"]=task.description
        map["startDate"]= Timestamp(task.startDate)
        if(task.endDate!=null) {
            map["endDate"] = Timestamp(task.endDate!!)
        }
        map["repeatType"]=task.repeatType
        map["repeatEvery"]=task.repeatEvery
        map["repeatWeekdays"]=task.repeatWeekdays
        map["goalId"]=task.goalId


        var mappedDeletedDates = ArrayList<Timestamp>()
        task.deletedDates?.let {
            for (deletedDate in it) {
                mappedDeletedDates.add(Timestamp(deletedDate))
            }
        }
        map["deletedDates"] = mappedDeletedDates
        return map
    }

}