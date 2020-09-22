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
import kotlinx.android.synthetic.main.activity_statistics_per_goal.*

class ActivityStatisticsPerGoal : AppCompatActivity() {

    private lateinit var viewModel: StatsViewModel
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private lateinit var userId : String
    private lateinit var goalId : String

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
            tasks?.let {
                statistics_per_goal_view_total_tasks_value.text = it.size.toString()
            }?: run{
                statistics_per_goal_view_total_tasks_value.text = "Error"
            }
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