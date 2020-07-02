package com.thetrainingplan.models

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object GoalModel {

    const val OTHER_ID = 0
    const val SPIRITUAL_ID = 1
    const val PHYSICAL_ID = 2
    const val PSYCHOLOGY_ID = 3
    const val EMOTIONAL_ID = 4

    const val OTHER = "Other"
    const val SPIRITUAL = "Spiritual"
    const val PHYSICAL = "Physical"
    const val PSYCHOLOGY = "Psychology"
    const val EMOTIONAL = "Emotional"

    private var mCachedGoals : HashMap<String, Goal> = HashMap() // these are the cached profiles
    private var mFirebaseRefsGoals : HashMap<String, ListenerRegistration> = HashMap() // these are our watches on the database
    private var mProfileCallbacksGoals = HashMap<String, ArrayList<(Goal?, Exception?) -> Unit>>() // callbacks for that user id

   /* fun numberOfGoalsListeners() : Int? {
        return mFirebaseRefsGoals.size
    }*/

    fun addGoalSingleListener(userId: String, pin : String, callback : (Goal?, Exception?) -> Unit){

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
            val ref = getDatabaseRefUserGoalsIds(userId).document(pin).addSnapshotListener{documentSnapshot, firebaseFirestoreException ->
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


    private val mFirebaseRefsAllUserGoalIds : HashMap<String, ListenerRegistration> = HashMap()
    private var mCachedAllUserGoalIds : HashMap<String, ArrayList<String?>> = HashMap() // these are the cached profiles
    private var mAllUserGoalIdsCallbacks = HashMap<String, ArrayList<(ArrayList<String?>, Exception?) -> Unit>>() // callbacks for that user id

    fun addAllUsersGoalIdsListeners(userId : String, onComplete : (ArrayList<String?>, Exception?) -> Unit){

        var callbacks = ArrayList<(ArrayList<String?>, Exception?) -> Unit>()

        // does it already have a callback
        if(mAllUserGoalIdsCallbacks.containsKey(userId)){
            mAllUserGoalIdsCallbacks[userId]?.let {
                callbacks = it
            }
        }
        else{
            callbacks = ArrayList()
        }

        // does not have this callback
        if(!callbacks.contains(onComplete)){
            callbacks.add(onComplete)
            mAllUserGoalIdsCallbacks[userId] = callbacks
        }

        // return is user has already been cached and no data has changed for that user
        if(mCachedAllUserGoalIds.containsKey(userId)){ // if user data has been cached then return with this already saved user data
            val profile = mCachedAllUserGoalIds[userId]
            if (profile != null) {
                onComplete(profile, null)
            }
        }

        // do this if it has no listener yet
        if(!mFirebaseRefsAllUserGoalIds.contains(userId)){
            val ref = getDatabaseRefUserGoalsIds(userId).addSnapshotListener{ querySnaphot, firebaseFirestoreException ->
                if(mAllUserGoalIdsCallbacks.containsKey(userId)){ // has a valid callback

                    val callbackList = mAllUserGoalIdsCallbacks[userId]
                    val list = ArrayList<String?>()

                    if(querySnaphot != null){
                        for(documentSnapshot in querySnaphot){
                            val id = documentSnapshot.id
                            list.add(id)
                        }
                    }

                    // caching the data
                    if(list.size > 0){
                        this.mCachedAllUserGoalIds[userId] = list
                    }

                    if(callbackList!=null) {
                        onComplete(list, firebaseFirestoreException)
                    }

                }
            }
            mFirebaseRefsAllUserGoalIds[userId] = ref
        }
    }

    fun removeAllUsersGoalIdsListeners(userId : String, onComplete : (ArrayList<String?>?, Exception?) -> Unit){
        val callbackList = mAllUserGoalIdsCallbacks[userId] // gets the list of callbacks for that user which was added in addListener

        if(callbackList != null && callbackList.contains(onComplete)){
            callbackList.remove(onComplete) // removes one call back at a time

            if(callbackList.size == 0){
                mAllUserGoalIdsCallbacks.remove(userId) // remove the list from the cache
                val databaseRef = mFirebaseRefsAllUserGoalIds[userId] // get value which is the listener
                if(databaseRef!=null){
                    databaseRef.remove() // remove the snapshot lister // change listener to unit
                    mFirebaseRefsAllUserGoalIds.remove(userId) // remove the ref from our cached list
                }
            }
            else {
                mAllUserGoalIdsCallbacks[userId] = callbackList // put the callback list back into list without the one just removed
            }
        }
    }

    fun getGoal(userId: String, goalId : String, callback: (Goal?, Exception?) -> Unit){
        getDatabaseRefUserGoalsIds(userId).document(goalId).get().addOnCompleteListener {
            if(it.isSuccessful){
                val result = it.result
                if (result != null) {
                    val goal = getGoal(result)
                    callback(goal, null)
                }
                else{
                    callback(null, it.exception)
                }
            }
            else{
                callback(null, it.exception)
            }
        }
    }

    fun addOrUpdateGoal(userId : String, goal : Goal, isNew : Boolean, callback : (Boolean?, Exception?) -> Unit){

        if(goal.id == null){
            goal.id = UUID.randomUUID().toString()
        }

        goal.id?.let {goalId ->
            getDatabaseRefUserGoalsIds(userId).document(goalId).set(toMap(goal)).addOnCompleteListener {
                if(it.isSuccessful){
                    //call a firebase function to add goal id to the users list
                    if(isNew){
                        goal.id?.let { _ ->

                            // write goal to userGoalsIds collection
                            addUserGoalIds(userId, goalId,goal, callback)

                            //UserModel.updateGoals(userId, idGoal, callback)
                        }
                    }
                    else{
                        callback(true, null)
                    }

                }
                else{
                    callback(false, it.exception)
                }
            }
        }
    }

    private fun addUserGoalIds(userId: String, goalId: String, goal : Goal, onComplete: (Boolean?, Exception?) -> Unit){
        getDatabaseRefUserGoalsIds(userId).document(goalId).set(
            toMap(goal)).addOnCompleteListener {
            if(it.isSuccessful){
                UserModel.updateGoals(userId, goalId, onComplete)
            }
            else{
                onComplete(false, it.exception)
            }
        }
    }

    fun updateIsDeleteGoal(userId: String, goalPin : String, callback : (Boolean?, Exception?) -> Unit){
        getDatabaseRefUserGoalsIds(userId).document(goalPin).update("isDeleted", getCurrentTimeInMillie()).addOnCompleteListener {task ->
            if(task.isSuccessful){
                callback(true, null)
            }
            else{
                callback(false, task.exception)
            }
        }
    }

    private fun getCurrentTimeInMillie() : Long{
        return Calendar.getInstance().time.time
    }

    fun updateIsCompletedGoal(userId: String, goalPin : String, callback : (Boolean?, Exception?) -> Unit){

        getDatabaseRefUserGoalsIds(userId).document(goalPin).update("isCompleted", getCurrentTimeInMillie()).addOnCompleteListener {task ->
            if(task.isSuccessful){
                callback(true, null)
            }
            else{
                callback(false, task.exception)
            }
        }
    }

    // by deleting the goal competed date it re-opens the goal
    fun reOpenGoal(userId: String, goalPin : String, callback : (Boolean?, Exception?) -> Unit){
        getDatabaseRefUserGoalsIds(userId).document(goalPin).update(
            "isCompleted", null,
            "isDeleted", null)
            .addOnCompleteListener {task ->
                if(task.isSuccessful){
                    callback(true, null)
                }
                else{
                    callback(false, task.exception)
                }

            }
    }

    fun permanentlyDeleteGoalPin(userId: String, goalPin : String, callback : (Boolean?, Exception?) -> Unit){
           UserModel.removeGoalPin(userId, goalPin){ data : Boolean?, exc : Exception? ->
                     if(data != null && data){
                         callback(true, null)
                     }
                     else{
                         callback(false, exc)
                     }
                 }
    }

   fun permanentlyDeleteGoal(userId : String, goalPin : String, callback : (Boolean?, Exception?) -> Unit){

       getDatabaseRefUserGoalsIds(userId).document(goalPin).update("isPermanentlyDeleted", true)  .addOnCompleteListener {task ->
           if(task.isSuccessful){
               //remove listener
               mCachedGoals.remove(goalPin)
               callback(true, null)
           }
           else{
               callback(false, task.exception)
           }
       }

       /*getDatabaseRefUserGoalsIds(userId).document(goalPin).delete() // delete goal from the goals collection
            .addOnCompleteListener {task ->
                if(task.isSuccessful){
                    //remove listener
                    mCachedGoals.remove(goalPin)
                    callback(true, null)
                }
                else{
                    callback(false, task.exception)
                }
            }*/
    }

    private fun getGoal(snapshot: DocumentSnapshot) : Goal?{

        val checkIfSnapshotHasNotBeenDeleted = snapshot.data
        if(checkIfSnapshotHasNotBeenDeleted != null){ // this check is needed for when you delete a goals document from database
            val data: HashMap<String, Any> = snapshot.data as HashMap<String, Any>
            val id = data["id"] as? String?
            val userId = data["userId"] as? String?
            val goalSetDate = data["goalSetDate"] as? Long?
            val goal = data["goal"] as? String?
            val typeValue = data["goalType"] as Number
            val goalType = typeValue.toInt()
            val goalDateDeadline =data["goalDateDeadline"] as? Long?
            val isDeleted = data["isDeleted"] as? Long?
            val isCompleted =data["isCompleted"] as? Long?
            val isPermanentlyDeleted = data["isPermanentlyDeleted"] as? Boolean?
            return Goal(id, userId, goalSetDate, goal, goalType, goalDateDeadline, isDeleted, isCompleted, isPermanentlyDeleted)
        }
        else{
            return null
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
        map["isDeleted"]=goal.isDeleted
        map["isCompleted"]=goal.isCompleted
        map["isPermanentlyDeleted"]=goal.isPermanentlyDeleted
        return map
    }

    /*private fun getDatabaseRefGoals(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("goals")
    }*/

    private fun getDatabaseRefUserGoalsIds(userId: String): CollectionReference {
        return FirebaseFirestore.getInstance().collection("goals").document(userId).collection("goalPins")
    }

    /*private fun getDatabaseRefUserGoalIds(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("userGoalIds")
    }*/
}