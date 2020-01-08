package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.thetrainingplan.models.UserModel

class AuthViewModel (application : Application) : AndroidViewModel(application)  {

    val email =  MutableLiveData<String>()
    val password =  MutableLiveData<String>()
    var isUserLoggedIn = MutableLiveData<Boolean>()
    var logInExc = MutableLiveData<Exception>()
    var isLoggingIn = MutableLiveData<Boolean>().apply { value = false }

    fun signIn(){

        if( email.value == null || password.value == null){
            isUserLoggedIn.value = false
            return
        }

        isLoggingIn.value = true

        email.value?.let {email ->
            password.value?.let { password ->
                UserModel.logIn(email, password){ isUser : Boolean?, exception : Exception? ->

                    if(exception != null){
                        logInExc.value = exception
                    }
                    else{
                        isUserLoggedIn.value = isUser != null && isUser
                    }
                    isLoggingIn.value = false
                }
            }
        }

    }
}