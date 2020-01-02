package com.thetrainingplan.models

import androidx.lifecycle.MutableLiveData

object UserModel {

    val currentListOfUser = MutableLiveData<ArrayList<User>>().apply { value = null }

    fun getData() : ArrayList<User>{
        val list = ArrayList<User>()
        list.add(User("Gareth", "sanashee05@hotmail.com"))
        list.add(User("John", "ronaldo@hotmail.com"))
        currentListOfUser.value = list
        return list
    }

}