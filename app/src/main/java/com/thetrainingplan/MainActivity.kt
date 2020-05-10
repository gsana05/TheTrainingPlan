package com.thetrainingplan

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.adapters.TasksAdaptor
import com.thetrainingplan.databinding.ActivityMainBinding
import com.thetrainingplan.models.*
import com.thetrainingplan.util.RecyclerViewClickListener
import com.thetrainingplan.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : TrainingPlanActivity(), RecyclerViewClickListener {

    override fun onRecyclerViewItemClick(view: View, any : Any) {

        if(any is AddTask){
            when(view.id){
                R.id.recycler_view_button -> {
                    any.goalId?.let { any.id?.let { it1 -> taskUpdateAlert(any, any.name, it, it1) } }
                }
            }
        }
        else{
            alert ("Please contact support - Something went wrong"){
                okButton {  }
            }.show()
        }

    }

    private lateinit var viewModel: MainViewModel
    private var mCallbackCurrentUser = { _:User?, _: Exception? -> Unit}
    private val theUsers = ArrayList<User>()
    private var mCallbackAllUserGoalIds = { _:ArrayList<String?>?, _: Exception? -> Unit}

    private var callbackForAllGoalTasks = { _:ArrayList<AddTask?>?, _: Exception? -> Unit}
    private val mapOfTasksForGoals = HashMap<String, ArrayList<AddTask?>?>()

    //private val listOfAllTasks = ArrayList<AddTask>()

    private var mapOfAllTasks = HashMap<String, AddTask>()
    private var listOpenGoals = ArrayList<Goal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        // reference to view model
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        //copy this to other activities
        setSupportActionBar(shop_navigation_toolbar)

        // hiding the label in title bar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupMenu(shop_drawer_layout_main, shop_nav_view_main, shop_navigation_toolbar)


        //Get the Date corresponding to 11:01:00 pm today.
        //Get the Date corresponding to 11:01:00 pm today.

       /* val userId = FirebaseAuth.getInstance().uid

        if(userId != null){
            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = 21
            calendar[Calendar.MINUTE] = 52
            calendar[Calendar.SECOND] = 59
            val time = calendar.time

            setAlarm(time)
        }*/




   /*     this.runOnUiThread {

            Looper.prepare()

            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = 19
            calendar[Calendar.MINUTE] = 56
            calendar[Calendar.SECOND] = 59
            val time = calendar.time

            Timer("SettingUp", false).schedule(time) {

                alert ("it is midnight - get ready to start your new tasks for today"){
                    okButton {  }
                }.show()

                //updateTasksAtMidNight()
            }
        }*/

       /* val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            alert ("it is midnight - get ready to start your new tasks for today"){
                okButton {  }
            }.show()
        }, 10000)*/

        viewModel.startStatsActivityEvent.observe(this, Observer{
            val intent = Intent(this, ActivityStats::class.java)
            startActivity(intent)
        })

        viewModel.startAddTaskActivityEvent.observe(this, Observer {
            val intent = Intent(this, ActivityAddTask::class.java)
            startActivity(intent)
            /*if(listOpenGoals.size > 0){

            }
            else{
                alert ("You need to set a Goal before adding a task"){
                    okButton {  }
                }.show()
            }*/


        })

        viewModel.startEnrollActivityEvent.observe(this, Observer {
            /*val intent = Intent(this, ActivityTrainingPrograms::class.java)
            startActivity(intent)*/
            val intent = Intent(this, ActivityEnrollTrainingProgram::class.java)
            startActivity(intent)
        })

        viewModel.startGoalsActivityEvent.observe(this, Observer {
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        })

        viewModel.startReadGoalsActivityEvent.observe(this, Observer{
            val intent = Intent(this, ActivityReadGoals::class.java)
            startActivity(intent)
        })

        mCallbackAllUserGoalIds = { data : ArrayList<String?>?, _ : Exception? ->
            if(data != null){

                //to get the number of goals
                val listOfPinIds = data
                listOfPinIds.let {
                    listOpenGoals.clear()
                    if(it.size > 0){
                        for(goalId in it){
                            val userId = FirebaseAuth.getInstance().uid
                            if (goalId != null && userId != null) {
                                GoalModel.getGoal(userId, goalId){ data : Goal?, _: Exception? ->
                                    if(data != null){

                                        // main activity displays number of open goals
                                        if(data.isCompleted == null && data.isDeleted == null){

                                            //get the list of goal ids
                                           val listOfGoalPins = ArrayList<String>()
                                            for( goal in listOpenGoals){
                                                goal.id?.let { id ->
                                                    listOfGoalPins.add(id)
                                                }
                                            }

                                            val hasId = listOfGoalPins.contains(data.id)
                                            if(!hasId){
                                                listOpenGoals.add(data)
                                            }


                                        }

                                        viewModel.numberOfOpenGoals.value = listOpenGoals.size

                                        // display today's tasks
                                        callbackForAllGoalTasks = { tasks : ArrayList<AddTask?>?, _ : Exception? ->

                                            tasks?.let {ta ->

                                                for( i in ta){
                                                    i?.let { t ->
                                                        t.id?.let {taskId ->
                                                            mapOfAllTasks[taskId] = t
                                                        }
                                                    }
                                                }
                                            }

                                            val filteredTaskForToday = AddTaskModel.filterEventsForDate(ArrayList(mapOfAllTasks.values), Calendar.getInstance())

                                            val checkForDeleted = AddTaskModel.filterForDeleted(filteredTaskForToday)

                                            //val checkForDone = AddTaskModel.filterRemoveDone(checkForDeleted)

                                            viewModel.numberOfTodayTasks.value = checkForDeleted.size

                                            var numberOfCompletedTasks = 0
                                            for( task in checkForDeleted){
                                                if(AddTaskModel.checkTaskIsCompleted(task)){
                                                    numberOfCompletedTasks++
                                                }
                                            }

                                            viewModel.numberOfCompletedTodayTasks.value = numberOfCompletedTasks

                                            main_recycler_view.also {recyclerView ->
                                                recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                                                recyclerView.adapter = TasksAdaptor(checkForDeleted, this)
                                                if(checkForDeleted.size < 1){
                                                    main_recycler_view_no_tasks_signage.visibility = View.VISIBLE
                                                }
                                                else{
                                                    main_recycler_view_no_tasks_signage.visibility = View.GONE
                                                }
                                            }

                                        }

                                        //listen to all the tasks for that goal
                                        AddTaskModel.addAllGoalTaskListeners(userId, goalId, callbackForAllGoalTasks)

                                    }
                                }
                            }
                        }
                    }
                    else{
                        //go goals mean no tasks outstanding or completed
                        viewModel.numberOfOpenGoals.value = listOpenGoals.size
                        viewModel.numberOfTodayTasks.value = 0
                        viewModel.numberOfCompletedTodayTasks.value = 0
                    }
                }

            }
        }

        mCallbackCurrentUser = { data : User?, exc: Exception? ->
            if(exc != null){
                alert("current user exc: = ${exc.message}") {
                    okButton {  }
                }.show()
            }
            else{
                if(data != null){
                    viewModel.currentUser.value = data
                }
            }
        }

    }

