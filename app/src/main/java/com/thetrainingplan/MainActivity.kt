package com.thetrainingplan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.thetrainingplan.adapters.WorkoutHistoryAdaptor
import com.thetrainingplan.databinding.ActivityMainBinding
import com.thetrainingplan.models.User
import com.thetrainingplan.models.UserModel
import com.thetrainingplan.viewmodels.MainViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.function.Predicate


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private val listOfUser = ArrayList<User>()
    private lateinit var clientListAdapter: WorkoutHistoryAdaptor

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

        /*val ob = io.reactivex.Observable
            .fromIterable(UserModel.getData())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .filter(object : Predicate<User>, io.reactivex.functions.Predicate<User> {
                override fun test(t: User): Boolean {
                    return t.email != "sanashee05@hotmail.com"
                }
            })

        ob.subscribe(object : Observer<User>{
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

        })*/

    }
}
