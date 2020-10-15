package com.thetrainingplan

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.databinding.ActivityDiaryBinding
import com.thetrainingplan.databinding.ActivityMainBinding
import com.thetrainingplan.models.AddTask
import com.thetrainingplan.models.AddTaskModel
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.viewmodels.DiaryViewModel
import com.thetrainingplan.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_diary.*
import kotlinx.android.synthetic.main.activity_statistics_board.*
import kotlinx.android.synthetic.main.diary_page_item.*
import kotlinx.android.synthetic.main.diary_page_item.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ActivityDiary : AppCompatActivity() {

    private lateinit var viewModel: DiaryViewModel
    private var mCallbackAllUserGoalIds = { _:ArrayList<String?>?, _: Exception? -> Unit}
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private var callbackForAllGoalTasks = { _:ArrayList<AddTask?>?, _: Exception? -> Unit}
    private val mapGoalList = HashMap<String, Goal>()
    private var listOfGoalPins = ArrayList<String>()
    private var mapOfAllTasks = HashMap<String, AddTask>()
    private val listOfTaskCallbacks = ArrayList<String>()
    var mDiaryEntries: java.util.ArrayList<AddTask>?=null
    val mDiarySize = 730

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_diary)
        // reference to view model
        viewModel = ViewModelProviders.of(this).get(DiaryViewModel::class.java)

        val binding: ActivityDiaryBinding = DataBindingUtil.setContentView(this, R.layout.activity_diary)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        diary_page_item_day_today_btn.setOnClickListener {
            showToday()
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


        //get all the tasks
        mCallbackCurrentGoal = { data : Goal?, _: Exception? ->
            if(data != null){
                data.id?.let {
                    mapGoalList.put(it, data)
                }
            }
            //setUpRecyclerView()
            if(listOfGoalPins.size == mapGoalList.size){ // when we have all goal pins and have used these pins to get the goals then run this code
                val listOfGoals = ArrayList(mapGoalList.values)


                // tasks stats in a goal - remove delete goal stats
                val listOfCompletedOpenGoals = listOfGoals.filter { it.isDeleted == null }
                listOfCompletedOpenGoals.forEach { goal ->
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
                                                mDiaryEntries = ArrayList(mapOfAllTasks.values)
                                                diary_page_list.adapter = DiaryPagerAdaper()
                                                showToday()
                                                /*if(listOfTaskCallbacks.size == mapGoalList.size){ // set the adapter only when we have all tasks from goal
                                                    val endResult = mapOfAllTasks[taskId]
                                                }*/
                                            }
                                        }
                                    }
                                }
                            }



                            //listen to all the tasks for that goal
                            if(!listOfTaskCallbacks.contains(goalId)){
                                listOfTaskCallbacks.add(goalId)
                                AddTaskModel.addAllGoalTaskListeners(userId, goalId, callbackForAllGoalTasks)
                            }
                        }
                    }
                }
            }
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

    fun showToday(){
        diary_page_list.currentItem = mDiarySize/2
    }

    fun getCurrentDate(position: Int) : Calendar{

        val size = mDiarySize/2
        val daysToRemove = size-position

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysToRemove)

        return cal
    }

    inner class DiaryPagerAdaper(): PagerAdapter(){

        override fun getCount(): Int {
            return mDiarySize
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        @SuppressLint("SimpleDateFormat")
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var items = java.util.ArrayList<AddTask>()

            val layout = LayoutInflater.from(this@ActivityDiary).inflate(R.layout.diary_page_item, container, false)

            val cal = getCurrentDate(position)

            val formatDay = SimpleDateFormat("EEEE")

            val dateFormat = DateFormat.getDateInstance(DateFormat.DATE_FIELD)

            val date = Date(cal.timeInMillis)

            val dayPrefix = if(position == (mDiarySize / 2 )) "Today - " else ""
            /*layout.diary_page_item_day.text =  dayPrefix + formatDay.format(date)*/
            layout.diary_page_item_day.text =  dayPrefix + formatDay.format(date)
            layout.diary_page_item_date.text = dateFormat.format(date)




            /*mDiaryEntries?.let {
                items = DiaryModel.filterEventsForDate(it,cal)

                items = DiaryModel.filterDeletedEvents(cal,items)

                layout.diary_page_item_events_list.adapter = DiaryDayItemListAdapter(items)


                layout.diary_page_item_up_arrow.setOnClickListener{
                    scrollEventsList(layout.diary_page_item_events_list, -1)
                }

                layout.diary_page_item_down_arrow.setOnClickListener{
                    scrollEventsList(layout.diary_page_item_events_list, 1)
                }
            }*/


            container.addView(layout)
            return layout
        }

        private fun scrollEventsList(recyclerView: RecyclerView, numPositions: Int) {
            recyclerView.scrollBy(0, (recyclerView.height - 20) * numPositions)
        }

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView(view as View)
        }
    }
}