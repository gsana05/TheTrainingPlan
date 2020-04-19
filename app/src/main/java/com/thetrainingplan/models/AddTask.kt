package com.thetrainingplan.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import java.io.InvalidObjectException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class AddTask() : Parcelable{
    var id: String = ""
    var requestUserId:String? = null
    var name: String = ""
    var description: String? = null
    var startDate: Date = Date()
    var endDate: Date? = null
    var repeatType:Int = 0
    var repeatEvery:Int? = null
    var repeatWeekdays:Int? = null
    var diaryStatus : Int = -1
    var deletedDates: ArrayList<Date>? = null
    var doneDates: ArrayList<Date>? =null
    var goalId : String? = null // pointing the task to which long term goal

    constructor(parcel: Parcel) : this() {

        val requestUserId = parcel.readString()
        val name = parcel.readString()
        val description = parcel.readString()
        val repeatType = parcel.readInt()
        val repeatEvery = parcel.readValue(Int::class.java.classLoader) as? Int
        val repeatWeekdays = parcel.readValue(Int::class.java.classLoader) as? Int
        val startDate = Date(parcel.readLong())
        val endLong = parcel.readLong()
        var endDate: Date? = Date(endLong)
        val goalId = parcel.readString()
        if(endLong == 0L){
            endDate = null
        }

        this(requestUserId,startDate,repeatType,description,endDate,repeatEvery,repeatWeekdays, goalId)
        id = parcel.readString().toString()
    }

    private operator fun invoke(requestUserId: String?, startDate: Date, repeatType: Int, description: String?, endDate: Date?, repeatEvery: Int?, repeatWeekdays: Int?, goalId : String?) {
        this.requestUserId = requestUserId
        this.name = name
        this.startDate=startDate
        this.repeatType=repeatType
        this.description=description
        this.endDate=endDate
        this.repeatEvery=repeatEvery
        this.repeatWeekdays=repeatWeekdays
        this.goalId = goalId
    }

    constructor(requestUserId: String?, name:String, startDate: Date, repeatType:Int, description:String?, endDate: Date?, repeatEvery:Int?, repeatWeekdays:Int?, goalId: String?) : this() {
        this.id= UUID.randomUUID().toString()
        this.requestUserId = requestUserId
        this.name = name
        this.startDate=startDate
        this.repeatType=repeatType
        this.description=description
        this.endDate=endDate
        this.repeatEvery=repeatEvery
        this.repeatWeekdays=repeatWeekdays
        this.goalId = goalId
    }

    @Throws(InvalidObjectException::class)
    constructor(data: HashMap<String, Any>) : this() {
        try {
            this.id = data["id"] as String
            requestUserId = data["requestUserId"] as String?
            doneDates = data["doneDates"] as ArrayList<Date>?
            name = data["name"] as String
            description = data["description"] as String?
            startDate=data["startDate"] as Date
            goalId=data["goalId"] as String?
        }catch (e: Exception){
            e.printStackTrace()
            throw InvalidObjectException("HashMap is invalid")
        }

        if(data.containsKey("repeatType")) {
            val repeatTypeNum = data["repeatType"] as Number?
            if(repeatTypeNum!=null) {
                repeatType = repeatTypeNum.toInt()
            }
            else{
                throw InvalidObjectException("HashMap is invalid")
            }
        }
        else{
            throw InvalidObjectException("HashMap is invalid")
        }
        if(data.containsKey("repeatEvery")) {
            val repeatEveryNum = data["repeatEvery"] as Number?
            repeatEvery = repeatEveryNum?.toInt()
        }
        if(data.containsKey("repeatWeekdays")) {
            val repeatWeekDaysNum = data["repeatWeekdays"] as Number?
            repeatWeekdays = repeatWeekDaysNum?.toInt()
        }
        if(data.containsKey("endDate")) {
            endDate = data["endDate"] as Date?
        }

        if (data.containsKey("deletedDates")) {
            deletedDates = data["deletedDates"] as ArrayList<Date>?
        }

    }

    fun toMap(): HashMap<String, Any?> {
        val map: HashMap<String, Any?> = HashMap()
        map["id"] = id
        map["requestUserId"] = requestUserId
        map["doneDates"] = doneDates
        map["name"]=name
        map["description"]=description
        map["startDate"]= Timestamp(startDate)
        if(endDate!=null) {
            map["endDate"] = Timestamp(endDate!!)
        }
        map["repeatType"]=repeatType
        map["repeatEvery"]=repeatEvery
        map["repeatWeekdays"]=repeatWeekdays
        map["goalId"]=goalId


        var mappedDeletedDates = ArrayList<Timestamp>()
        deletedDates?.let {
            for (deletedDate in it) {
                mappedDeletedDates.add(Timestamp(deletedDate))
            }
        }
        map["deletedDates"] = mappedDeletedDates
        return map
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(requestUserId)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeInt(repeatType)
        parcel.writeValue(repeatEvery)
        parcel.writeValue(repeatWeekdays)
        parcel.writeInt(diaryStatus)
        parcel.writeLong(startDate.time)
        parcel.writeLong(endDate?.time?:0)
        parcel.writeString(id)
        parcel.writeString(goalId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AddTask> {
        override fun createFromParcel(parcel: Parcel): AddTask {
            return AddTask(parcel)
        }

        override fun newArray(size: Int): Array<AddTask?> {
            return arrayOfNulls(size)
        }

        public val NEVER = 0
        public val DAY = 1
        public val WEEK = 2
        public val MONTH = 3
        public val YEAR = 4

        public val REJECTED = 3 // rejected diary event
        public val ACCEPTED = 2 // accepted diary events
        public val PENDING = 0 // pending diary events


        fun getTimePortionAsMillis(time: Date): Long {
            val cal = Calendar.getInstance()
            cal.time = time
            val hours = TimeUnit.HOURS.toMillis(cal.get(Calendar.HOUR_OF_DAY).toLong())
            val mins = TimeUnit.MINUTES.toMillis(cal.get(Calendar.MINUTE).toLong())
            val secs = TimeUnit.SECONDS.toMillis(cal.get(Calendar.SECOND).toLong())

            return hours + mins + secs
        }
    }
}