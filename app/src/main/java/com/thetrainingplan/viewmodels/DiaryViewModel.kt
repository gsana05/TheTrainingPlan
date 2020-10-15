package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class DiaryViewModel (application : Application) : AndroidViewModel(application){
    val name =  MutableLiveData<String>()
}