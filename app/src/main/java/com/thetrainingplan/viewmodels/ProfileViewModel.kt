package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.models.User
import com.thetrainingplan.models.UserModel
import com.thetrainingplan.util.LiveEvent

class ProfileViewModel(application : Application) : AndroidViewModel(application) {

    val currentProfileUser = MutableLiveData<User>()
    val isUpdatingUserProfile = MutableLiveData<Boolean>().apply { value = false }
    val finishProfileActivityEvent = LiveEvent<Void>()
    val logOutActivityEvent = LiveEvent<Void>()
    val isLoggingOut = MutableLiveData<Boolean>().apply { value = false }


    fun updateUserProfile(){
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            isUpdatingUserProfile.value = true
            currentProfileUser.value?.let {
                UserModel.updateUserProfile(userId, it){ data : Boolean?, _ : Exception? ->
                    if(data != null){
                        isUpdatingUserProfile.value = false
                        finishProfileActivity()
                    }
                }
            }?:run{
                isUpdatingUserProfile.value = false
            }
        }
    }

    fun logOut(){
        isLoggingOut.value = true
        logOutActivityEvent.call()
    }



    fun finishProfileActivity(){
        finishProfileActivityEvent.call()
    }
}