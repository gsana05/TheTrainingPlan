package com.thetrainingplan.models

import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
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

    fun setTimeCompletionDoneDates(userId: String, goalId: String, taskId: String, timeCompletion : Long, onComplete: (data: Boolean?, exc: Exception?) -> Unit){
        firebaseRefAddTask(userId, goalId).document(taskId).update("completionTime", timeCompletion).addOnCompleteListener {
            if(it.isSuccessful){
                onComplete(true, null)
            }
            else{
                onComplete(false, null)
            }
        }
    }

    fun addToDoneDates(task : AddTask, userId: String, goalId: String, taskId : String, onComplete: (data: Boolean?, exc: Exception?) -> Unit){
        val newDoneTasks = ArrayList<Date>()
        val currentDoneTasks = task.doneDates
        val date = Calendar.getInstance().time

        currentDoneTasks?.let {
            newDoneTasks.addAll(currentDoneTasks)
        }

        newDoneTasks.add(date)

        firebaseRefAddTask(userId, goalId).document(taskId).update("doneDates", newDoneTasks).addOnCompleteListener {
            if(it.isSuccessful){
                onComplete(true, null)
            }
            else{
                onComplete(false, null)
            }
        }
    }

    fun addToDeletedDates(task : AddTask, userId: String, goalId: String, taskId : String, onComplete: (data: Boolean?, exc: Exception?) -> Unit){
        val newDeletedTasks = ArrayList<Date>()
        val currentDeletedTasks = task.deletedDates
        val date = Calendar.getInstance().time


       currentDeletedTasks?.let {
           newDeletedTasks.addAll(currentDeletedTasks)
       }

        newDeletedTasks.add(date)


        firebaseRefAddTask(userId, goalId).document(taskId).update("deletedDates", newDeletedTasks).addOnCompleteListener {
            if(it.isSuccessful){
                onComplete(true, null)
            }
            else{
                onComplete(false, null)
            }
        }
    }

    fun checkTaskIsCompleted(task : AddTask) : Boolean{

        val todayDate = Date(Calendar.getInstance().timeInMillis)
        var isTaskCompleted : Boolean = false
        val format = SimpleDateFormat("yyyyMMMdd")
        task.doneDates?.let {dates ->

            if(dates.size > 0){
                val timeStampDates = dates as ArrayList<Timestamp>

                for(stamp in timeStampDates){

                    val date = stamp.toDate()

                    val doneDate = format.format(date)
                    val dateToday = format.format(todayDate)

                    if(doneDate == dateToday){
                        isTaskCompleted = true
                    }

                }
            }
        }

        return isTaskCompleted
    }

    fun filterRemoveDone(tasks : ArrayList<AddTask>) : ArrayList<AddTask>{

        val todayDate = Date(Calendar.getInstance().timeInMillis)
        val listOfTasks = ArrayList<AddTask>()

        for(task in tasks){
            if(task.doneDates == null){
                listOfTasks.add(task)
                continue
            }
            else{
                val format = SimpleDateFormat("yyyyMMMdd")
                task.doneDates?.let {dates ->

                    if(dates.size > 0){
                        val timeStampDates = dates as ArrayList<Timestamp>

                        for(stamp in timeStampDates){

                            val date = stamp.toDate()

                            val doneDate = format.format(date)
                            val dateToday = format.format(todayDate)

                            if(doneDate != dateToday){
                                listOfTasks.add(task)
                                //tasks.remove(task)
                            }

                        }
                    }
                    else{
                        listOfTasks.add(task)
                    }


                }?:run{
                    listOfTasks.add(task)
                }

            }

        }

        return listOfTasks
    }

    fun filterForDeleted(tasks : ArrayList<AddTask>) : ArrayList<AddTask>{

        val todayDate = Date(Calendar.getInstance().timeInMillis)
        val listOfTasks = ArrayList<AddTask>()

        for(task in tasks){
            if(task.deletedDates == null){
                continue
            }
            else{
                val format = SimpleDateFormat("yyyyMMMdd")

                task.deletedDates?.let {dates ->

                    if(dates.size > 0){
                        val timeStampDates = dates as ArrayList<Timestamp>

                        for(stamp in timeStampDates){

                            val date = stamp.toDate()

                            val deletedDate = format.format(date)
                            val dateToday = format.format(todayDate)

                            if(deletedDate != dateToday){
                                listOfTasks.add(task)
                                //tasks.remove(task)
                            }

                        }
                    }
                    else{
                        listOfTasks.add(task)
                    }


                }?: run {
                    listOfTasks.add(task)
                }

            }

        }

        return listOfTasks
    }

    fun filterEventsForDate(diaryEntries: ArrayList<AddTask>, cal: Calendar): ArrayList<AddTask> {
        val items = ArrayList<AddTask>()
        val date = Date(cal.timeInMillis)

        for (d in diaryEntries) {

            val format = SimpleDateFormat("yyyyMMMdd")
            if (format.format(d.startDate) == format.format(date) && d.repeatType == NEVER) { // did it start today
                items.add(d)
            }

            if ( d.endDate == null) { // is the repeating date valid
                continue
            }

            //after this point the task has been filtered for only repeating tasks
            // repeating asks begin

            val startDate = d.startDate
            val startDateCal = Calendar.getInstance()
            startDate?.let {
                startDateCal.time = Date(startDate)
            }

            // sets start date to the very first second of the day
            startDateCal.set(Calendar.HOUR_OF_DAY,0)
            startDateCal.set(Calendar.MINUTE,0)
            startDateCal.set(Calendar.SECOND,0)

            val endDate = d.endDate
            val endDateCal = Calendar.getInstance()
            endDate?.let {
                endDateCal.time = Date(endDate)
            }

            // sets end date to the very last second of the day
            endDateCal.set(Calendar.HOUR_OF_DAY,23)
            endDateCal.set(Calendar.MINUTE,59)
            endDateCal.set(Calendar.SECOND,59)

            val s = startDateCal.time
            val e = endDateCal.time

            if (!startDateCal.time.before(date) || !endDateCal.time.after(date)) { // does it span today
                continue
            }

            d.startDate?.let {

                val yt = timePeriodBetween(Date(it) , d.repeatType)

                val mod = timePeriodBetween(Date(it) , d.repeatType) % d.repeatEvery!!
                val dayOfTheMonth = cal.get(Calendar.DAY_OF_MONTH)
                val monthOfTheDay = cal.get(Calendar.MONTH)

                val eventCalendar = Calendar.getInstance()
                eventCalendar.time = Date(it)

                if (d.endDate != null && d.repeatType == DAILY) {// daily repeat
                    // daily repeat
                    val p = timePeriodBetween(date, d.repeatType) % d.repeatEvery!!
                    if ((timePeriodBetween(date, d.repeatType) % d.repeatEvery!!) == mod) { // this check the stat date and the today's date are the same
                        items.add(d)
                    }
                    else{
                        val i = 2
                    }
                }

                if (d.repeatType == WEEKLY) { // weekly repeat

                    d.repeatWeekdays.let {
                        // sunday
                        var dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) - 7

                        if(dayOfWeek == -1){
                            dayOfWeek = 6
                        }

                        if ((d.repeatWeekdays!! and (1 shl dayOfWeek)) != 0) {
                            if ((timePeriodBetween(date, d.repeatType) % d.repeatEvery!!) == mod) {
                                items.add(d)
                            }
                        }
                    }
                }

                if (d.repeatType == MONTHLY) {  // monthly repeat
                    if (dayOfTheMonth == eventCalendar.get(Calendar.DAY_OF_MONTH)) {
                        d.repeatEvery?.let {
                            if ((timePeriodBetween(date, d.repeatType) % d.repeatEvery!!) == mod) {
                                items.add(d)
                            }
                        }
                    }
                }

                if (d.repeatType == ANNUALLY) {
                    if (dayOfTheMonth == eventCalendar.get(Calendar.DAY_OF_MONTH) && monthOfTheDay == eventCalendar.get(Calendar.MONTH)) {
                        d.repeatEvery?.let {
                            if ((timePeriodBetween(date, d.repeatType) % d.repeatEvery!!) == mod) {
                                items.add(d)
                            }
                        }
                    }
                }
            }
        }

        return items
    }

    private fun timePeriodBetween(startDate:Date, repeatType:Int):Int{
        val startOfTimeCalendar = Calendar.getInstance()
        startOfTimeCalendar.time = Date(0)

        val startDateCal = Calendar.getInstance()
        startDateCal.setTime(startDate)
        startDateCal.set(Calendar.MILLISECOND,0)
        startDateCal.set(Calendar.SECOND,0)
        startDateCal.set(Calendar.MINUTE,0)
        startDateCal.set(Calendar.HOUR,0)

        val diffMillis = startDateCal.timeInMillis-startOfTimeCalendar.timeInMillis

        // this returns you the number of days from January 1970 until the start date
        when(repeatType){
            NEVER->{
                return 0
            }
            DAILY->{
                val i = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
                return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
            }
            WEEKLY->{
                return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()/7
            }
            MONTHLY->{
                return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()/28
            }
            ANNUALLY->{
                return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()/365
            }
        }
        return 0
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

            val completionTime= data["completionTime"] as Long?

            val deletedDates = data["deletedDates"] as ArrayList<Date>?

            return repeatType?.let {
                AddTask(id,requestUserId, name, description, startDate, endDate,
                    it, repeatEvery, repeatWeekdays, deletedDates, doneDates, goalId, completionTime )
            }
        }
        else{
            return null
        }


    }

    fun deleteAllTasksInAGoal(userId: String, goalId: String, onComplete : (data : Boolean?, exc : Exception?) -> Unit){

        getAllTasksForGoal(userId, goalId){ data : ArrayList<AddTask>?, _ : Exception? ->

            if(data != null && data.size > 0){

                for( document in data){
                    document.id?.let { taskId ->
                        deleteTask(userId, goalId, taskId)
                    }
                }

                getAllTasksForGoal(userId, goalId){ data1 : ArrayList<AddTask>?, _ : Exception? ->
                    if(data1 == null || data1.size < 1){
                        onComplete(true, null)
                    }
                    else{
                        onComplete(false, Exception("Tasks have not been deleted"))
                    }


                }

            }
            else{
                onComplete(true, null)
            }

        }
    }

    private fun getAllTasksForGoal(userId: String, goalId: String, onComplete : (data : ArrayList<AddTask>?, exc : Exception?) -> Unit){
        firebaseRefAddTask(userId, goalId).get().addOnCompleteListener {
            if(it.isSuccessful){
                val documents = it.result?.documents
                if(documents != null){
                    val listOfTasks = ArrayList<AddTask>()
                    for( document in documents){
                        val task = getTask(document)
                        task?.let {
                            listOfTasks.add(it)
                        }
                    }
                    onComplete(listOfTasks, null)
                }
            }
            else{
                onComplete(ArrayList(), it.exception)
            }
        }
    }

    private fun deleteTask(userId: String, goalId: String, taskId : String){
        firebaseRefAddTask(userId, goalId).document(taskId).delete().addOnCompleteListener {
            if(it.isSuccessful){
                Log.v("TAG", "Task deleted")
            }
            else{
                Log.v("TAG", "Task NOT deleted")
            }
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
        map["completionTime"]=task.completionTime


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