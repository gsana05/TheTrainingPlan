package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.thetrainingplan.databinding.ActivityStatisticsPerGoalBinding
import com.thetrainingplan.databinding.ActivityStatsBinding
import com.thetrainingplan.models.AddTask
import com.thetrainingplan.models.AddTaskModel
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.viewmodels.StatsViewModel
import kotlinx.android.synthetic.main.activity_statistics_board.*
import kotlinx.android.synthetic.main.activity_statistics_per_goal.*
import java.util.*
import kotlin.collections.ArrayList

class ActivityStatisticsPerGoal : AppCompatActivity() {

    private lateinit var viewModel: StatsViewModel
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private lateinit var userId : String
    private lateinit var goalId : String
    private var mapOfAllTasks = HashMap<String, AddTask>()

    private var callbackForAllGoalTasks = { _:ArrayList<AddTask?>?, _: Exception? -> Unit}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_statistics_per_goal)

        viewModel = ViewModelProviders.of(this).get(StatsViewModel::class.java)

        val binding: ActivityStatisticsPerGoalBinding = DataBindingUtil.setContentView(this, R.layout.activity_statistics_per_goal)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        userId = intent.getStringExtra("userId") ?: throw Exception("Please specify a user ID")
        goalId = intent.getStringExtra("goalId") ?: throw Exception("Please specify a goal ID")

        statistics_per_goal_heading.setOnClickListener {
            finish()
        }

        mCallbackCurrentGoal = { data : Goal?, _: Exception? ->
            data?.let {
                statistics_per_goal_view_goal.text = data.goal
            }?:run{
                statistics_per_goal_view_goal.text = "error"
            }
        }

        callbackForAllGoalTasks = { tasks : ArrayList<AddTask?>?, _ : Exception? ->
            tasks?.let { tasksForGoal ->

                tasksForGoal?.let {ta ->
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
                var tasksDone = 0


                val allTasksNotRepeating = tasksForGoals.filter { it.repeatEvery == null }
                allTasksNotRepeating.map {task ->

                    val datesDeleted  = task.deletedDates
                    val datesDone = task.doneDates

                    // tasks that have been deleted
                    datesDeleted?.let { taskDeleted ->
                        if(taskDeleted.size > 0){
                            tasksDeleted += taskDeleted.size
                        }
                    }

                    // tasks done
                    datesDone?.let {donedates ->
                        tasksDone += donedates.size
                    }
                }

                // from the tasks get number of repeating tasks
                // get all repeating tasks
                val allTasksWithRepeating = tasksForGoals.filter { it.repeatEvery != null }
                //workout how many times they repeat until the end date
                if(allTasksWithRepeating.isNotEmpty()){ // repeating task
                    allTasksWithRepeating.map {

                        val datesDeleted  = it.deletedDates
                        val datesDone = it.doneDates

                        // tasks that have been deleted
                        datesDeleted?.let { tasksDel ->
                            tasksDeleted += tasksDel.size
                        }

                        // tasks done
                        datesDone?.let {donedates ->
                            tasksDone += donedates.size
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
                                    val repeatTasksForThisOneTask = numberOfRepeatTasks.toInt()
                                    repeatingTasks += repeatTasksForThisOneTask
                                    //inclusive of the start day
                                    repeatingTasks++


                                }

                            }

                        }
                    }
                }

                val sizeOfRepeating = repeatingTasks.toInt()

                val totalTasks = allTasksNotRepeating.size + sizeOfRepeating

                viewModel.totalNumberOfTasksPerGoal.value = totalTasks
                viewModel.totalNumberOfTasksPerGoalOpenTasks.value = totalTasks - (tasksDone + tasksDeleted)
                viewModel.totalNumberOfTasksPerGoalCompletedTasks.value = tasksDone
                viewModel.totalNumberOfTasksPerGoalDeletedTasks.value = tasksDeleted

                // number of hours and minuets towards the goal
                var time = 0
                tasksForGoals.map {
                    it.completionTime?.let {completion ->
                        time += completion.toInt()
                    }
                }

                val timeSpent = convertLongToTime(time.toLong())
                statistics_per_goals_view_number_of_hours_result.text = timeSpent[0].toString()
                statistics_per_goals_view_number_of_minutes_result.text = timeSpent[1].toString()

            }
        }

    }

    private fun convertLongToTime(timeCompletion : Long) : IntArray{
        timeCompletion.let {
            val longVal: Long = it
            val hourss = longVal.toInt() / 3600
            val ioi = hourss
            var remainder = longVal.toInt() - hourss * 3600
            val mins = remainder / 60
            remainder = remainder - mins * 60
            val secs = remainder

            val ints = intArrayOf(hourss, mins, secs)
            return ints
        }
    }

    override fun onResume() {
        super.onResume()
        GoalModel.addGoalSingleListener(userId, goalId, mCallbackCurrentGoal)
        AddTaskModel.addAllGoalTaskListeners(userId, goalId, callbackForAllGoalTasks)

    }

    override fun onPause() {
        super.onPause()
        GoalModel.removeGoalSingleListener(goalId, mCallbackCurrentGoal)
        AddTaskModel.removeAllGoalTasksListeners(goalId, callbackForAllGoalTasks)
    }
}