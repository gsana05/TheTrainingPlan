package com.thetrainingplan.models

import java.lang.Exception

class User {
    var name: String? = null
    var email: String
    //var profileImage: String? = null
    //var profileImageDownloadUrl: String? = null
    var pin: String? = null

    constructor(name : String, email: String){
        this.email = email
        this.name = name
        //this.profileImage = profileImage
        //this.profileImageDownloadUrl = profileImageDownloadUrl
    }


   /* @Throws(Exception::class)
    constructor(snapshot: DocumentSnapshot){
        val data: HashMap<String, Any> = snapshot.data as HashMap<String, Any>
        profileImageDownloadUrl = data["profileImageDownloadUrl"] as String?
        name = data["name"] as String?
        email = data["email"] as String
        profileImage = data["profileImage"] as String?
        pin = data["pin"] as String?
    }*/


    // write to database
    fun toMap():HashMap<String, Any?>{
        val map:HashMap<String, Any?> = HashMap()
        map["name"]=name
        map["email"]=email
        //map["profileImage"]=profileImage
        //map["profileImageDownloadUrl"]= profileImageDownloadUrl
        //map["pin"] =pin
        return map
    }
}