/*    private fun setAlarm(date: Date) {
        val alarmManager : AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ActivityAlarmBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0,  intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
            date.time, 86400000,
            pendingIntent)

        *//*alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
            date.time, AlarmManager.INTERVAL_DAY,
            pendingIntent)*//*
    }*/

    /*private fun goToUrl(url: String){
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }*/

    private fun alertCompletionTime(task : AddTask, goalId: String, taskId : String, dialogUpdate: AlertDialog){
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

            if(hours.toLong() > 0){
                val hour = TimeUnit.HOURS.toSeconds(hours.toLong())
                timeHours = hour
            }

            if(minutes.toLong() > 0){
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
                AddTaskModel.addToDoneDates(task, userId, goalId, taskId){ data: Boolean?, _: java.lang.Exception? ->
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

        private fun taskUpdateAlert(task : AddTask, taskName: String, goalId: String, taskId : String){

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
                        alertCompletionTime(task, goalId, taskId, dialog)
                    }
                }


                val delete : Button = inflatedLayout.findViewById(R.id.update_task_complete_delete)
                delete.setOnClickListener {

                    AddTaskModel.addToDeletedDates(task, userId, goalId, taskId){ data: Boolean?, _: java.lang.Exception? ->
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





      /*  val title: TextView = inflatedLayout.findViewById(R.id.notification_alert_title)
        notificationTitle?.let {
            title.text = it
        }

        val body: TextView = inflatedLayout.findViewById(R.id.notification_alert_body)
        notificationBody?.let {
            body.text = it
        }

        val link: Button = inflatedLayout.findViewById(R.id.notification_alert_link_button)
        if(notificationLink != null){
            link.visibility = View.VISIBLE
            link.setOnClickListener {
                //goToUrl(notificationLink)
            }
        }
        else{
            link.visibility = View.GONE
        }

        val dismissBtn: Button = inflatedLayout.findViewById(R.id.notification_alert_dismiss_button)
        dismissBtn.setOnClickListener {
            dialog.dismiss()
        }*/
    }


    override fun onPause() {
        super.onPause()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            UserModel.removeCurrentUserListener(userId, mCallbackCurrentUser)
            GoalModel.removeAllUsersGoalIdsListeners(userId, mCallbackAllUserGoalIds)

            for ((key, _) in mapOfTasksForGoals) {
                AddTaskModel.removeAllGoalTasksListeners(key, callbackForAllGoalTasks)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().uid
        if(userId == null){
            startActivity(Intent(this, ActivityLogIn::class.java))
        }
        else{
            UserModel.addCurrentUserListener(userId, mCallbackCurrentUser)
            GoalModel.addAllUsersGoalIdsListeners(userId, mCallbackAllUserGoalIds)
        }
    }

}
