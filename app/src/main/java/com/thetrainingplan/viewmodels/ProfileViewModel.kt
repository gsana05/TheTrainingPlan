package com.thetrainingplan.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.models.User
import com.thetrainingplan.models.UserModel

class ProfileViewModel(application : Application) : AndroidViewModel(application) {

    val currentProfileUser = MutableLiveData<User>()

    fun updateUserProfile(){
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            currentProfileUser.value?.let {
                UserModel.updateUserProfile(userId, it){ data : Boolean?, exc : Exception? ->
                    if(data != null && data){
                        Log.v("Tag", "testing")
                    }
                }
            }
        }
    }
}