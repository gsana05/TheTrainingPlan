package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.thetrainingplan.databinding.ActivityEnrollTrainingProgramBinding
import com.thetrainingplan.databinding.ActivityMainBinding
import com.thetrainingplan.viewmodels.EnrollViewModel
import com.thetrainingplan.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_enroll_training_program.*

class ActivityEnrollTrainingProgram : AppCompatActivity() {

    private lateinit var viewModel: EnrollViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_enroll_training_program)

        // reference to view model
        viewModel = ViewModelProviders.of(this).get(EnrollViewModel::class.java)

        val binding: ActivityEnrollTrainingProgramBinding = DataBindingUtil.setContentView(this, R.layout.activity_enroll_training_program)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        enroll_card_view_strength_size_description.loadUrl("file:///android_asset/strengthAndSizeDetails.html")
        enroll_card_view_fat_burner_description.loadUrl("file:///android_asset/fatLoss.html")
    }
}
