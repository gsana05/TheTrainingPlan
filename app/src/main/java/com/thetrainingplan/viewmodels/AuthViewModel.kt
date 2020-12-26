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

    var joinName = MutableLiveData<String>()
    var joinEmail = MutableLiveData<String>()
    var joinPassword = MutableLiveData<String>()
    var isUserJoined = MutableLiveData<Boolean>()
    var userException = MutableLiveData<Exception>()
    var isJoiningProgress = MutableLiveData<Boolean>().apply { value = false }

    fun joinAndSignIn(){

        val name = joinName.value
        val email =  joinEmail.value
        val password = joinPassword.value

        if(name == null || email == null || password == null){
            isUserJoined.value = false
            return
        }

        if(password.length < 6){
            isUserJoined.value = false
            return
        }

        isJoiningProgress.value = true

        UserModel.signUp(name, email, password){ isSignedUp : Boolean?, exception : Exception? ->

            if(exception != null && isSignedUp != null && isSignedUp){ // auth created but user object not saved - let user log in
                userException.value = exception
                isUserJoined.value = isSignedUp
            }
            else if(exception != null){ // auth not created
                userException.value = exception
            }
            else{ // successful auth and saved user object
                isUserJoined.value = isSignedUp != null && isSignedUp
            }

            isJoiningProgress.value = false
        }

    }

    val signInStartCreateAccountActivityEvent = LiveEvent<Void>()
    val joinFinishCreateAccountActivityEvent = LiveEvent<Void>()

    fun startActivityCreateAccount(){
        signInStartCreateAccountActivityEvent.call()
    }

    fun finishCreateAccountActivity(){
        joinFinishCreateAccountActivityEvent.call()
    }

}