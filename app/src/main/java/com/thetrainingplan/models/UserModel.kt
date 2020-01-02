package com.thetrainingplan.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

object UserModel {

    val currentListOfUser = MutableLiveData<ArrayList<User>>().apply { value = null }

    fun getData() : ArrayList<User>{
        val list = ArrayList<User>()
        list.add(User("Gareth", "sanashee05@hotmail.com"))
        list.add(User("John", "ronaldo@hotmail.com"))
        currentListOfUser.value = list
        return list
    }

    fun logIn(email : String, password : String, callback : (Boolean?, Exception?) -> Unit){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    callback(true, null)
                }
                else{
                    callback(false, it.exception)
                }
            }


    }

    fun signUp(name : String, email : String, password : String, callback : (Boolean?, Exception?) -> Unit){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    saveSignUpDetails(name, email, callback)
                }
                else{
                    callback(false, it.exception)
                }
            }
    }

    private fun saveSignUpDetails(name : String, email : String, callback : (Boolean?, Exception?) -> Unit){
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            val user = User(name, email)
            getDatabaseRef().document(userId).set(user.toMap()).addOnCompleteListener {
                if(it.isSuccessful){
                    callback(true, null)
                }
                else{
                    callback(false, it.exception)
                }
            }
        }
        else{
            callback(false, Exception("No user auth found"))
        }
    }

    private fun getDatabaseRef(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("users")
    }

}