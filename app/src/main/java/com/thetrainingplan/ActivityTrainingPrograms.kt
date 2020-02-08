package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.thetrainingplan.adapters.TrainingProgramsAdapter
import com.thetrainingplan.databinding.ActivityTrainingProgramsBinding
import com.thetrainingplan.models.User
import com.thetrainingplan.viewmodels.TrainingProgramsViewModel
import kotlinx.android.synthetic.main.activity_training_programs.*

class ActivityTrainingPrograms : AppCompatActivity() {

    private lateinit var viewModel: TrainingProgramsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_training_programs)

        // reference to view model
        viewModel = ViewModelProviders.of(this).get(TrainingProgramsViewModel::class.java)

        val binding: ActivityTrainingProgramsBinding = DataBindingUtil.setContentView(this, R.layout.activity_training_programs)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        training_programs_view_pager.adapter = TrainingProgramsAdapter()
    }

    fun getData() : ArrayList<User>{
        val list = ArrayList<User>()
        list.add(User("abc", "John", "email john"))
        list.add(User("efg", "Paul", "email Paul"))
        return list
    }
}
