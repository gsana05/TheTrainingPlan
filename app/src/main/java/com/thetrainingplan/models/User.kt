package com.thetrainingplan.models

import com.google.firebase.firestore.DocumentSnapshot
import java.lang.Exception

class User {
    var userId : String
    var name: String? = null
    var email: String? = null

    constructor(userId : String, name : String, email: String){
        this.userId = userId
        this.email = email
        this.name = name
    }

    @Throws(Exception::class)
    constructor(snapshot: DocumentSnapshot){
        val data: HashMap<String, Any> = snapshot.data as HashMap<String, Any>
        userId = data["userId"] as String
        name = data["name"] as String?
        email = data["email"] as String?
    }

    // write to database
    fun toMap():HashMap<String, Any?>{
        val map:HashMap<String, Any?> = HashMap()
        map["userId"]=userId
        map["name"]=name
        map["email"]=email
        return map
    }
}