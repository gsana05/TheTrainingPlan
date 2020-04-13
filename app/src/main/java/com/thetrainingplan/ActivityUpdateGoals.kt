package com.thetrainingplan

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.databinding.ActivityUpdateGoalsBinding
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.viewmodels.GoalsViewModel
import java.text.DateFormat
import java.text.SimpleDateFormat

class ActivityUpdateGoals : AppCompatActivity() {

    private lateinit var viewModel: GoalsViewModel
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private var goalId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_update_goals)

        // reference to view model
        viewModel = ViewModelProviders.of(this).get(GoalsViewModel::class.java)

        val binding: ActivityUpdateGoalsBinding = DataBindingUtil.setContentView(this, R.layout.activity_update_goals)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.finishUpdateGoals.observe(this, Observer {
            dismissKeyboard()
            finish()
        })

        goalId = intent.getStringExtra("id")
        goalId?.let {
            val mapGoalList = HashMap<String, Goal>()
            mCallbackCurrentGoal = { data : Goal?, _: Exception? ->
                if(data != null){
                    data.id?.let { goalPin ->
                        mapGoalList[goalPin] = data
                        val currentGoal = mapGoalList[goalPin]
                        currentGoal?.let {goal ->
                            viewModel.goal.value = goal
                            viewModel.goalSet.value = goal.goal
                            viewModel.dateGoalDeadlineInMillie.value = goal.goalDateDeadline
                            viewModel.dateGoalDeadline.value = SimpleDateFormat.getDateInstance(DateFormat.LONG).format(viewModel.dateGoalDeadlineInMillie.value)
                        }
                    }
                }
            }

            val userId = FirebaseAuth.getInstance().uid
            if(userId != null){
                GoalModel.addGoalSingleListener(userId, it, mCallbackCurrentGoal)
            }

        }

        viewModel.finishUpdateGoalsActivityEvent.observe(this, Observer{
            dismissKeyboard()
            finish()
        })
    }

    private fun dismissKeyboard(){
        val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            inputManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.SHOW_FORCED)
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
