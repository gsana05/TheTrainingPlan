package com.thetrainingplan.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import java.io.InvalidObjectException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class AddTask( var id: String?,
               var requestUserId:String? = null,
               var name: String = "",
               var description: String? = null,
               var startDate: Long? = null,
               var endDate: Long? = null,
               var repeatType:Int = 0,
               var repeatEvery:Int? = null,
               var repeatWeekdays:Int? = null,
               var deletedDates: ArrayList<Date>? = null,
               var doneDates: ArrayList<Date>? =null,
               var goalId : String? = null,
               var completionTime : Long? = null
               )
