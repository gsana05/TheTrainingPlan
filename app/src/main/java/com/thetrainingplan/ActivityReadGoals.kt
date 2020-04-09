package com.thetrainingplan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.adapters.GoalsAdapter
import com.thetrainingplan.databinding.ActivityReadGoalsBinding
import com.thetrainingplan.databinding.FragmentFragmentReadGoalsBinding
import com.thetrainingplan.models.Goal
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.models.User
import com.thetrainingplan.models.UserModel
import com.thetrainingplan.util.RecyclerViewClickListener
import com.thetrainingplan.viewmodels.GoalsViewModel
import kotlinx.android.synthetic.main.activity_read_goals.*
import kotlinx.android.synthetic.main.fragment_fragment_read_goals.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.yesButton


@Suppress("DEPRECATION")
class ActivityReadGoals : AppCompatActivity(), RecyclerViewClickListener {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var viewModel: GoalsViewModel

    private var mCallbackCurrentUser = { _: User?, _: Exception? -> Unit}
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private var listOfGoalPins = ArrayList<String>()
    private val mapGoalList = HashMap<String, Goal>()

    private var localTracker : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_read_goals)

        // reference to view model
        viewModel = ViewModelProviders.of(this).get(GoalsViewModel::class.java)

        val binding: ActivityReadGoalsBinding = DataBindingUtil.setContentView(this, R.layout.activity_read_goals)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        view_pager.adapter = mSectionsPagerAdapter

        view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))

        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(view_pager))

        tabs.setOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                //view_pager.currentItem = tab.position
                //viewModel.tabTracker.value = tab.position
                //fragment_read_goals_text_view.text = tab.position.toString()
                localTracker = tab.position
                setUpRecyclerView()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }

        })

        mCallbackCurrentGoal = { data : Goal?, _: Exception? ->
            if(data != null){
                data.id?.let { mapGoalList.put(it, data) }
            }
            setUpRecyclerView()
        }

        mCallbackCurrentUser = { data : User?, exc: Exception? ->
            if(exc != null){
                alert("current user exc: = ${exc.message}") {
                    okButton {  }
                }.show()
            }
            else{

                if(data != null){
                    data.goals?.let { listOfPin ->

                        if(listOfPin.size > 0){
                            for(pin in listOfPin){
                                //clear cache - when goals have been removed
                                val listOfCacheGoals = ArrayList(mapGoalList.values)
                                for(i in listOfCacheGoals){
                                    if(!listOfPin.contains(i.id)){ // it needs to match listOfPin as that is fresh from database
                                        mapGoalList.remove(i.id)
                                    }
                                }


                                // add goal listener
                                GoalModel.addGoalSingleListener(pin, mCallbackCurrentGoal)
                            }
                            listOfGoalPins = listOfPin
                        }
                        else{
                            //remove listeners - if all goals have been deleted
                            for(pin in listOfPin){
                                GoalModel.removeGoalSingleListener(pin, mCallbackCurrentGoal)
                            }

                            // remove from local cache
                            mapGoalList.clear()

                            //update recycle view
                            setUpRecyclerView()
                        }


                    }
                }
            }
        }
    }

    private fun setUpRecyclerView(){
        fragment_read_goals_recycler_view.also {
            it.layoutManager = LinearLayoutManager(this)

          /*  // get all the deleted goals
            val deletedOrCompletedGoals = ArrayList<String>()
            for(goal in ArrayList(mapGoalList.values)){
                if(goal.isDeleted == true || goal.isCompleted == true){
                    goal.id?.let { it1 -> deletedOrCompletedGoals.add(it1) }
                }
            }

            //remove deleted goals from the list that will go on the UI
            for(deleted in deletedOrCompletedGoals){
                mapGoalList.remove(deleted)
            }*/

            //sort list by deadline date coming soon

            val listOfGoals = ArrayList(mapGoalList.values)
            val filteredListOfGoals = ArrayList<Goal>()
            //viewModel.numberOfOpenGoals.value = sortedListByDeadlineDate.size

            // plurals for goals or goal
            /*val item = resources.getQuantityString(R.plurals.numberOfGoals, sortedListByDeadlineDate.size, sortedListByDeadlineDate.size)
            view_goals_recycler_view_heading.text = item*/

            when(localTracker){
                0 -> {
                    listOfGoals.forEach {
                        if((it.isCompleted == null) && (it.isDeleted ==null)){
                            filteredListOfGoals.add(it)
                        }
                    }
                }
                1 -> {
                    listOfGoals.forEach {
                        if((it.isCompleted != null) && (it.isDeleted ==null)){
                            filteredListOfGoals.add(it)
                        }
                    }
                }
                2 -> {
                    listOfGoals.forEach {
                        if(it.isDeleted != null){
                            filteredListOfGoals.add(it)
                        }
                    }
                }
                else -> {

                }
            }

            //sortedListByDeadlineDate.sortBy { list -> list.goalDateDeadline }
            it.adapter = GoalsAdapter(filteredListOfGoals, this)
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            UserModel.addCurrentUserListener(userId, mCallbackCurrentUser)
        }
    }


    override fun onPause() {
        super.onPause()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            UserModel.removeCurrentUserListener(userId, mCallbackCurrentUser)

            for(pin in listOfGoalPins){
                GoalModel.removeGoalSingleListener(pin, mCallbackCurrentGoal)
            }
        }
    }

    @Suppress("DEPRECATION")
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1)
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }

    override fun onRecyclerViewItemClick(view: View, any: Any) {
        val mGoal = any as Goal
        when(view.id){
            R.id.goals_item_button -> {

                val intent = Intent(this, ActivityUpdateGoals::class.java)
                intent.putExtra("id", mGoal.id)
                startActivity(intent)

            }
            R.id.goals_item_button_completed -> {
                alert ("Excellent! Press OK to confirm you have completed your goal"){
                    yesButton {
                        mGoal.id?.let { it1 ->
                            viewModel.completedGoal(it1){data : Boolean?, exc : Exception? ->
                                if(data != null && data){

                                    alert ("Goal has been completed"){
                                        okButton {  }
                                    }.show()
                                }
                                else{
                                    alert ("Goal has NOT been been completed. Please try again later"){
                                        okButton {  }
                                    }.show()
                                }

                            }
                        }
                    }
                    noButton {

                    }
                }.show()
            }
            R.id.goals_item_button_completed_re_open -> {
                alert ("Would you like to re-open this goal?"){
                    positiveButton("Yes"){
                        mGoal.id?.let { it1 ->
                            viewModel.reOpenGoal(it1){data : Boolean?, exc : Exception? ->
                                if(data != null && data){

                                    alert ("Goal has been re-opened"){
                                        okButton {  }
                                    }.show()
                                }
                                else{
                                    alert ("Goal has NOT been been re-opened. Please try again later"){
                                        okButton {  }
                                    }.show()
                                }

                            }
                        }
                    }
                    negativeButton("No"){

                    }
                }.show()
            }
            R.id.goals_item_button_delete_goal_perm -> {
                alert ("Would you like to permanently delete this goal?"){
                    positiveButton("Yes"){
                        mGoal.id?.let { goalId ->
                            val userId = FirebaseAuth.getInstance().uid
                            userId?.let {userId ->
                                viewModel.permanentlyDeleteGoalPin(userId, goalId){data : Boolean?, exc : Exception? ->
                                    if(data != null && data){
                                        mapGoalList.remove(goalId)
                                        viewModel.permanentlyDeleteGoal(userId, goalId){data : Boolean?, exc : Exception? ->
                                            if(data != null && data){

                                                //remove listener for deleted goal
                                                GoalModel.removeGoalSingleListener(goalId, mCallbackCurrentGoal)
                                                alert ("Goal has been deleted"){
                                                    okButton {  }
                                                }.show()
                                            }
                                            else{
                                                alert ("Goal has NOT been deleted"){
                                                    okButton {  }
                                                }.show()
                                            }


                                        }
                                    }
                                    else{
                                        alert ("Unable to delete goal from database"){
                                            okButton {  }
                                        }.show()
                                    }
                                }
                            }

                        }
                    }
                    negativeButton("No"){

                    }
                }.show()
            }
            R.id.goals_item_button_share_goal_completed -> {

                val goalAchieved = "Hey, I want to share my achievement with you. I have achieved my goal which was:\n\n ${mGoal.goal}\n\n" +
                        "I set this goal on the ${mGoal.goalSetDate?.let { U.formatDate(it, false) }} and I completed on ${mGoal.isCompleted?.let {
                            U.formatDate(
                                it, false)
                        }} "

                shareGoal(goalAchieved)
            }
            R.id.goals_item_button_share_open_goal -> {
                val goalAchieved = "Hey, I want to share my goal with you. This is the goal I have set:\n\n ${mGoal.goal}\n\n" +
                        "I set this goal on the ${mGoal.goalSetDate?.let { U.formatDate(it, false) }} and I aim to achieve it by ${mGoal.goalDateDeadline?.let {
                            U.formatDate(
                                it, false)
                        }} "

                shareGoal(goalAchieved)
            }
            else -> {
                alert ("Something went wrong"){
                    okButton {  }
                }.show()
            }
        }
    }

    private fun shareGoal(goal : String){
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.putExtra(Intent.EXTRA_TEXT, goal)
        //share.setPackage("com.whatsapp")
        startActivity(
            Intent.createChooser(
                share,
                "Goal Achieved"
            )
        )
    }
}

