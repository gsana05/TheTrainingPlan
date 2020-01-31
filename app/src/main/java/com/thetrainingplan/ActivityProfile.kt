package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.databinding.ActivityMainBinding
import com.thetrainingplan.databinding.ActivityProfileBinding
import com.thetrainingplan.models.User
import com.thetrainingplan.models.UserModel
import com.thetrainingplan.viewmodels.MainViewModel
import com.thetrainingplan.viewmodels.ProfileViewModel
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class ActivityProfile : AppCompatActivity() {

    private lateinit var viewModel: ProfileViewModel
    private var mCallbackCurrentUser = { _: User?, _: Exception? -> Unit}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_profile)

        // reference to view model
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        val binding: ActivityProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        mCallbackCurrentUser = { data : User?, exc: Exception? ->
            if(exc != null){
                alert("current user exc: = ${exc.message}") {
                    okButton {  }
                }.show()
            }
            else{

                if(data != null){
                    viewModel.currentProfileUser.value = data
                }

            }
        }


        viewModel.finishProfileActivityEvent.observe(this, Observer {
            finish()
        })
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            UserModel.addCurrentUserListener(userId, mCallbackCurrentUser)
        }
    }

    override fun onPause() {
        super.onPause()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            UserModel.removeCurrentUserListener(userId, mCallbackCurrentUser)
        }
    }
}
