package com.thetrainingplan.models

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object UserModel {

    //val currentListOfUser = MutableLiveData<ArrayList<User>>().apply { value = null }

    /*fun getData() : ArrayList<User>{
        val list = ArrayList<User>()
        list.add(User("Gareth", "sanashee05@hotmail.com"))
        list.add(User("John", "ronaldo@hotmail.com"))
        //currentListOfUser.value = list
        return list
    }*/

    private val mFirebaseRefs : HashMap<String, ListenerRegistration> = HashMap()
    private var mCachedProfiles : HashMap<String, ArrayList<User?>> = HashMap() // these are the cached profiles
    private var mProfileCallbacks = HashMap<String, ArrayList<(ArrayList<User?>, Exception?) -> Unit>>() // callbacks for that user id

  /*  fun addAllUsersListenersTest(userId : String) : ArrayList<User> {

        val list = ArrayList<User?>()

        // return is user has already been cached and no data has changed for that user
        if(mCachedProfiles.containsKey(userId)){ // if user data has been cached then return with this already saved user data
            val profile = mCachedProfiles[userId]
            if (profile != null) {
                val theList = ArrayList<User>()
                profile.let {
                    for( i in it){
                        i.let {
                            it?.let {
                                theList.add(it)
                            }
                        }
                    }
                }
                return theList
            }
        }

        // do this if it has no listener yet
        if(!mFirebaseRefs.contains(userId)){
            val ref = getDatabaseRef().addSnapshotListener{ querySnaphot, firebaseFirestoreException ->
                    try {
                        if(querySnaphot != null){
                            for(documentSnapshot in querySnaphot){
                                val profile = User(documentSnapshot) // gets data from database
                                list.add(profile)
                            }
                        }
                    }
                    catch(e : Exception){ }

                    // caching the data
                    if(list.size > 0){
                        this.mCachedProfiles[userId] = list
                    }
            }
            mFirebaseRefs[userId] = ref
        }


        val theList2 = ArrayList<User>()
        list.let {
            for( i in it){
                i.let {
                    it?.let {
                        theList2.add(it)
                    }
                }
            }
        }

        return theList2
    }*/


    fun addAllUsersListeners(userId : String, onComplete : (ArrayList<User?>, Exception?) -> Unit){

        var callbacks = ArrayList<(ArrayList<User?>, Exception?) -> Unit>()

        // does it already have a callback
        if(mProfileCallbacks.containsKey(userId)){
            mProfileCallbacks[userId]?.let {
                callbacks = it
            }
        }
        else{
            callbacks = ArrayList()
        }

        // does not have this callback
        if(!callbacks.contains(onComplete)){
            callbacks.add(onComplete)
            mProfileCallbacks[userId] = callbacks
        }

        // return is user has already been cached and no data has changed for that user
        if(mCachedProfiles.containsKey(userId)){ // if user data has been cached then return with this already saved user data
            val profile = mCachedProfiles[userId]
            if (profile != null) {
                onComplete(profile, null)
            }
        }

        // do this if it has no listener yet
        if(!mFirebaseRefs.contains(userId)){
            val ref = getDatabaseRef().addSnapshotListener{ querySnaphot, firebaseFirestoreException ->
                if(mProfileCallbacks.containsKey(userId)){ // has a valid callback

                    val callbackList = mProfileCallbacks[userId]
                    val list = ArrayList<User?>()

                    try {
                        if(querySnaphot != null){
                            for(documentSnapshot in querySnaphot){
                                val profile = User(documentSnapshot) // gets data from database
                                list.add(profile)
                            }
                        }
                    }
                    catch(e : Exception){ }

                    // caching the data
                    if(list.size > 0){
                        this.mCachedProfiles[userId] = list
                    }

                    if(callbackList!=null) {
                        onComplete(list, firebaseFirestoreException)
                    }

                }
            }
            mFirebaseRefs[userId] = ref
        }
    }

    fun removeAllUsersListeners(userId : String, onComplete : (ArrayList<User?>?, Exception?) -> Unit){
        val callbackList = mProfileCallbacks[userId] // gets the list of callbacks for that user which was added in addListener

        if(callbackList != null && callbackList.contains(onComplete)){
            callbackList.remove(onComplete) // removes one call back at a time

            if(callbackList.size == 0){
                mProfileCallbacks.remove(userId) // remove the list from the cache
                val databaseRef = mFirebaseRefs[userId] // get value which is the listener
                if(databaseRef!=null){
                    databaseRef.remove() // remove the snapshot lister // change listener to unit
                    mFirebaseRefs.remove(userId) // remove the ref from our cached list
                }
            }
            else {
                mProfileCallbacks[userId] = callbackList // put the callback list back into list without the one just removed
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