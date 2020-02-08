package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.thetrainingplan.models.User

class TrainingProgramsViewModel (application : Application) : AndroidViewModel(application)  {

    fun getData() : List<User>{
        val list = ArrayList<User>()
        list.add(User("abc", "John", "email john"))
        list.add(User("efg", "Paul", "email Paul"))
        return list
    }

}