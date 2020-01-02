package com.thetrainingplan.models

import com.google.firebase.firestore.DocumentSnapshot
import java.lang.Exception

class User {
    var name: String? = null
    var email: String

    constructor(name : String, email: String){
        this.email = email
        this.name = name
    }

    @Throws(Exception::class)
    constructor(snapshot: DocumentSnapshot){
        val data: HashMap<String, Any> = snapshot.data as HashMap<String, Any>
        name = data["name"] as String?
        email = data["email"] as String
    }

    // write to database
    fun toMap():HashMap<String, Any?>{
        val map:HashMap<String, Any?> = HashMap()
        map["name"]=name
        map["email"]=email
        return map
    }
}