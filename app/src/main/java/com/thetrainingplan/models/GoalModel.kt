package com.thetrainingplan.models

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object GoalModel {

    private var mCachedGoals : HashMap<String, Goal> = HashMap() // these are the cached profiles
    private var mFirebaseRefsGoals : HashMap<String, ListenerRegistration> = HashMap() // these are our watches on the database
    private var mProfileCallbacksGoals = HashMap<String, ArrayList<(Goal?, Exception?) -> Unit>>() // callbacks for that user id

   /* fun numberOfGoalsListeners() : Int? {
        return mFirebaseRefsGoals.size
    }*/

    fun addGoalSingleListener(pin : String, callback : (Goal?, Exception?) -> Unit){

        var callbacks = ArrayList<(Goal?, Exception?) -> Unit>()

        if(mProfileCallbacksGoals.containsKey(pin)){
            mProfileCallbacksGoals[pin]?.let {
                callbacks = it
            }
        }
        else{
            callbacks = ArrayList()
        }

        if(!callbacks.contains(callback)){
            callbacks.add(callback)
            mProfileCallbacksGoals[pin] = callbacks
        }

        if(mCachedGoals.containsKey(pin)){
            val goal = mCachedGoals[pin]
            callback(goal, null)
        }

        if(!mFirebaseRefsGoals.containsKey(pin)){
            val ref = getDatabaseRefGoals().document(pin).addSnapshotListener{documentSnapshot, firebaseFirestoreException ->
                if(mProfileCallbacksGoals.containsKey(pin)){ // has a valid callback

                    val callbackList = mProfileCallbacksGoals[pin] // gets the list of users - will have atleast one

                    // START - GET USER AND CACHE //
                    var goal : Goal? = null

                    if(documentSnapshot != null){
                        goal = getGoal(documentSnapshot) // gets data from database
                    }

                    if(goal!=null) { // caches the data
                        this.mCachedGoals[pin] = goal
                    }
                    // END - GET USER AND CACHE //

                    if(callbackList!=null) {
                        for (callbackGoal in callbackList) {
                            callback(goal, firebaseFirestoreException) //RETURN DATA
                        }
                    }
                }
            }
            mFirebaseRefsGoals[pin] = ref // add snapshot listeners to database
        }
        else{
            callback(null, null)
        }

    }

    fun removeGoalSingleListener(pin : String, onComplete : (Goal?, Exception?) -> Unit){
        val callbackList = mProfileCallbacksGoals[pin] // gets the list of callbacks for that user which was added in addListener

        if(callbackList != null && callbackList.contains(onComplete)){
            callbackList.remove(onComplete) // removes one call back at a time

            if(callbackList.size == 0){
                mProfileCallbacksGoals.remove(pin) // remove the list from the cache
                val databaseRef = mFirebaseRefsGoals[pin] // get value which is the listener
                if(databaseRef!=null){
                    databaseRef.remove() // remove the snapshot lister // change listener to unit
                    mFirebaseRefsGoals.remove(pin) // remove the ref from our cached list
                }
            }
            else {
                mProfileCallbacksGoals[pin] = callbackList // put the callback list back into list without the one just removed
            }
        }
    }

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

    private fun getGoal(snapshot: DocumentSnapshot) : Goal{
        val data: HashMap<String, Any> = snapshot.data as HashMap<String, Any>
        val id = data["id"] as String?
        val userId = data["userId"] as String?
        val goalSetDate = data["goalSetDate"] as Long?
        val goal = data["goal"] as String?
        val typeValue = data["goalType"] as Number
        val goalType = typeValue.toInt()
        val goalDateDeadline =data["goalDateDeadline"] as Long?
        return Goal(id, userId, goalSetDate, goal, goalType, goalDateDeadline)
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

    private fun getDatabaseRefGoals(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("goals")
    }
}