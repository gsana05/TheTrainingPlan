package com.thetrainingplan.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.thetrainingplan.models.User
import com.thetrainingplan.models.UserModel
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.activity_log_in.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class AuthViewModel (application : Application) : AndroidViewModel(application)  {

    val email =  MutableLiveData<String>()
    val password =  MutableLiveData<String>()
    var isLoggedIn = MutableLiveData<Boolean>().apply { value = false }
    var isInputValid = MutableLiveData<Boolean>()
    var logInExc = MutableLiveData<Exception>()
    var isLoggingIn = MutableLiveData<Boolean>().apply { value = false }


    fun signIn(){

        isLoggingIn.value = true

        if( email.value == null){
            isInputValid.value = false
            isLoggingIn.value = false
            return
        }

        if(password.value == null){
            isInputValid.value = false
            isLoggingIn.value = false
            return
        }

        email.value?.let {email ->
            password.value?.let { password ->
                UserModel.logIn(email, password){ isUser : Boolean?, exception : Exception? ->

                    if(exception != null){
                        logInExc.value = exception
                        isLoggingIn.value = false
                    }
                    else{
                        isLoggedIn.value = isUser != null && isUser
                        isLoggingIn.value = true
                    }


                }
            }
        }

    }
}