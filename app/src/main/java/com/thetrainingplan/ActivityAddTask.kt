package com.thetrainingplan

import android.app.DatePickerDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.adapters.AddTaskGoalsAdapter
import com.thetrainingplan.databinding.ActivityAddTaskBinding
import com.thetrainingplan.models.AddTask
import com.thetrainingplan.models.AddTaskModel
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.util.RecyclerViewClickListener
import com.thetrainingplan.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_add_task.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ActivityAddTask : AppCompatActivity(), RecyclerViewClickListener {

    var mRepeatInterval = 1

    private lateinit var viewModel: MainViewModel
    private var mCallbackAllUserGoalIds = { _:ArrayList<String?>?, _: Exception? -> Unit}
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private var listOfGoalPins = ArrayList<String>()
    private val mapGoalList = HashMap<String, Goal>()
    private var mIsSaving = false
    private var timeInMillieForStartDate : Long? = null
    private var timeInMillieForEndDate : Long? = null
    private var calendarEndDate : Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_add_task)
        // reference to view model
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        val binding: ActivityAddTaskBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_task)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.finishAddTaskActivityEvent.observe(this, Observer {
            dismissKeyboard()
            finish()
        })

        mCallbackCurrentGoal = { data : Goal?, _: Exception? ->
            if(data != null){
                data.id?.let { mapGoalList.put(it, data) }
            }
            setUpRecyclerView(mapGoalList)
        }

        mCallbackAllUserGoalIds = { data : ArrayList<String?>?, exc : Exception? ->
            if(exc != null){
                alert("current user exc: = ${exc.message}") {
                    okButton {  }
                }.show()
            }
            else{

                data?.let { listOfPin ->

                    if(listOfPin.size > 0){
                        val pins = ArrayList<String>()

                        for(pin in listOfPin){
                            pin?.let {
                                pins.add(pin)
                            }
                        }

                        listOfGoalPins = pins

                        for(pin in listOfPin){
                            // add goal listener
                            val userId = FirebaseAuth.getInstance().uid
                            if(userId != null){
                                if (pin != null) {
                                    GoalModel.addGoalSingleListener(userId, pin, mCallbackCurrentGoal)
                                }
                            }

                        }
                    }
                    else{
                        add_task_to_which_goal_recycler_view.adapter = AddTaskGoalsAdapter(ArrayList(), this)
                        viewModel.numberOfOpenGoals.value = listOfPin.size
                    }
                }?: run {
                    add_task_to_which_goal_recycler_view.adapter = AddTaskGoalsAdapter(ArrayList(), this)
                    viewModel.numberOfOpenGoals.value = 0
                }

            }
        }

        add_task_submit.setOnClickListener {
            addTask()
        }

        add_task_start_date_title.setOnClickListener {
            setDate(true)
        }

        add_task_end_date_title.setOnClickListener {
            setDate(false)
        }

        add_task_every.setText("$mRepeatInterval")
        add_event_plus.setOnCheckedChangeListener { _, _ ->
            add_event_plus.isChecked = false
            mRepeatInterval+=1
            add_task_every.setText("$mRepeatInterval")
        }
        add_event_minus.setOnCheckedChangeListener { _, _ ->
            add_event_minus.isChecked = false
            if(mRepeatInterval>1) {
                mRepeatInterval-=1
            }
            add_task_every.setText("$mRepeatInterval")
        }


    }

    private fun setDate(isStartDate : Boolean){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, yearDP, monthOfYear, dayOfMonth ->
            // Display Selected date in text box
            c.set(Calendar.YEAR, yearDP)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val date = SimpleDateFormat.getDateInstance(DateFormat.LONG).format(c.time)

            if(isStartDate){
                timeInMillieForStartDate = c.timeInMillis // this is what stored to database for task start date
                viewModel.taskStartDate.value = date
            }
            else{
                timeInMillieForEndDate = c.timeInMillis // this is what stored to database for task start date
                calendarEndDate = c
                viewModel.taskEndDate.value = date
            }

         /*   if(c.timeInMillis < Calendar.getInstance().timeInMillis){
                alert ("You need to enter a date in the future"){
                    okButton {  }
                }.show()
            }
            else{

                val date = SimpleDateFormat.getDateInstance(DateFormat.LONG).format(c.time)

                if(isStartDate){
                    timeInMillieForStartDate = c.timeInMillis // this is what stored to database for task start date
                    viewModel.taskStartDate.value = date
                }
                else{
                    timeInMillieForEndDate = c.timeInMillis // this is what stored to database for task start date
                    calendarEndDate = c
                    viewModel.taskEndDate.value = date
                }

            }*/

        }, year, month, day)

        dpd.show()
    }


    private fun addTask(){
        mIsSaving = true

        if(add_task_name.text.isBlank()){
            alert ("Please enter a name"){ okButton {  } }.show()
            mIsSaving = false
            return
        }
        if(add_task_description.text.isBlank()){
            alert ("Please enter a description"){ okButton {  } }.show()
            mIsSaving = false
            return
        }
        if(timeInMillieForStartDate == null){
            alert ("Please select a start date"){ okButton {  } }.show()
            mIsSaving = false
            return
        }
        val goalId = viewModel.goalSelectedAddTask.value
        if(goalId != null){

            val goal = mapGoalList[goalId]
            if(goal != null){
                alert ("Are you happy to put this task towards: ${goal.goal}"){
                    positiveButton("Yes"){

                    }
                    negativeButton("No"){
                        mIsSaving = false
                    }
                }

                if(!mIsSaving){
                    return
                }
            }
        }



        val newEvent = AddTask(null, null,  add_task_name.text.toString(), add_task_description.text.toString(), timeInMillieForStartDate!!, null, viewModel.addTaskState.value!!, null, null, null, null, goalId, null)

        if(viewModel.addTaskState.value != AddTaskModel.NEVER){
            // do this if it is a repeating task
            if(calendarEndDate==null){
                alert ("Please select a end date"){ okButton {  } }.show()
                mIsSaving = false
                return
            }

            calendarEndDate!!.set(Calendar.HOUR_OF_DAY , 23)
            calendarEndDate!!.set(Calendar.MINUTE , 59)
            calendarEndDate!!.set(Calendar.SECOND , 59)
            newEvent.endDate = calendarEndDate!!.timeInMillis
            newEvent.repeatEvery = mRepeatInterval
            if(viewModel.addTaskState.value!! == AddTaskModel.WEEKLY){
                var days = 0
                if(add_event_mon.isChecked){
                    days = days or 0x1
                }
                if(add_event_tue.isChecked){
                    days = days or 0x2
                }
                if(add_event_wed.isChecked){
                    days = days or 0x4
                }
                if(add_event_thu.isChecked){
                    days = days or 0x8
                }
                if(add_event_fri.isChecked){
                    days = days or 0x10
                }
                if(add_event_sat.isChecked){
                    days = days or 0x20
                }
                if(add_event_sun.isChecked){
                    days = days or 0x40
                }

                if(days == 0){
                    alert ("Please select at least one day for the repeating diary event"){
                        okButton {  }
                    }.show()
                    mIsSaving = false
                    return
                }

                newEvent.repeatWeekdays = days
            }
        }

        val task = newEvent
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            if (goalId != null) {
                AddTaskModel.addTask(userId, goalId, task){ data : Boolean?, exc : Exception? ->
                    if(data != null && data){
                        alert ("Task has been saved"){
                            okButton {  }
                        }.show()
                        dismissKeyboard()
                        finish()
                    }
                    else{
                        alert ("Task has NOT been saved"){
                            okButton {  }
                        }.show()
                    }
                }
            }
        }

    }

    private fun dismissKeyboard(){
        val inputManager: InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.SHOW_FORCED)
    }

    fun setUpRecyclerView(mapGoalList : HashMap<String, Goal>){

        add_task_to_which_goal_recycler_view.also {

        it.layoutManager = LinearLayoutManager(applicationContext)

        // get all the deleted goals
        val deletedOrCompletedGoals = ArrayList<String>()
        for(goal in ArrayList(mapGoalList.values)){
            if(goal.isDeleted != null || goal.isCompleted != null){
                goal.id?.let { it1 -> deletedOrCompletedGoals.add(it1) }
            }
        }

        //remove deleted goals from the list that will go on the UI
        for(deleted in deletedOrCompletedGoals){
            mapGoalList.remove(deleted)
        }

        //sort list by deadline date coming soon

        val sortedListByDeadlineDate = ArrayList(mapGoalList.values)
        viewModel.numberOfOpenGoals.value = sortedListByDeadlineDate.size


        sortedListByDeadlineDate.sortBy { list -> list.goalDateDeadline }

            if(sortedListByDeadlineDate.size > 0){
                val setGoalId = sortedListByDeadlineDate[0].id
                setGoalId?.let {
                    viewModel.goalSelectedAddTask.value = it
                }
            }
            else{
                viewModel.goalSelectedAddTask.value = null
            }


        it.adapter = AddTaskGoalsAdapter(sortedListByDeadlineDate, this)

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
            for(pin in listOfGoalPins){
                GoalModel.removeGoalSingleListener(pin, mCallbackCurrentGoal)
            }
        }
    }

    override fun onRecyclerViewItemClick(view: View, any: Any) {
        val mGoal = any is Goal
        if(mGoal){
            val goal = any as Goal
            viewModel.goalSelectedAddTask.value = goal.id
        }
    }
}
