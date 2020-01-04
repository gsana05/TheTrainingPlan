package com.thetrainingplan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.thetrainingplan.adapters.WorkoutHistoryAdaptor
import com.thetrainingplan.databinding.ActivityMainBinding
import com.thetrainingplan.models.User
import com.thetrainingplan.models.UserModel
import com.thetrainingplan.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var clientListAdapter: WorkoutHistoryAdaptor
    private var mCallbackAllUsers = { _:ArrayList<User?>?, _: Exception? -> Unit}
    private var mCallbackCurrentUser = { _:User?, _: Exception? -> Unit}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        // reference to view model
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        clientListAdapter = WorkoutHistoryAdaptor()
        val mLayoutManager = LinearLayoutManager(applicationContext)
        main_recycler_view.layoutManager = mLayoutManager
        main_recycler_view.adapter = clientListAdapter

        main_log_out_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val userId = FirebaseAuth.getInstance().uid
            if(userId == null){
                startActivity(Intent(this, ActivityLogIn::class.java))
            }
        }

        mCallbackCurrentUser = { data : User?, _: Exception? ->
            if(data != null){
                viewModel.currentUser.value = data
            }
        }

        mCallbackAllUsers = { data : ArrayList<User?>?, _ : Exception? ->
            if(data != null) {
                Log.i("Hello", "all data ${data.size}")
                viewModel.listOfUser.value = data
            }
        }

        /*val userId = FirebaseAuth.getInstance().uid
     if(userId != null){

         val li = UserModel.addAllUsersListenersTest(userId)
         val lo = UserModel.getData()

         val ob = io.reactivex.Observable
             .fromIterable(li)
             .subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .filter(object : Predicate<User>, io.reactivex.functions.Predicate<User> {
                 override fun test(t: User): Boolean {
                     return t.email != "sanashee05@hotmail.com"
                 }
             })

         ob.subscribe(object : io.reactivex.Observer<User> {
             override fun onComplete() {
                 //main_recycler_view.adapter = WorkoutHistoryAdaptor(listOfUser)
                 Log.v("TAG","onComplete called:")
             }

             override fun onSubscribe(d: Disposable) {
                 Log.v("TAG","onSubscribed called:")
             }

             override fun onNext(t: User) {
                 //listOfUser.add(t)
                 Log.v("TAG","onNext called:" + Thread.currentThread().name)
                 Log.v("TAG","onNext called:" + t.email)
             }

             override fun onError(e: Throwable) {
                 Log.v("TAG", "onError called:$e")
             }

         })
     }*/
    }

    override fun onPause() {
        super.onPause()
        val userId = FirebaseAuth.getInstance().uid
        if(userId != null){
            UserModel.removeCurrentUserListener(userId, mCallbackCurrentUser)
            UserModel.removeAllUsersListeners(userId, mCallbackAllUsers)
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().uid
        if(userId == null){
            startActivity(Intent(this, ActivityLogIn::class.java))
        }
        else{
            UserModel.addCurrentUserListener(userId, mCallbackCurrentUser)
            UserModel.addAllUsersListeners(userId, mCallbackAllUsers)
        }
    }
}
