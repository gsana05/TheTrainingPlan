package com.thetrainingplan

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.thetrainingplan.databinding.FragmentCrudGoalBinding
import com.thetrainingplan.models.GoalModel
import com.thetrainingplan.models.GoalTypeSpinner
import com.thetrainingplan.viewmodels.GoalsViewModel

class CrudGoalFragment : Fragment() {

    private lateinit var viewModel: GoalsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        viewModel = ViewModelProviders.of(activity!!).get(GoalsViewModel::class.java)

        val binding: FragmentCrudGoalBinding = FragmentCrudGoalBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.goalsAddGoalDateInput.isEnabled = false

        val listOfGoalTypes = ArrayList<GoalTypeSpinner?>()
        listOfGoalTypes.add(null)
        listOfGoalTypes.add(GoalTypeSpinner(GoalModel.SPIRITUAL_ID, GoalModel.SPIRITUAL))
        listOfGoalTypes.add(GoalTypeSpinner(GoalModel.PHYSICAL_ID, GoalModel.PHYSICAL))
        listOfGoalTypes.add(GoalTypeSpinner(GoalModel.PSYCHOLOGY_ID, GoalModel.PSYCHOLOGY))
        listOfGoalTypes.add(GoalTypeSpinner(GoalModel.EMOTIONAL_ID, GoalModel.EMOTIONAL))

        val spinnerAdapterMinDealSize = CustomDropDownAdapter<GoalTypeSpinner?>(inflater.context, listOfGoalTypes) { _, item, view ->
            val label: TextView = view.findViewById(R.id.txtDropDownLabel) as TextView
            label.text = item?.type ?: "Other"
        }

        binding.fragmentGoalsSpinnerGoalType.adapter = spinnerAdapterMinDealSize

        binding.fragmentGoalsSpinnerGoalType.onItemSelectedListener= object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                parent?.let {
                    viewModel.spinnerPosition.value = position
                }
            }
        }

        viewModel.goal.observe(this, Observer {
            it?.goalType?.let {
                binding.fragmentGoalsSpinnerGoalType.setSelection(it)
            }
        })

        return binding.root

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
