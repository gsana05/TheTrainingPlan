package com.thetrainingplan

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.databinding.ActivityDiaryBinding
import com.thetrainingplan.databinding.ActivityMainBinding
import com.thetrainingplan.models.AddTask
import com.thetrainingplan.models.AddTaskModel
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.viewmodels.DiaryViewModel
import com.thetrainingplan.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_diary.*
import kotlinx.android.synthetic.main.activity_statistics_board.*
import kotlinx.android.synthetic.main.diary_item.view.*
import kotlinx.android.synthetic.main.diary_page_item.*
import kotlinx.android.synthetic.main.diary_page_item.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class ActivityDiary : AppCompatActivity() {

    private lateinit var viewModel: DiaryViewModel
    private var mCallbackAllUserGoalIds = { _:ArrayList<String?>?, _: Exception? -> Unit}
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private var callbackForAllGoalTasks = { _:ArrayList<AddTask?>?, _: Exception? -> Unit}
    private val mapGoalList = HashMap<String, Goal>()
    private var listOfGoalPins = ArrayList<String>()
    private var mapOfAllTasks = HashMap<String, AddTask>()
    private val listOfTaskCallbacks = ArrayList<String>()
    var mDiaryEntries: java.util.ArrayList<AddTask>?=null
    val mDiarySize = 730

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_diary)
        // reference to view model
        viewModel = ViewModelProviders.of(this).get(DiaryViewModel::class.java)

        val binding: ActivityDiaryBinding = DataBindingUtil.setContentView(this, R.layout.activity_diary)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        diary_header_icon.setOnClickListener {
            finish()
        }

        diary_page_item_day_today_btn.setOnClickListener {
            showToday()
        }

        diary_previous_arrow_button.setOnClickListener {
            showPreviousDay()
        }

        diary_next_arrow_button.setOnClickListener {
            showNextDay()
        }

        mCallbackAllUserGoalIds = { data : ArrayList<String?>?, _ : Exception? ->
            data?.let {
                if(data.size > 0){

                    for(pin in data){
                        //clear cache - when goals have been removed
                        val listOfCacheGoals = ArrayList(mapGoalList.values)
                        for(i in listOfCacheGoals){
                            if(!data.contains(i.id)){ // it needs to match listOfPin as that is fresh from database
                                mapGoalList.remove(i.id)
                                i.id?.let { it1 -> GoalModel.removeGoalSingleListener(it1, mCallbackCurrentGoal) }
                            }
                        }


                        // add goal listener
                        val userId = FirebaseAuth.getInstance().uid
                        if(userId != null){
                            pin?.let { it1 ->
                                if(!mapGoalList.contains(it1)){
                                    GoalModel.addGoalSingleListener(userId, it1, mCallbackCurrentGoal)
                                }
                            }
                        }

                    }

                    data.let { pinsList ->
                        val pins = ArrayList<String>()

                        for(pin in pinsList){
                            pin?.let {
                                pins.add(pin)
                            }
                        }

                        listOfGoalPins = pins
                    }

                } else{
                    //remove listeners - if all goals have been deleted
                    for(pin in ArrayList(mapGoalList.values)){
                        pin.id?.let { it1 -> GoalModel.removeGoalSingleListener(it1, mCallbackCurrentGoal) }
                    }

                    // remove from local cache
                    mapGoalList.clear()

                    //update recycle view
                    //setUpRecyclerView()
                }
            }
        }


        //get all the tasks
        mCallbackCurrentGoal = { data : Goal?, _: Exception? ->
            if(data != null){
                data.id?.let {
                    mapGoalList.put(it, data)
                }
            }
            //setUpRecyclerView()
            if(listOfGoalPins.size == mapGoalList.size){ // when we have all goal pins and have used these pins to get the goals then run this code
                val listOfGoals = ArrayList(mapGoalList.values)


                // tasks stats in a goal - remove delete goal stats
                val listOfCompletedOpenGoals = listOfGoals.filter { it.isDeleted == null }
                listOfCompletedOpenGoals.forEach { goal ->
                    val userId = FirebaseAuth.getInstance().uid
                    if(userId != null){

                        goal.id?.let { goalId ->

                            // display today's tasks
                            callbackForAllGoalTasks = { tasks : ArrayList<AddTask?>?, _ : Exception? ->

                                tasks?.let {ta ->
                                    for( i in ta){
                                        i?.let { t ->
                                            t.id?.let {taskId ->
                                                mapOfAllTasks[taskId] = t
                                                mDiaryEntries = ArrayList(mapOfAllTasks.values)
                                                diary_page_list.adapter = DiaryPagerAdaper()
                                                showToday()
                                                /*if(listOfTaskCallbacks.size == mapGoalList.size){ // set the adapter only when we have all tasks from goal
                                                    val endResult = mapOfAllTasks[taskId]
                                                }*/
                                            }
                                        }
                                    }
                                }
                            }



                            //listen to all the tasks for that goal
                            if(!listOfTaskCallbacks.contains(goalId)){
                                listOfTaskCallbacks.add(goalId)
                                AddTaskModel.addAllGoalTaskListeners(userId, goalId, callbackForAllGoalTasks)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            GoalModel.addAllUsersGoalIdsListeners(userId, mCallbackAllUserGoalIds)
        }
    }

    override fun onPause() {
        super.onPause()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            GoalModel.removeAllUsersGoalIdsListeners(userId, mCallbackAllUserGoalIds)
        }

        for(pin in listOfGoalPins){
            GoalModel.removeGoalSingleListener(pin, mCallbackCurrentGoal)
        }

        for(goalId in listOfTaskCallbacks){
            AddTaskModel.removeAllGoalTasksListeners(goalId, callbackForAllGoalTasks)
        }

        mapOfAllTasks.clear()

    }

    fun showNextDay(){
        diary_page_list.currentItem += 1
    }

    fun showPreviousDay(){
        diary_page_list.currentItem -= 1
    }

    fun showToday(){
        diary_page_list.currentItem = mDiarySize/2
    }

    fun getCurrentDate(position: Int) : Calendar{

        val size = mDiarySize/2
        val daysToRemove = size-position

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysToRemove)

        return cal
    }

    inner class DiaryPagerAdaper(): PagerAdapter(){

        override fun getCount(): Int {
            return mDiarySize
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        @SuppressLint("SimpleDateFormat")
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var items = java.util.ArrayList<AddTask>()

            val layout = LayoutInflater.from(this@ActivityDiary).inflate(R.layout.diary_page_item, container, false)

            val cal = getCurrentDate(position)

            val formatDay = SimpleDateFormat("EEEE")

            val dateFormat = DateFormat.getDateInstance(DateFormat.DATE_FIELD)

            val date = Date(cal.timeInMillis)

            val dayPrefix = if(position == (mDiarySize / 2 )) "Today - " else ""
            /*layout.diary_page_item_day.text =  dayPrefix + formatDay.format(date)*/
            layout.diary_page_item_day.text =  dayPrefix + formatDay.format(date)
            layout.diary_page_item_date.text = dateFormat.format(date)




            mDiaryEntries?.let {
                items = AddTaskModel.filterEventsForDate(it,cal)

                //items = AddTaskModel.filterForDeleted(items)

                layout.diary_page_item_events_list.adapter = DiaryDayItemListAdapter(items, date)
            }


            container.addView(layout)
            return layout
        }

        private fun scrollEventsList(recyclerView: RecyclerView, numPositions: Int) {
            recyclerView.scrollBy(0, (recyclerView.height - 20) * numPositions)
        }

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView(view as View)
        }
    }

    inner class DiaryDayItemListAdapter(val entries: ArrayList<AddTask>, val dateOfDiary: Date): RecyclerView.Adapter <DiaryDayItemListAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.diary_item, parent, false))
        }

        override fun getItemCount(): Int {
            return entries.size
        }

        @SuppressLint("SimpleDateFormat")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = entries[position]

            if(entry.repeatEvery != null){
                holder.itemView.diary_item_layout.setBackgroundColor(resources.getColor(R.color.nka_red))

                val format = SimpleDateFormat("dd-MMMM-yyyy")
                val timestamps = entry.doneDates as? ArrayList<com.google.firebase.Timestamp>

                timestamps?.let {
                    for(i in timestamps){
                        val doneDate = i.toDate()
                        val dd = format.format(doneDate)
                        val td = format.format(dateOfDiary)
                        if(dd == td){
                            holder.itemView.diary_item_layout.setBackgroundColor(resources.getColor(R.color.aqua))
                        }
                    }
                }


                val timestampsDelete = entry.deletedDates as? ArrayList<com.google.firebase.Timestamp>

                val i = dateOfDiary
                timestampsDelete?.let { deletedTaskDates ->
                    for(deleteDate in deletedTaskDates){
                        val i = format.format(deleteDate.toDate())
                        val x = format.format(dateOfDiary)
                        if(i == x){
                            holder.itemView.diary_item_layout.setBackgroundColor(resources.getColor(R.color.black))
                        }
                    }
                }


                //holder.itemView.testdate.text = dateOfDiary.toString()
            }
            else{
                val isCompleted = AddTaskModel.checkTaskIsCompleted(entry)

                if(isCompleted){
                    holder.itemView.diary_item_layout.setBackgroundResource(R.drawable.green_border_thick)
                    holder.itemView.diary_item_layout.setBackgroundColor(resources.getColor(R.color.green_success))
                }
                else{
                    holder.itemView.diary_item_layout.setBackgroundResource(R.drawable.blue_border_thick)
                }
            }



            holder.itemView.diary_entry_title.text = entry.name

            val format = DateFormat.getTimeInstance(DateFormat.SHORT)
            holder.itemView.diary_item_ago.text = format.format(entry.startDate)
            //holder.itemView.diary_item_type.setImageResource(if(entry.repeatType!=null && entry.repeatType!=DiaryEntry.NEVER) R.drawable.ic_refresh_arrow else R.drawable.ic_calendar)
            //holder.itemView.diary_item_type.setBackgroundColor(ResourcesCompat.getColor(resources,if(entry.repeatType!=null && entry.repeatType!=AddTaskModel.NEVER) R.color.nka_red else R.color.light_blue,null))

            // click on specific accepted request event
            holder.itemView.setOnClickListener {

                val formatDates = SimpleDateFormat("yyyyMMMdd")

                val timeStampDeletedDates = entry.deletedDates as? ArrayList<com.google.firebase.Timestamp>
                val deletedDates = timeStampDeletedDates?.map { formatDates.format(it.toDate()) }

                val timeStampDoneDates = entry.doneDates as? ArrayList<com.google.firebase.Timestamp>
                val completedDates = timeStampDoneDates?.map { formatDates.format(it.toDate()) }

                val diaryDate = formatDates.format(dateOfDiary)

                val delete = deletedDates?.contains(diaryDate) ?: false // if its null then set to false
                val done = completedDates?.contains(diaryDate) ?: false

                if(delete == false && done == false){ // if a date is either in deleted or done dates do not show pop up
                    entry.goalId?.let { goalId -> entry.id?.let { taskId ->
                            taskUpdateAlert(entry,
                                entry.name,
                                goalId,
                                taskId,
                                dateOfDiary
                            )
                        }
                    }
                }
                else{
                    alert ("This task has been resolved"){
                        okButton {  }
                    }.show()
                }


            }
        }

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){ }
    }

    private fun taskUpdateAlert(task : AddTask, taskName: String, goalId: String, taskId : String, dateOfDiary: Date){

        val builder = AlertDialog.Builder(this)
        val viewGroup = findViewById<View>(android.R.id.content) as ViewGroup
        val inflatedLayout: View = layoutInflater.inflate(R.layout.task_update_alert, viewGroup, false)
        builder.setView(inflatedLayout)

        val dialog = builder.show()
        dialog.setCancelable(false)

        val exit : ImageView
                = inflatedLayout.findViewById(R.id.update_task_exit)
        exit.setOnClickListener {
            dialog.dismiss()
        }

        val name: TextView = inflatedLayout.findViewById(R.id.update_task_task_input)
        name.text = taskName

        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            GoalModel.getGoal(userId, goalId){ data : Goal?, _: Exception? ->
                if(data != null){
                    val idGoal: TextView = inflatedLayout.findViewById(R.id.update_task_task_towards_spinner)
                    idGoal.text = data.goal
                }
            }

            val completed : Button = inflatedLayout.findViewById(R.id.update_task_complete_button)
            val isCompleted = AddTaskModel.checkTaskIsCompleted(task)
            if(isCompleted){
                completed.visibility = View.GONE
            }
            else{
                completed.visibility = View.VISIBLE
                completed.setOnClickListener {
                    alertCompletionTime(task, goalId, taskId, dialog, dateOfDiary)
                }
            }


            val delete : Button = inflatedLayout.findViewById(R.id.update_task_complete_delete)
            delete.setOnClickListener {

                AddTaskModel.addToDeletedDates(task, userId, goalId, taskId, dateOfDiary){ data: Boolean?, _: java.lang.Exception? ->
                    if(data != null && data){
                        alert ("success"){
                            okButton {  }
                        }.show()
                        dismissKeyboard(dialog, it)
                        dialog.dismiss()
                    }
                    else{
                        alert ("fail"){
                            okButton {  }
                        }.show()
                        dismissKeyboard(dialog, it)
                        dialog.dismiss()
                    }
                }
            }

        }
    }

    private fun alertCompletionTime(task : AddTask, goalId: String, taskId : String, dialogUpdate: AlertDialog, dateOfDiary: Date){
        val builder = AlertDialog.Builder(this)
        val viewGroup = findViewById<View>(android.R.id.content) as ViewGroup
        val inflatedLayout: View = layoutInflater.inflate(R.layout.alert_completion_time, viewGroup, false)
        builder.setView(inflatedLayout)

        val dialog = builder.show()
        dialog.setCancelable(false)

        val close : ImageView = inflatedLayout.findViewById(R.id.completion_task_exit)
        close.setOnClickListener {
            dialog.dismiss()
        }

        val timeTaken : Button = inflatedLayout.findViewById(R.id.completion_task_complete_button)
        timeTaken.setOnClickListener {
            val numberOfHours : EditText = inflatedLayout.findViewById(R.id.completion_task_task_input)
            val hours = numberOfHours.text.toString().trim()

            val numberOfMinutes: EditText = inflatedLayout.findViewById(R.id.completion_task_task_towards_spinner)
            val minutes = numberOfMinutes.text.toString().trim()

            var timeMinuets : Long? = null
            var timeHours : Long? = null

            if(hours.isEmpty()){
                numberOfHours.requestFocus()
                numberOfHours.error = "Enter a number of hours"
                return@setOnClickListener
            }

            if(minutes.isEmpty()){
                numberOfMinutes.requestFocus()
                numberOfMinutes.error = "Enter a number of minutes"
                return@setOnClickListener
            }

            if(hours.toLong() >= 0){
                val hour = TimeUnit.HOURS.toSeconds(hours.toLong())
                timeHours = hour
            }

            if(minutes.toLong() >= 0){
                val mins = TimeUnit.MINUTES.toSeconds(minutes.toLong())
                timeMinuets = mins
            }

            var completionTime : Long? = null
            val op = timeHours
            val p = timeMinuets
            op?.let { h ->
                p?.let { m ->
                    completionTime = h + m
                }
            }


            val userId = FirebaseAuth.getInstance().uid
            if(userId != null){
                AddTaskModel.addToDoneDates(task, userId, goalId, taskId, dateOfDiary){ data: Boolean?, _: java.lang.Exception? ->
                    if(data != null && data){
                        completionTime?.let { it1 ->
                            AddTaskModel.setTimeCompletionDoneDates(userId, goalId, taskId, it1){ data : Boolean?, _: Exception? ->

                                if(data != null && data){
                                    alert ("success"){
                                        okButton {  }
                                    }.show()

                                    dismissKeyboard(dialog, it)
                                    dialog.dismiss()
                                    dialogUpdate.dismiss()
                                }
                                else{
                                    alert ("error"){
                                        okButton {  }
                                    }.show()
                                    dismissKeyboard(dialog, it)
                                    dialog.dismiss()
                                    dialogUpdate.dismiss()
                                }

                            }
                        }
                    }
                    else{
                        alert ("error"){
                            okButton {  }
                        }.show()
                        dismissKeyboard(dialog, it)
                        dialog.dismiss()
                        dialogUpdate.dismiss()
                    }
                }
            }
        }
    }

    private fun dismissKeyboard(alertDialog: AlertDialog, view: View){
        val imm = alertDialog.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

}