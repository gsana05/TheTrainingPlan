package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.adapters.StatsGoalAdapter
import com.thetrainingplan.databinding.ActivityStatisticsBoardBinding
import com.thetrainingplan.databinding.ActivityStatsBinding
import com.thetrainingplan.models.AddTask
import com.thetrainingplan.models.AddTaskModel
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.viewmodels.StatsViewModel
import kotlinx.android.synthetic.main.activity_statistics_board.*
import kotlinx.android.synthetic.main.activity_stats.*
import java.util.HashMap

class ActivityStatisticsBoard : AppCompatActivity() {

    private lateinit var viewModel: StatsViewModel
    private var mCallbackAllUserGoalIds = { _:ArrayList<String?>?, _: Exception? -> Unit}
    private val mapGoalList = HashMap<String, Goal>()
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private var listOfGoalPins = ArrayList<String>()
    private var mapOfAllTasks = HashMap<String, AddTask>()
    private var callbackForAllGoalTasks = { _:ArrayList<AddTask?>?, _: Exception? -> Unit}
    private val listOfTaskCallbacks = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_statistics_board)

        viewModel = ViewModelProviders.of(this).get(StatsViewModel::class.java)

        val binding: ActivityStatisticsBoardBinding = DataBindingUtil.setContentView(this, R.layout.activity_statistics_board)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel


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

        mCallbackCurrentGoal = { data : Goal?, _: Exception? ->
            if(data != null){
                data.id?.let { mapGoalList.put(it, data) }
            }
            //setUpRecyclerView()
            if(listOfGoalPins.size == mapGoalList.size){
                val listOfGoals = ArrayList(mapGoalList.values)
                statistics_board_goals_view_number_of_goals.text = listOfGoals.size.toString()

              /*  statistics_heading_individual_recycler_view.also {recyclerView ->

                    val goals = ArrayList(mapGoalList.values).filter { it.isDeleted == null }
                   *//* recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                    recyclerView.adapter = StatsGoalAdapter(ArrayList(goals), this)*//*
                }*/


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
                                statistics_board_goals_view_number_of_tasks.text = tasks.size.toString()

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
                            if(!listOfTaskCallbacks.contains(goalId)){
                                AddTaskModel.addAllGoalTaskListeners(userId, goalId, callbackForAllGoalTasks)
                                listOfTaskCallbacks.add(goalId)
                            }


                        }
                    }
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
}