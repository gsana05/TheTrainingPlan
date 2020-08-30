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
import java.util.*
import kotlin.collections.ArrayList

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
                viewModel.totalNumberOfGoals.value = listOfGoals.size

                val listOfOpenGoals = listOfGoals.filter { it.isDeleted == null && it.isCompleted == null }
                viewModel.openGoals.value = listOfOpenGoals.size

                val listOfCompletedGoals = listOfGoals.filter { it.isCompleted != null }
                viewModel.completedGoals.value = listOfCompletedGoals.size

                val listOfDeletedGoals = listOfGoals.filter { it.isDeleted != null }
                viewModel.deletedGoals.value = listOfDeletedGoals.size


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

                                val tasksForGoals = ArrayList(mapOfAllTasks.values)
                                var repeatingTasks = 0L
                                var tasksDeleted = 0
                                // from the tasks get number of repeating tasks
                                // get all repeating tasks
                                val allTasksWithRepeating = tasksForGoals.filter { it.repeatEvery != null }
                                //workout how many times they repeat until the end date
                                if(allTasksWithRepeating.isNotEmpty()){ // repeating task
                                    allTasksWithRepeating.map {
                                        // tasks that have been deleted
                                        it.deletedDates?.let { tasksDel ->
                                            tasksDeleted += tasksDel.size
                                        }

                                        // total number of tasks including repeating tasks
                                        it.startDate?.let {start ->
                                            val st = Date(start)

                                            it.endDate?.let { end ->
                                                val en = Date(end)

                                                val daysBetween = AddTaskModel.numberOfDaysBetweenDates(st, en)
                                                val i = daysBetween

                                                it.repeatEvery?.let { repeat ->

                                                    val numberOfRepeatTasks = daysBetween / repeat
                                                    val ans = numberOfRepeatTasks
                                                    repeatingTasks += ans
                                                    //take one away as you would have added the same task twice - once in repeating and once in tasks
                                                    repeatingTasks--

                                                }

                                            }

                                        }
                                    }
                                }

                                val allTasksNotRepeating = tasksForGoals.filter { it.repeatEvery == null }
                                allTasksNotRepeating.map {task ->
                                    // tasks that have been deleted
                                    task.deletedDates?.let { taskDeleted ->
                                        if(taskDeleted.size > 0){
                                            tasksDeleted += taskDeleted.size
                                        }
                                    }
                                }

                                viewModel.deletedTasks.value = tasksDeleted
                                viewModel.totalNumberOfTasks.value = tasksForGoals.size + repeatingTasks.toInt()

                                val checkForDeleted = AddTaskModel.filterToGetDeletedTasks(tasksForGoals)

                                val i = checkForDeleted

                                /*val listOfOpenTasks = tasks.filter { }
                                viewModel.openTasks.value = listOfOpenTasks.size

                                val listOfCompletedTasks = tasks.filter { it.doneDates }
                                viewModel.completedTasks.value = listOfCompletedTasks.size

                                val listOfDeletedTasks = tasks.filter { it.isDeleted != null }
                                viewModel.deletedTasks.value = listOfDeletedTasks.size*/

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