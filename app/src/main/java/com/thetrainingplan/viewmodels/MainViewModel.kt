package com.thetrainingplan.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.thetrainingplan.models.User
import com.thetrainingplan.models.UserModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.function.Predicate

class MainViewModel(application : Application) : AndroidViewModel(application) {

    var filteredAgreements: LiveData<List<User>> = Transformations.switchMap(UserModel.currentListOfUser) { listOfUser  ->
        getFilteredList(listOfUser)
    }

    private fun getFilteredList(list : ArrayList<User>) : LiveData<List<User>>{
        return Transformations.map(UserModel.currentListOfUser){ listOfAgreements ->
            val oo = listOfAgreements.toList()
            applyFilters(oo)
        }
    }

    private fun applyFilters(listOfAgreements : List<User>) : List<User>{
        return listOfAgreements.filter {agreement ->

            agreement.email.length > 3

        }
    }

   /* private fun test(){
        val ob = io.reactivex.Observable
            .fromIterable(UserModel.getData())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .filter(object : Predicate<User>, io.reactivex.functions.Predicate<User> {
                override fun test(t: User): Boolean {
                    return t.email != "sanashee05@hotmail.com"
                }
            })

        ob.subscribe(object : Observer<User> {
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