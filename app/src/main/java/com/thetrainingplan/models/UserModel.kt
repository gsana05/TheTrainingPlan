package com.thetrainingplan.models

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object UserModel {

    private val mCurrentUserFirebaseRef : HashMap<String, ListenerRegistration> = HashMap()
    private val mCurrentUserCache : HashMap<String, User> = HashMap()
    private val mCurrentUserCallbacks = HashMap<String, ArrayList<(User?, Exception?) -> Unit>>()

    fun clearCache(){
        mCurrentUserCache.clear()
        mCachedAllUsers.clear()
    }

    fun addCurrentUserListener(userId : String, onComplete: (User?, Exception?) -> Unit){

        var callbacks = ArrayList<(User?,Exception?)->Unit>()

        if(mCurrentUserCallbacks.containsKey(userId)){
            mCurrentUserCallbacks[userId]?.let {
                callbacks = it // This is what it means "callbacks = mProfileCallbacks[userId]"
            }
        }
        else{
            callbacks = ArrayList()
        }

        if (!callbacks.contains(onComplete)){ // if it does NOT have a callback add one // adds callback to the cached user data
            callbacks.add(onComplete)
            mCurrentUserCallbacks[userId] = callbacks
        }

        if(mCurrentUserCache.containsKey(userId)){ // if user data has been cached then return with this already saved user data
            val profile = mCurrentUserCache[userId]
            onComplete(profile, null) //RETURN DATA
        }

        if(!mCurrentUserFirebaseRef.containsKey(userId)){ // if the user has not been cached get the data from database
            val ref = getDatabaseRef().document(userId).addSnapshotListener{ documentSnapshot, firebaseFirestoreException ->

                if(mCurrentUserCallbacks.containsKey(userId)){ // has a valid callback

                    val callbackList = mCurrentUserCallbacks[userId] // gets the list of users - will have atleast one

                    // START - GET USER AND CACHE //
                    var profile : User? = null
                    try {
                        if(documentSnapshot != null){
                            profile = User(documentSnapshot) // gets data from database
                        }
                    }
                    catch(e : Exception){}
                    if(profile!=null) { // caches the data
                        this.mCurrentUserCache[userId] = profile
                    }
                    // END - GET USER AND CACHE //

                    if(callbackList!=null) {
                        for (callback in callbackList) {
                            onComplete(profile, firebaseFirestoreException) //RETURN DATA
                        }
                    }
                }
            }
            mCurrentUserFirebaseRef[userId] = ref // add snapshot listeners to database
        }
    }


    fun removeCurrentUserListener(userId : String, onComplete: (User?, Exception?) -> Unit){
        val callbackList = mCurrentUserCallbacks[userId] // gets the list of callbacks for that user which was added in addListener

        if(callbackList != null && callbackList.contains(onComplete)){
            callbackList.remove(onComplete) // removes one call back at a time

            if(callbackList.size == 0){
                mCurrentUserCallbacks.remove(userId) // remove the list from the cache
                val databaseRef = mCurrentUserFirebaseRef[userId] // get value which is the listener
                if(databaseRef!=null){
                    databaseRef.remove() // remove the snapshot lister // change listener to unit
                    mCurrentUserFirebaseRef.remove(userId) // remove the ref from our cached list
                }
            }
            else {
                mCurrentUserCallbacks[userId] = callbackList // put the callback list back into list without the one just removed
            }
        }
    }


    private val mFirebaseRefsAllUsers : HashMap<String, ListenerRegistration> = HashMap()
    private var mCachedAllUsers : HashMap<String, ArrayList<User?>> = HashMap() // these are the cached profiles
    private var mAllUsersCallbacks = HashMap<String, ArrayList<(ArrayList<User?>, Exception?) -> Unit>>() // callbacks for that user id

    fun addAllUsersListeners(userId : String, onComplete : (ArrayList<User?>, Exception?) -> Unit){

        var callbacks = ArrayList<(ArrayList<User?>, Exception?) -> Unit>()

        // does it already have a callback
        if(mAllUsersCallbacks.containsKey(userId)){
            mAllUsersCallbacks[userId]?.let {
                callbacks = it
            }
        }
        else{
            callbacks = ArrayList()
        }

        // does not have this callback
        if(!callbacks.contains(onComplete)){
            callbacks.add(onComplete)
            mAllUsersCallbacks[userId] = callbacks
        }

        // return is user has already been cached and no data has changed for that user
        if(mCachedAllUsers.containsKey(userId)){ // if user data has been cached then return with this already saved user data
            val profile = mCachedAllUsers[userId]
            if (profile != null) {
                onComplete(profile, null)
            }
        }

        // do this if it has no listener yet
        if(!mFirebaseRefsAllUsers.contains(userId)){
            val ref = getDatabaseRef().addSnapshotListener{ querySnaphot, firebaseFirestoreException ->
                if(mAllUsersCallbacks.containsKey(userId)){ // has a valid callback

                    val callbackList = mAllUsersCallbacks[userId]
                    val list = ArrayList<User?>()

                    if(querySnaphot != null){
                        for(documentSnapshot in querySnaphot){
                            val profile = User(documentSnapshot) // gets data from database
                            list.add(profile)
                        }
                    }

                    // caching the data
                    if(list.size > 0){
                        this.mCachedAllUsers[userId] = list
                    }

                    if(callbackList!=null) {
                        onComplete(list, firebaseFirestoreException)
                    }

                }
            }
            mFirebaseRefsAllUsers[userId] = ref
        }
    }

    fun removeAllUsersListeners(userId : String, onComplete : (ArrayList<User?>?, Exception?) -> Unit){
        val callbackList = mAllUsersCallbacks[userId] // gets the list of callbacks for that user which was added in addListener

        if(callbackList != null && callbackList.contains(onComplete)){
            callbackList.remove(onComplete) // removes one call back at a time

            if(callbackList.size == 0){
                mAllUsersCallbacks.remove(userId) // remove the list from the cache
                val databaseRef = mFirebaseRefsAllUsers[userId] // get value which is the listener
                if(databaseRef!=null){
                    databaseRef.remove() // remove the snapshot lister // change listener to unit
                    mFirebaseRefsAllUsers.remove(userId) // remove the ref from our cached list
                }
            }
            else {
                mAllUsersCallbacks[userId] = callbackList // put the callback list back into list without the one just removed
            }
        }
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
                    // user has auth but data didn't save to collection
                    callback(true, Exception("Auth created but user data not saved"))
                }
            }
        }
        else{
            callback(false, Exception("No user auth found"))
        }
    }

    fun updateUserProfile(userId : String, user : User, callback : (Boolean?, Exception?) -> Unit){
        getDatabaseRef().document(userId).set(user.toMap()).addOnCompleteListener {task ->
            if(task.isSuccessful){
                callback(true, null)
            }
            else{
                if(task.exception != null){
                    callback(false, task.exception)
                }
                else{
                    callback(false, Exception("Unknown error"))
                }
            }
        }
    }

    private fun getDatabaseRef(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("users")
    }

}