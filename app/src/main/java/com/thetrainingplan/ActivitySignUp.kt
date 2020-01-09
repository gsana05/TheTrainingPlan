package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.thetrainingplan.databinding.ActivitySignUpBinding
import com.thetrainingplan.viewmodels.AuthViewModel
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class ActivitySignUp : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_sign_up)
        // reference to view model
        viewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)

        val binding: ActivitySignUpBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.userException.observe(this, Observer {
            alert ("${it.message}"){
                okButton {  }
            }.show()
        })

        viewModel.isUserJoined.observe(this, Observer {
            if(!it){
                alert(getString(R.string.please_fill_in_all_fields)) {
                    okButton {  }
                }.show()
            }
            else{
                finish()
            }
        })

        viewModel.joinFinishCreateAccountActivityEvent.observe(this, Observer {
            finish()
        })
    }
}
