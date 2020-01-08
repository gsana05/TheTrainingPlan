package com.thetrainingplan

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.databinding.ActivityLogInBinding
import com.thetrainingplan.viewmodels.AuthViewModel
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class ActivityLogIn : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_log_in)

        // reference to view model
        viewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)

        val binding: ActivityLogInBinding = DataBindingUtil.setContentView(this, R.layout.activity_log_in)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.isUserLoggedIn.observe(this, Observer {
            if(!it){
                alert ("Please enter email and password"){
                    okButton {  }
                }.show()
            }
            else{
                dismissKeyboard()
                finish()
            }
        })

        viewModel.logInExc.observe(this, Observer {
            alert ("${it.message}"){
                okButton { }
            }.show()
            dismissKeyboard()
        })

    }

    private fun dismissKeyboard(){
        val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.SHOW_FORCED)
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            finish()
        }
    }
}
