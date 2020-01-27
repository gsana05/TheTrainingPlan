package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.thetrainingplan.models.User

class ProfileViewModel(application : Application) : AndroidViewModel(application) {

    val currentProfileUser = MutableLiveData<User>()




}