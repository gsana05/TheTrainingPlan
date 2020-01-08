package com.thetrainingplan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.thetrainingplan.models.UserModel
import com.thetrainingplan.util.LiveEvent

class AuthViewModel (application : Application) : AndroidViewModel(application)  {

    val signInEmail =  MutableLiveData<String>()
    val signInPassword =  MutableLiveData<String>()
    var signInIsUserLoggedIn = MutableLiveData<Boolean>()
    var signInExc = MutableLiveData<Exception>()
    var isSigningIn = MutableLiveData<Boolean>().apply { value = false }
    val signInStartCreateAccountActivityEvent = LiveEvent<Void>()
    val joinFinishCreateAccountActivityEvent = LiveEvent<Void>()

    fun signIn(){

        if( signInEmail.value == null || signInPassword.value == null){
            signInIsUserLoggedIn.value = false
            return
        }

        isSigningIn.value = true

        signInEmail.value?.let { email ->
            signInPassword.value?.let { password ->
                UserModel.logIn(email, password){ isUser : Boolean?, exception : Exception? ->

                    if(exception != null){
                        signInExc.value = exception
                    }
                    else{
                        signInIsUserLoggedIn.value = isUser != null && isUser
                    }
                    isSigningIn.value = false
                }
            }
        }

    }

    fun signUp(){

    }

    fun startActivityCreateAccount(){
        signInStartCreateAccountActivityEvent.call()
    }

    fun finishCreateAccountActivity(){
        joinFinishCreateAccountActivityEvent.call()
    }
}