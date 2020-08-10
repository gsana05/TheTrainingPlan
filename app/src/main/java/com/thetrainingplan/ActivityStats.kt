package com.thetrainingplan

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.adapters.GoalsAdapter
import com.thetrainingplan.adapters.StatsGoalAdapter
import com.thetrainingplan.adapters.TasksAdaptor
import com.thetrainingplan.databinding.ActivityStatsBinding
import com.thetrainingplan.models.AddTask
import com.thetrainingplan.models.AddTaskModel
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.util.RecyclerViewClickListener
import com.thetrainingplan.viewmodels.StatsViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_stats.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import java.util.*
import kotlin.collections.ArrayList

class ActivityStats : AppCompatActivity(), RecyclerViewClickListener {

    private lateinit var viewModel: StatsViewModel

    private var callbackForAllGoalTasks = { _:ArrayList<AddTask?>?, _: Exception? -> Unit}
    private var mCallbackAllUserGoalIds = { _:ArrayList<String?>?, _: Exception? -> Unit}
    private val mapGoalList = HashMap<String, Goal>()
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private var listOfGoalPins = ArrayList<String>()
    private val listOfTaskCallbacks = ArrayList<String>()
    private var mapOfAllTasks = HashMap<String, AddTask>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_stats)

        viewModel = ViewModelProviders.of(this).get(StatsViewModel::class.java)

        val binding: ActivityStatsBinding = DataBindingUtil.setContentView(this, R.layout.activity_stats)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        statistics_goals_icon.setOnClickListener {
            finish()
        }


        mCallbackCurrentGoal = { data : Goal?, _: Exception? ->
            if(data != null){
                data.id?.let { mapGoalList.put(it, data) }
            }
            //setUpRecyclerView()
            if(listOfGoalPins.size == mapGoalList.size){
                val listOfGoals = ArrayList(mapGoalList.values)

                statistics_heading_individual_recycler_view.also {recyclerView ->

                    val goals = ArrayList(mapGoalList.values)

                    recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                    recyclerView.adapter = StatsGoalAdapter(ArrayList(mapGoalList.values), this)
                    /*  if(checkForDeleted.size < 1){
                          main_recycler_view_no_tasks_signage.visibility = View.VISIBLE
                      }
                      else{
                          main_recycler_view_no_tasks_signage.visibility = View.GONE
                      }*/
                }


                listOfGoals.forEach { goal ->
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
                                            }
                                        }
                                    }
                                }

                                val tasks = ArrayList(mapOfAllTasks.values)

                              /*  val filteredTaskForToday = AddTaskModel.filterEventsForDate(ArrayList(mapOfAllTasks.values), Calendar.getInstance())

                                val checkForDeleted = AddTaskModel.filterForDeleted(filteredTaskForToday)

                                if(goal.isDeleted != null || goal.isCompleted != null){
                                    // this means the goal is NOT open - so remove those tasks
                                    val tasksToRemove = ArrayList<AddTask>()
                                    for( i in checkForDeleted){
                                        if(i.goalId == goal.id){
                                            tasksToRemove.add(i)
                                        }
                                    }

                                    for (k in tasksToRemove){
                                        if(checkForDeleted.contains(k)){
                                            mapOfAllTasks.remove(k.id)
                                            checkForDeleted.remove(k)
                                        }
                                    }

                                }

                                //val checkForDone = AddTaskModel.filterRemoveDone(checkForDeleted)

                                var numberOfCompletedTasks = 0
                                for( task in checkForDeleted){
                                    if(AddTaskModel.checkTaskIsCompleted(task)){
                                        numberOfCompletedTasks++
                                    }
                                }*/


                            }
                            //listen to all the tasks for that goal
                            AddTaskModel.addAllGoalTaskListeners(userId, goalId, callbackForAllGoalTasks)
                            listOfTaskCallbacks.add(goalId)

                        }


                    }



                }



            }

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
                            }
                        }


                        // add goal listener
                        val userId = FirebaseAuth.getInstance().uid
                        if(userId != null){
                            pin?.let { it1 ->
                                GoalModel.addGoalSingleListener(userId, it1, mCallbackCurrentGoal)
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
                    for(pin in data){
                        if (pin != null) {
                            GoalModel.removeGoalSingleListener(pin, mCallbackCurrentGoal)
                        }
                    }

                    // remove from local cache
                    mapGoalList.clear()

                    //update recycle view
                    //setUpRecyclerView()
                }
            }
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

    override fun onResume() {
        super.onResume()

        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            GoalModel.addAllUsersGoalIdsListeners(userId, mCallbackAllUserGoalIds)
        }

    }

    private fun convertLongToTime(timeCompletion : Long){
        timeCompletion.let {
            val longVal: Long = it
            val hourss = longVal.toInt() / 3600
            val ioi = hourss
            var remainder = longVal.toInt() - hourss * 3600
            val mins = remainder / 60
            remainder = remainder - mins * 60
            val secs = remainder

            val ints = intArrayOf(hourss, mins, secs)
        }
    }

    override fun onRecyclerViewItemClick(view: View, any: Any) {
        alert ("pressed"){
            okButton {  }
        }.show()
    }
}
