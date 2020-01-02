package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thetrainingplan.models.UserModel
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class ActivitySignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sign_up_button.setOnClickListener {
            doSignUp()
        }

        sign_up_cancel_btn.setOnClickListener {
            finish()
        }
    }

    private fun doSignUp(){

        val name = sign_up_name_input.text.toString().trim()
        val email = sign_up_email_input.text.toString().trim()
        val password = sign_up_password_input.text.toString().trim()

        if(name.isEmpty()){
            sign_up_name_input.error ="Please enter name"
            sign_up_name_input.requestFocus()
            return
        }

        if(email.isEmpty()){
            sign_up_email_input.error ="Please enter name"
            sign_up_email_input.requestFocus()
            return
        }

        if(password.isEmpty()){
            sign_up_password_input.error ="Please enter name"
            sign_up_password_input.requestFocus()
            return
        }


        UserModel.signUp(name, email, password){ isSignedUp : Boolean?, exception : Exception? ->
            if(isSignedUp != null && isSignedUp){
                finish()
            }
            else{
                exception?.let{
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
}
