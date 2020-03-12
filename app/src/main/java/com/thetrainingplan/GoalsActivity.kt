package com.thetrainingplan

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
import com.thetrainingplan.viewmodels.GoalsViewModel
import com.thetrainingplan.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_goals.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

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

        // min deal size
        val listMinDealSize = listOf(null, 10000, 20000, 40000, 80000, 160000)
        val spinnerAdapterMinDealSize = CustomDropDownAdapter<Int?>(this, listMinDealSize){ _, item, view ->
            val label: TextView = view.findViewById(R.id.txtDropDownLabel) as TextView

            if(item == null){
                label.text = "ALL"
            }
            else {
                val formatCurrency  = U.formatCurrencyShort(item.toDouble())
                label.text = formatCurrency
            }
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
