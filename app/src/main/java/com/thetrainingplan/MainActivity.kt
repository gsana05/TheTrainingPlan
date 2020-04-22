package com.thetrainingplan

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.adapters.WorkoutHistoryAdaptor
import com.thetrainingplan.databinding.ActivityMainBinding
import com.thetrainingplan.models.*
import com.thetrainingplan.util.RecyclerViewClickListener
import com.thetrainingplan.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.jetbrains.anko.runOnUiThread
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.schedule


class MainActivity : TrainingPlanActivity(), RecyclerViewClickListener {

    override fun onRecyclerViewItemClick(view: View, any : Any) {

        val theUser = any as User
        when(view.id){
            R.id.recycler_view_button -> {
                alert ("${theUser.name}"){
                    okButton {  }
                }.show()
                Toast.makeText(applicationContext, "Book Button Pressed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private lateinit var viewModel: MainViewModel
    private var mCallbackAllUsers = { _:ArrayList<User?>?, _: Exception? -> Unit}
    private var mCallbackCurrentUser = { _:User?, _: Exception? -> Unit}
    private val theUsers = ArrayList<User>()
    private var mCallbackAllUserGoalIds = { _:ArrayList<String?>?, _: Exception? -> Unit}

    private var callbackForAllGoalTasks = { _:ArrayList<AddTask?>?, _: Exception? -> Unit}
    private val mapOfTasksForGoals = HashMap<String, ArrayList<AddTask?>?>()

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

        val userId = FirebaseAuth.getInstance().uid

        if(userId != null){
            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = 21
            calendar[Calendar.MINUTE] = 52
            calendar[Calendar.SECOND] = 59
            val time = calendar.time

            setAlarm(time)
        }




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


        viewModel.startAddTaskActivityEvent.observe(this, Observer {
            val intent = Intent(this, ActivityAddTask::class.java)
            startActivity(intent)
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

                val listOfAllTasks = ArrayList<AddTask>()
                //to get the number of goals
                val listOpenGoals = ArrayList<Goal>()
                val listOfPinIds = data
                listOfPinIds.let {
                    for(goalId in it){
                        val userId = FirebaseAuth.getInstance().uid
                        if (goalId != null && userId != null) {
                            GoalModel.getGoal(userId, goalId){ data : Goal?, _: Exception? ->
                                if(data != null){

                                    // main activity displays number of open goals
                                    if(data.isCompleted == null && data.isDeleted == null){
                                        listOpenGoals.add(data)
                                    }

                                    viewModel.numberOfOpenGoals.value = listOpenGoals.size

                                    // display today's tasks
                                    callbackForAllGoalTasks = { tasks : ArrayList<AddTask?>?, _ : Exception? ->

                                        // get all the task for each goal
                                        tasks?.let {
                                            for(i in it){
                                                i?.let {task ->

                                                    val cal = Calendar.getInstance()
                                                    val format = SimpleDateFormat("yyyyMMMdd")
                                                    if (format.format(task.startDate)==format.format(cal.time)) {
                                                        listOfAllTasks.add(task)
                                                    }
                                                }
                                            }
                                        }

                                        mapOfTasksForGoals[goalId] = tasks


                                        main_recycler_view.also {
                                            it.layoutManager = LinearLayoutManager(applicationContext)
                                            it.adapter = WorkoutHistoryAdaptor(listOfAllTasks, this)
                                        }

                                    }

                                    //listen to all the tasks for that goal
                                    AddTaskModel.addAllGoalTaskListeners(userId, goalId, callbackForAllGoalTasks)

                                }
                            }
                        }
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

        mCallbackAllUsers = { data : ArrayList<User?>?, exc : Exception? ->
            if(exc != null){
                alert("exc = ${exc.message}") {
                    okButton {  }
                }.show()
            }

            theUsers.clear()
            data?.let { list->
                for(user in list){
                    user?.let {
                        theUsers.add(it)
                    }
                }
            }

            /*main_recycler_view.also {
                it.layoutManager = LinearLayoutManager(applicationContext)
                it.adapter = WorkoutHistoryAdaptor(theUsers, this)
            }*/

          /*  clientListAdapter = WorkoutHistoryAdaptor(theUsers, this)
            val mLayoutManager = LinearLayoutManager(applicationContext)
            main_recycler_view.layoutManager = mLayoutManager
            main_recycler_view.adapter = clientListAdapter*/


            //viewModel.listOfUser.value = data

        }
    }

    private fun setAlarm(date: Date) {
        val alarmManager : AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ActivityAlarmBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0,  intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
            date.time, 86400000,
            pendingIntent)

        /*alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
            date.time, AlarmManager.INTERVAL_DAY,
            pendingIntent)*/
    }

    /*private fun goToUrl(url: String){
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }*/



    override fun onPause() {
        super.onPause()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            UserModel.removeCurrentUserListener(userId, mCallbackCurrentUser)
            UserModel.removeAllUsersListeners(userId, mCallbackAllUsers)
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
            UserModel.addAllUsersListeners(userId, mCallbackAllUsers)
            GoalModel.addAllUsersGoalIdsListeners(userId, mCallbackAllUserGoalIds)
        }
    }

}
