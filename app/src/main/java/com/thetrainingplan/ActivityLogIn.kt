package com.thetrainingplan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.models.UserModel
import kotlinx.android.synthetic.main.activity_log_in.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class ActivityLogIn : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)


        log_in_button.setOnClickListener {
            doLogIn()
        }

        log_in_create_account_btn.setOnClickListener {
            startActivity(Intent(this, ActivitySignUp::class.java))
        }

    }

    private fun doLogIn(){

        val email = log_in_email_input.text.toString().trim()
        val password = log_in_password_input.text.toString().trim()

        if(email.isEmpty()){
            log_in_email_input.error = "enter email"
            log_in_email_input.requestFocus()
            return
        }

        if(password.isEmpty()){
            log_in_password_input.error = "enter password"
            log_in_password_input.requestFocus()
            return
        }

        UserModel.logIn(email, password){  isUser : Boolean?, exception : Exception? ->
            if(isUser != null && isUser){
                finish()
            }
            else{
                exception?.let {
                    alert ("${it.message}"){
                        okButton {  }
                    }.show()
                }?:run{
                    alert ("Unable to log in"){
                        okButton {  }
                    }.show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            finish()
        }
    }
}