class PlaceholderFragment : Fragment() {

    private lateinit var viewModel: GoalsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewModel = ViewModelProviders.of(activity!!).get(GoalsViewModel::class.java)

        val binding: FragmentFragmentReadGoalsBinding = FragmentFragmentReadGoalsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        if(arguments?.getInt(ARG_SECTION_NUMBER) == 1){
            val openGoals = binding.root.context.resources.getString(R.string.open_goals)
            //viewModel.tabTracker.value = arguments?.getInt(ARG_SECTION_NUMBER)
            binding.root.fragment_read_goals_text_view.text = openGoals
        }

        if(arguments?.getInt(ARG_SECTION_NUMBER) == 2){
            val completedGoals = binding.root.context.resources.getString(R.string.completed_goals)
            //viewModel.tabTracker.value = arguments?.getInt(ARG_SECTION_NUMBER)
            binding.root.fragment_read_goals_text_view.text = completedGoals
        }

        if(arguments?.getInt(ARG_SECTION_NUMBER) == 3){
            val deletedGoals = binding.root.context.resources.getString(R.string.deleted_goals)
            //viewModel.tabTracker.value = arguments?.getInt(ARG_SECTION_NUMBER)
            binding.root.fragment_read_goals_text_view.text = deletedGoals
        }

        val text = binding.root.fragment_read_goals_text_view.text.trim().toString()

        return binding.root
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            val fragment = PlaceholderFragment()
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }
}
