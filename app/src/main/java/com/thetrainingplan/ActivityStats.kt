package com.thetrainingplan

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.adapters.StatsGoalAdapter
import com.thetrainingplan.databinding.ActivityStatsBinding
import com.thetrainingplan.models.AddTask
import com.thetrainingplan.models.AddTaskModel
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.util.RecyclerViewClickListener
import com.thetrainingplan.viewmodels.StatsViewModel
import kotlinx.android.synthetic.main.activity_stats.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.yesButton
import java.util.*
import kotlin.collections.ArrayList

class ActivityStats : AppCompatActivity(), RecyclerViewClickListener {

    private lateinit var viewModel: StatsViewModel

    private var mCallbackAllUserGoalIds = { _:ArrayList<String?>?, _: Exception? -> Unit}
    private val mapGoalList = HashMap<String, Goal>()
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private var listOfGoalPins = ArrayList<String>()
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



        viewModel.startAllStatsActivityEvent.observe(this, androidx.lifecycle.Observer {
            val intent = Intent(this, ActivityStatisticsBoard::class.java)
            startActivity(intent)
        })


        mCallbackCurrentGoal = { data : Goal?, _: Exception? ->
            if(data != null){
                data.id?.let { mapGoalList.put(it, data) }
            }
            //setUpRecyclerView()
            if(listOfGoalPins.size == mapGoalList.size){

                statistics_heading_individual_recycler_view.also {recyclerView ->

                    val goals = ArrayList(mapGoalList.values).filter { it.isDeleted == null }
                    recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                    recyclerView.adapter = StatsGoalAdapter(ArrayList(goals), this)
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

        mapOfAllTasks.clear()

    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            GoalModel.addAllUsersGoalIdsListeners(userId, mCallbackAllUserGoalIds)
        }
    }

    override fun onRecyclerViewItemClick(view: View, any: Any) {

        val mGoal = any as Goal
        when(view.id){
            R.id.stats_goals_item_button_completed -> {
                val intent = Intent(this, ActivityStatisticsPerGoal::class.java)
                intent.putExtra("goalId", mGoal.id)
                intent.putExtra("userId", mGoal.userId)
                startActivity(intent)
            }
            else -> {
                alert ("Something went wrong"){
                    okButton {  }
                }.show()
            }
        }
    }
}
