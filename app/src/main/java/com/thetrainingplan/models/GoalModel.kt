package com.thetrainingplan.models

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.HashMap

object GoalModel {

    fun addGoal(userId : String, goal : Goal, callback : (Boolean?, Exception?) -> Unit){

        if(goal.id == null){
            goal.id = UUID.randomUUID().toString()
        }

        goal.id?.let {goalId ->
            getDatabaseRefGoals().document(goalId).set(toMap(goal)).addOnCompleteListener {
                if(it.isSuccessful){
                    //call a firebase function to add goal id to the users list
                    goal.id?.let {idGoal ->
                        UserModel.updateGoals(userId, idGoal, callback)
                    }
                }
                else{
                    callback(false, it.exception)
                }
            }
        }


    }


    private fun toMap(goal : Goal):HashMap<String, Any?>{
        val map:HashMap<String, Any?> = HashMap()
        map["id"]=goal.id
        map["userId"]=goal.userId
        map["goalSetDate"]=goal.goalSetDate
        map["goal"]=goal.goal
        map["goalType"]=goal.goalType
        map["goalDateDeadline"]=goal.goalDateDeadline
        return map
    }

    private fun getDatabaseRefUsers(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("users")
    }

    private fun getDatabaseRefGoals(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("goals")
    }
}