package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.thetrainingplan.models.Goal

class UpdateGoalsViewModel(application : Application) : AndroidViewModel(application) {

    var goal = MutableLiveData<Goal?>()

}