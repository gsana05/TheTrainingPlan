package com.thetrainingplan

import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.thetrainingplan.databinding.ActivityGoalsBinding
import com.thetrainingplan.databinding.ActivityMainBinding
import com.thetrainingplan.models.GoalTypeSpinner
import com.thetrainingplan.viewmodels.GoalsViewModel
import com.thetrainingplan.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_goals.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import android.view.MotionEvent
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class GoalsActivity : AppCompatActivity() {

    private lateinit var viewModel: GoalsViewModel
    private var selectedControlView : View? = null
    private var mSwitchFilterStyle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_goals)

        // reference to view model
        viewModel = ViewModelProviders.of(this).get(GoalsViewModel::class.java)

        val binding: ActivityGoalsBinding = DataBindingUtil.setContentView(this, R.layout.activity_goals)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

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

                val timeInMillies = c.timeInMillis

                val textView = goals_spinner_goal_date_deadline_input as TextView
                textView.text = SimpleDateFormat.getDateInstance(DateFormat.LONG).format(c.time)

                /*val millionSeconds = timeInMillies - Calendar.getInstance().timeInMillis
                goals_number_of_days.text = TimeUnit.MILLISECONDS.toDays(millionSeconds).toString() + "days"*/

                val cal = Calendar.getInstance()
                //val SimpleDateFormat = SimpleDateFormat("EEE MMM dd hh:mm:ss 'GMT'Z yyyy")
                val now = cal.time
                //val i = SimpleDateFormat.format(cal.time)


                printDifference(now, c.time)


            }, year, month, day)

            dpd.show()
        }

        //disable slide finger on switch
        settings_switch.setOnTouchListener { _, event -> event.actionMasked == MotionEvent.ACTION_MOVE }

        // min deal size
        val listOfGoalTypes = ArrayList<GoalTypeSpinner?>()
        listOfGoalTypes.add(null)
        listOfGoalTypes.add(GoalTypeSpinner(1, "Spiritual"))
        listOfGoalTypes.add(GoalTypeSpinner(2, "Physical"))
        listOfGoalTypes.add(GoalTypeSpinner(3, "Psychology"))
        listOfGoalTypes.add(GoalTypeSpinner(4, "Emotional"))


        val spinnerAdapterMinDealSize = CustomDropDownAdapter<GoalTypeSpinner?>(this, listOfGoalTypes){ _, item, view ->
            val label: TextView = view.findViewById(R.id.txtDropDownLabel) as TextView
            label.text = item?.type ?: "Other"
        }
        goals_spinner_goal_type.adapter = spinnerAdapterMinDealSize

        goals_spinner_goal_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                parent.let {

                    mSwitchFilterStyle = true
                    selectedControlView = goals_spinner_goal_type
                    updateUI()
                }
            }
        }

        updateUI()

    }

    //1 minute = 60 seconds
//1 hour = 60 x 60 = 3600
//1 day = 3600 x 24 = 86400
    fun printDifference(startDate : Date, endDate : Date) {
    //milliseconds
    var different = endDate.time - startDate.time

    val secondsInMilli = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60
    val daysInMilli = hoursInMilli * 24

    val elapsedDays = different / daysInMilli
        different %= daysInMilli

    val elapsedHours = different / hoursInMilli
        different %= hoursInMilli

    val elapsedMinutes = different / minutesInMilli
        different %= minutesInMilli

    val elapsedSeconds = different / secondsInMilli

    System.out.printf(
        "%d days, %d hours, %d minutes, %d seconds%n",
        elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds)


        System.out.printf(
            "%d days, %d hours, %d minutes, %d seconds%n",
            elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds)

        goals_number_of_days.text = getString(R.string.timeUntilGoalDeadline, elapsedDays, elapsedHours)
}

    fun updateUI(){
        // min deal size
        /*goals_spinner_goal_type.setBackgroundResource(if( selectedControlView == goals_spinner_goal_type && mSwitchFilterStyle){R.drawable.gradient_spinner_highlight} else {R.drawable.gradient_spinner})
        goals_spinner_goal_type.setColorFilter(if( selectedControlView == goals_spinner_goal_type  && mSwitchFilterStyle){colGreen} else {colWhite})*/
        goals_spinner_goal_type_arrow.rotation = if( selectedControlView == goals_spinner_goal_type  && mSwitchFilterStyle){0.0f} else {180.0f}
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
