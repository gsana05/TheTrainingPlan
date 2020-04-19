package com.thetrainingplan.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import java.io.InvalidObjectException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

object AddTaskModel {

    const val NEVER = 1
    const val DAILY = 2
    const val WEEKLY = 3
    const val MONTHLY = 4
    const val ANNUALLY = 5


    // task listeners
    private val mFirebaseRefsAllTasks : HashMap<String, ListenerRegistration> = HashMap()
    private var mCachedAllGoalTasks: HashMap<String, ArrayList<AddTask?>> = HashMap() // these are the cached profiles
    private var mAllGoalTasksCallbacks = HashMap<String, ArrayList<(ArrayList<AddTask?>, Exception?) -> Unit>>() // callbacks for that user id

    fun addAllGoalTaskListeners(userId : String, goalId: String, onComplete : (ArrayList<AddTask?>, Exception?) -> Unit){

        var callbacks = ArrayList<(ArrayList<AddTask?>, Exception?) -> Unit>()

        // does it already have a callback
        if(mAllGoalTasksCallbacks.containsKey(goalId)){
            mAllGoalTasksCallbacks[goalId]?.let {
                callbacks = it
            }
        }
        else{
            callbacks = ArrayList()
        }

        // does not have this callback
        if(!callbacks.contains(onComplete)){
            callbacks.add(onComplete)
            mAllGoalTasksCallbacks[goalId] = callbacks
        }

        // return is user has already been cached and no data has changed for that user
        if(mCachedAllGoalTasks.containsKey(goalId)){ // if user data has been cached then return with this already saved user data
            val profile = mCachedAllGoalTasks[goalId]
            if (profile != null) {
                onComplete(profile, null)
            }
        }

        // do this if it has no listener yet
        if(!mFirebaseRefsAllTasks.contains(goalId)){
            val ref = firebaseRefAddTask(userId, goalId).addSnapshotListener{ querySnaphot, firebaseFirestoreException ->
                if(mAllGoalTasksCallbacks.containsKey(goalId)){ // has a valid callback

                    val callbackList = mAllGoalTasksCallbacks[goalId]
                    val list = ArrayList<AddTask?>()

                    if(querySnaphot != null){
                        for(documentSnapshot in querySnaphot){
                            val i = documentSnapshot.id
                            val task = getTask(documentSnapshot)
                            list.add(task)
                        }
                    }

                    // caching the data
                    if(list.size > 0){
                        this.mCachedAllGoalTasks[goalId] = list
                    }

                    if(callbackList!=null) {
                        onComplete(list, firebaseFirestoreException)
                    }

                }
            }
            mFirebaseRefsAllTasks[goalId] = ref
        }
    }

    fun removeAllGoalTasksListeners(goalId : String, onComplete : (ArrayList<AddTask?>?, Exception?) -> Unit){
        val callbackList = mAllGoalTasksCallbacks[goalId] // gets the list of callbacks for that user which was added in addListener

        if(callbackList != null && callbackList.contains(onComplete)){
            callbackList.remove(onComplete) // removes one call back at a time

            if(callbackList.size == 0){
                mAllGoalTasksCallbacks.remove(goalId) // remove the list from the cache
                val databaseRef = mFirebaseRefsAllTasks[goalId] // get value which is the listener
                if(databaseRef!=null){
                    databaseRef.remove() // remove the snapshot lister // change listener to unit
                    mFirebaseRefsAllTasks.remove(goalId) // remove the ref from our cached list
                }
            }
            else {
                mAllGoalTasksCallbacks[goalId] = callbackList // put the callback list back into list without the one just removed
            }
        }
    }

    private fun getTask(snapshot: DocumentSnapshot) : AddTask?{

        val checkIfSnapshotHasNotBeenDeleted = snapshot.data
        if(checkIfSnapshotHasNotBeenDeleted != null){ // this check is needed for when you delete a goals document from database
            val data: HashMap<String, Any> = snapshot.data as HashMap<String, Any>

            //val id = data["id"] as String
            val id = snapshot.id

            val requestUserId = data["requestUserId"] as String?
            val doneDates = data["doneDates"] as ArrayList<Date>?
            val name = data["name"] as String
            val description = data["description"] as String?
            val startDate=data["startDate"] as Long
            val goalId=data["goalId"] as String?

            val repeatTypeNum = data["repeatType"] as Number?
            val repeatType = repeatTypeNum?.toInt()

            val repeatEveryNum = data["repeatEvery"] as Number?
            val repeatEvery = repeatEveryNum?.toInt()

            val repeatWeekDaysNum = data["repeatWeekdays"] as Number?
            val repeatWeekdays = repeatWeekDaysNum?.toInt()

            val endDate = data["endDate"] as Long?

            val deletedDates = data["deletedDates"] as ArrayList<Date>?

            return repeatType?.let {
                AddTask(id,requestUserId, name, description, startDate, endDate,
                    it, repeatEvery, repeatWeekdays, deletedDates, doneDates, goalId )
            }
        }
        else{
            return null
        }


    }


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

    private fun firebaseRefAddTask(userId : String, goalId : String) : CollectionReference {
        return FirebaseFirestore.getInstance().collection("goals").document(userId).collection("goalPins").document(goalId).collection("goalTasks")
    }

    private fun toMap(task : AddTask): HashMap<String, Any?> {
        val map: HashMap<String, Any?> = HashMap()
        map["requestUserId"] = task.requestUserId
        map["doneDates"] = task.doneDates
        map["name"]=task.name
        map["description"]=task.description
        map["startDate"]= task.startDate
        if(task.endDate!=null) {
            map["endDate"] = task.endDate!!
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