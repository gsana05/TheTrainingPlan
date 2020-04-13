package com.thetrainingplan

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.adapters.AddTaskGoalsAdapter
import com.thetrainingplan.databinding.ActivityAddTaskBinding
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.util.RecyclerViewClickListener
import com.thetrainingplan.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_add_task.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class ActivityAddTask : AppCompatActivity(), RecyclerViewClickListener {

    private lateinit var viewModel: MainViewModel
    private var mCallbackAllUserGoalIds = { _:ArrayList<String?>?, _: Exception? -> Unit}
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private var listOfGoalPins = ArrayList<String>()
    private val mapGoalList = HashMap<String, Goal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_add_task)
        // reference to view model
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        val binding: ActivityAddTaskBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_task)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.finishAddTaskActivityEvent.observe(this, Observer {
            finish()
        })

        mCallbackCurrentGoal = { data : Goal?, _: Exception? ->
            if(data != null){
                data.id?.let { mapGoalList.put(it, data) }
            }
            setUpRecyclerView(mapGoalList)
        }

        mCallbackAllUserGoalIds = { data : ArrayList<String?>?, exc : Exception? ->
            if(exc != null){
                alert("current user exc: = ${exc.message}") {
                    okButton {  }
                }.show()
            }
            else{

                data?.let { listOfPin ->

                    if(listOfPin.size > 0){
                        val pins = ArrayList<String>()

                        for(pin in listOfPin){
                            pin?.let {
                                pins.add(pin)
                            }
                        }

                        listOfGoalPins = pins

                        for(pin in listOfPin){
                            // add goal listener
                            val userId = FirebaseAuth.getInstance().uid
                            if(userId != null){
                                if (pin != null) {
                                    GoalModel.addGoalSingleListener(userId, pin, mCallbackCurrentGoal)
                                }
                            }

                        }
                    }
                    else{
                        add_task_to_which_goal_recycler_view.adapter = AddTaskGoalsAdapter(ArrayList(), this)
                        viewModel.numberOfOpenGoals.value = listOfPin.size
                    }
                }?: run {
                    add_task_to_which_goal_recycler_view.adapter = AddTaskGoalsAdapter(ArrayList(), this)
                    viewModel.numberOfOpenGoals.value = 0
                }

            }
        }
    }

    fun setUpRecyclerView(mapGoalList : HashMap<String, Goal>){

        add_task_to_which_goal_recycler_view.also {

        it.layoutManager = LinearLayoutManager(applicationContext)

        // get all the deleted goals
        val deletedOrCompletedGoals = ArrayList<String>()
        for(goal in ArrayList(mapGoalList.values)){
            if(goal.isDeleted != null || goal.isCompleted != null){
                goal.id?.let { it1 -> deletedOrCompletedGoals.add(it1) }
            }
        }

        //remove deleted goals from the list that will go on the UI
        for(deleted in deletedOrCompletedGoals){
            mapGoalList.remove(deleted)
        }

        //sort list by deadline date coming soon

        val sortedListByDeadlineDate = ArrayList(mapGoalList.values)
        viewModel.numberOfOpenGoals.value = sortedListByDeadlineDate.size


        sortedListByDeadlineDate.sortBy { list -> list.goalDateDeadline }
        it.adapter = AddTaskGoalsAdapter(sortedListByDeadlineDate, this)

        }
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            GoalModel.addAllUsersGoalIdsListeners(userId, mCallbackAllUserGoalIds)
        }
    }

    override fun onPause() {
        super.onPause()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            for(pin in listOfGoalPins){
                GoalModel.removeGoalSingleListener(pin, mCallbackCurrentGoal)
            }
        }
    }

    override fun onRecyclerViewItemClick(view: View, any: Any) {
        val mGoal = any is Goal
        if(mGoal){
            val goal = any as Goal
            viewModel.goalSelectedAddTask.value = goal.id
        }
    }
}
