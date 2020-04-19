package com.thetrainingplan

import android.content.Intent
import android.os.Bundle
import android.view.View
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

        val callbackForAllGoalTasks = { data : ArrayList<AddTask?>?, exc : Exception? ->


        }

        mCallbackAllUserGoalIds = { data : ArrayList<String?>?, _ : Exception? ->
            if(data != null){

                val map = HashMap<String, ArrayList<AddTask?>?>()
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

                                    if(data.isCompleted == null && data.isDeleted == null){
                                        listOpenGoals.add(data)
                                    }

                                    viewModel.numberOfOpenGoals.value = listOpenGoals.size

                                    val userId = FirebaseAuth.getInstance().uid
                                    if(userId != null){
                                        AddTaskModel.addAllGoalTaskListeners(userId, goalId){ data : ArrayList<AddTask?>?, exc : Exception? ->

                                            // get all the task for each goal
                                            data?.let {
                                                for(i in it){
                                                    i?.let {task ->
                                                        listOfAllTasks.add(task)
                                                    }
                                                }
                                            }

                                            map[goalId] = data
                                            val kok = map

                                            main_recycler_view.also {
                                                it.layoutManager = LinearLayoutManager(applicationContext)
                                                it.adapter = WorkoutHistoryAdaptor(listOfAllTasks, this)
                                            }

                                        }

                                    }
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
