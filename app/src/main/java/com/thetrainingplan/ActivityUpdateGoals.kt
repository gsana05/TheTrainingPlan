package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.adapters.GoalsAdapter
import com.thetrainingplan.databinding.ActivityGoalsBinding
import com.thetrainingplan.databinding.ActivityUpdateGoalsBinding
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.viewmodels.GoalsViewModel
import com.thetrainingplan.viewmodels.UpdateGoalsViewModel
import kotlinx.android.synthetic.main.activity_goals.*

class ActivityUpdateGoals : AppCompatActivity() {

    private lateinit var viewModel: UpdateGoalsViewModel
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private var goalId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_update_goals)

        // reference to view model
        viewModel = ViewModelProviders.of(this).get(UpdateGoalsViewModel::class.java)

        val binding: ActivityUpdateGoalsBinding = DataBindingUtil.setContentView(this, R.layout.activity_update_goals)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        goalId = intent.getStringExtra("id")
        goalId?.let {
            val mapGoalList = HashMap<String, Goal>()
            mCallbackCurrentGoal = { data : Goal?, _: Exception? ->
                if(data != null){
                    data.id?.let { goalPin ->
                        mapGoalList[goalPin] = data
                        val goal = mapGoalList[goalPin]
                        goal?.let {goal ->
                            viewModel.goal.value = goal
                        }
                    }
                }
            }
            GoalModel.addGoalSingleListener(it, mCallbackCurrentGoal)
        }
    }

    override fun onPause() {
        super.onPause()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            goalId?.let { GoalModel.removeGoalSingleListener(it, mCallbackCurrentGoal) }
        }
    }
}
