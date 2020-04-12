package com.thetrainingplan

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.adapters.GoalsAdapter
import com.thetrainingplan.databinding.ActivityGoalsBinding
import com.thetrainingplan.models.*
import com.thetrainingplan.util.RecyclerViewClickListener
import com.thetrainingplan.viewmodels.GoalsViewModel
import kotlinx.android.synthetic.main.activity_goals.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.yesButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GoalsActivity : AppCompatActivity(), RecyclerViewClickListener {

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
                        val userId = FirebaseAuth.getInstance().uid
                        if(userId != null){
                            mGoal.id?.let { it1 -> viewModel.completedGoal(userId, it1){data : Boolean?, _ : Exception? ->
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

                            } }
                        }
                    }
                    noButton {

                    }
                }.show()
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

    private lateinit var viewModel: GoalsViewModel
    private var mCallbackCurrentUser = { _: User?, _: Exception? -> Unit}
    private var mCallbackCurrentGoal = { _: Goal?, _: Exception? -> Unit}
    private var listOfGoalPins = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_goals)

        // reference to view model
        viewModel = ViewModelProviders.of(this).get(GoalsViewModel::class.java)

        val binding: ActivityGoalsBinding = DataBindingUtil.setContentView(this, R.layout.activity_goals)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.showAlert.observe(this, Observer {
            if(it){
                alert ("fill in all fields"){
                    okButton {  }
                }.show()
            }
        })

        viewModel.saveGoalException.observe(this, Observer {
            if(it != null){
                alert ("$it"){
                    okButton {  }
                }.show()
            }
        })

        viewModel.hasGoalSavedToDatabase.observe(this, Observer {
            if(it){
                settings_switch.isChecked = true
                val dialog = alert ("Goal has been saved successfully"){
                    okButton {}
                }.show()
                dialog.setCancelable(false)
            }
        })

        val mapGoalList = HashMap<String, Goal>()
        mCallbackCurrentGoal = { data : Goal?, _: Exception? ->
            if(data != null){
                data.id?.let { mapGoalList.put(it, data) }
            }

            view_goals_recycler_view.also {
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

                // plurals for goals or goal
                val item = resources.getQuantityString(R.plurals.numberOfGoals, sortedListByDeadlineDate.size, sortedListByDeadlineDate.size)
                view_goals_recycler_view_heading.text = item

                    sortedListByDeadlineDate.sortBy { list -> list.goalDateDeadline }
                it.adapter = GoalsAdapter(sortedListByDeadlineDate, this)
            }
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
                            listOfGoalPins = listOfPin
                            for(pin in listOfPin){
                                // add goal listener
                                val userId = FirebaseAuth.getInstance().uid
                                if(userId != null){
                                    GoalModel.addGoalSingleListener(userId, pin, mCallbackCurrentGoal)
                                }

                            }
                        }
                        else{
                            view_goals_recycler_view.adapter = GoalsAdapter(ArrayList(), this)
                            viewModel.numberOfOpenGoals.value = listOfPin.size
                        }
                    }?: run {
                        view_goals_recycler_view.adapter = GoalsAdapter(ArrayList(), this)
                        viewModel.numberOfOpenGoals.value = 0
                    }
                }
            }
        }



        // show a date picker
        goals_spinner_goal_date_deadline_input.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, yearDP, monthOfYear, dayOfMonth ->
                // Display Selected date in text box
                c.set(Calendar.YEAR, yearDP)
                c.set(Calendar.MONTH, monthOfYear)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                viewModel.dateGoalDeadlineInMillie.value = c.timeInMillis
                viewModel.dateGoalDeadline.value = SimpleDateFormat.getDateInstance(DateFormat.LONG).format(c.time)


                viewModel.printDifference(Calendar.getInstance().time, c.time)

            }, year, month, day)

            dpd.show()
        }

        //disable slide finger on switch
        settings_switch.setOnTouchListener { _, event -> event.actionMasked == MotionEvent.ACTION_MOVE }

        // min deal size
        val listOfGoalTypes = ArrayList<GoalTypeSpinner?>()
        listOfGoalTypes.add(null)
        listOfGoalTypes.add(GoalTypeSpinner(GoalModel.SPIRITUAL_ID, GoalModel.SPIRITUAL))
        listOfGoalTypes.add(GoalTypeSpinner(GoalModel.PHYSICAL_ID, GoalModel.PHYSICAL))
        listOfGoalTypes.add(GoalTypeSpinner(GoalModel.PSYCHOLOGY_ID, GoalModel.PSYCHOLOGY))
        listOfGoalTypes.add(GoalTypeSpinner(GoalModel.EMOTIONAL_ID, GoalModel.EMOTIONAL))


        val spinnerAdapterMinDealSize = CustomDropDownAdapter<GoalTypeSpinner?>(this, listOfGoalTypes){ _, item, view ->
            val label: TextView = view.findViewById(R.id.txtDropDownLabel) as TextView
            label.text = item?.type ?: "Other"
        }
        goals_spinner_goal_type.adapter = spinnerAdapterMinDealSize

        goals_spinner_goal_type.onItemSelectedListener= object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                parent?.let {
                    viewModel.spinnerPosition.value = position
                }
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


/*    private fun notificationAlert(notificationTitle : String?, notificationBody : String?, notificationLink : String?){
        val builder = AlertDialog.Builder(this)
        val viewGroup = findViewById<View>(android.R.id.content) as ViewGroup
        val inflatedLayout: View = layoutInflater.inflate(R.layout.activity_notification_alert, viewGroup, false)
        builder.setView(inflatedLayout)

        val dialog = builder.show()

        val title: TextView = inflatedLayout.findViewById(R.id.notification_alert_title)
        notificationTitle?.let {
            title.text = it
        }

        val body: TextView = inflatedLayout.findViewById(R.id.notification_alert_body)
        notificationBody?.let {
            body.text = it
        }

        val link: Button = inflatedLayout.findViewById(R.id.notification_alert_link_button)
        if(notificationLink != null){
            link.visibility = View.VISIBLE
            link.setOnClickListener {
                goToUrl(notificationLink)
            }
        }
        else{
            link.visibility = View.GONE
        }

        val dismissBtn: Button = inflatedLayout.findViewById(R.id.notification_alert_dismiss_button)
        dismissBtn.setOnClickListener {
            dialog.dismiss()
        }
    }*/

  /*  private fun goToUrl(url: String){
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }*/

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

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            UserModel.addCurrentUserListener(userId, mCallbackCurrentUser)
        }
    }

    // custom spinner that takes null values
    class CustomDropDownAdapter<T>(context: Context, listItemsTxt: List<T?>, var bindView: (position: Int, item: T?, view: View) -> Unit) : ArrayAdapter<T>(context, -1, listItemsTxt) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getViewLayout(position, convertView , R.layout.custom_spinner_view)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getViewLayout(position, convertView , R.layout.custom_spinner_view_selected)
        }

        private fun getViewLayout(position: Int, convertView: View?, @LayoutRes layout : Int) : View{
            val view = convertView ?: LayoutInflater.from(context).inflate(layout, null)
            bindView(position, getItem(position), view)
            return view
        }
    }
}
