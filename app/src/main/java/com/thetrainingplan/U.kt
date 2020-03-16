package com.thetrainingplan


import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.annotation.ArrayRes
import com.thetrainingplan.models.GoalModel
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*
import java.util.Calendar.*
import kotlin.math.max
import kotlin.math.min

class U {

    companion object {

        @JvmStatic
        fun goalType(type: Int): String {
          when(type){
              GoalModel.OTHER_ID -> {
                  return GoalModel.OTHER
              }
              GoalModel.SPIRITUAL_ID -> {
                  return GoalModel.SPIRITUAL
              }
              GoalModel.PHYSICAL_ID -> {
                  return GoalModel.PHYSICAL
              }
              GoalModel.PSYCHOLOGY_ID -> {
                  return GoalModel.PSYCHOLOGY
              }
              GoalModel.EMOTIONAL_ID -> {
                  return GoalModel.EMOTIONAL
              }
              else -> {
                  return "Error - Please contact support"
              }
          }
        }

        @JvmStatic
        fun formatCurrencyShort(amount: Double): String {
            val test = amount.toInt()
            val currencySymbol = Currency.getInstance(Locale.UK).symbol

            return if (test >= 1000000) {
                "$currencySymbol${test / 1000000}M"
            }
            else if (test >= 1000) {
                "$currencySymbol${test / 1000}K"
            }
            else {
                "$currencySymbol$test"
            }
        }

        @JvmStatic
        @JvmOverloads
        fun formatCurrency(amount: Double, showMinorCurrencyUnit: Boolean = true): String {
            val format = NumberFormat.getCurrencyInstance(Locale.UK)
            if (!showMinorCurrencyUnit) {
                format.maximumFractionDigits = 0
                format.minimumFractionDigits = 0
            }
            return format.format(amount)
        }

        @JvmStatic
        @JvmOverloads
        fun formatDate(msSince1970: Long, longFormat: Boolean = true): String {
            val style = if (longFormat) DateFormat.LONG else DateFormat.SHORT
            return DateFormat.getDateInstance(style).format(Date(msSince1970))
        }

        /*fun formatFromStringArray(@ArrayRes res: Int, value: Int, defaultVal: String = ""): String {
            val names = MyApplication.currentApplication.applicationContext.resources.getStringArray(res)
            return if (value >= 0 && value < names.size) names[value] else defaultVal
        }*/

        @JvmStatic
        @JvmOverloads
        fun toStringOrDash(num: Number?, dp: Int = 0): String {
            return when {
                num == null || num.toDouble() == 0.0 -> "-"
                dp == 0 -> "%d".format(num.toInt())
                else -> "%.{$dp}f".format(num.toDouble())
            }
        }

        @JvmStatic
        fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap {

            val width = bm.width
            val height = bm.height

            val scaleWidth = newWidth.toFloat() / width
            val scaleHeight = newHeight.toFloat() / height

            val matrix = Matrix()
            matrix.postScale(scaleWidth, scaleHeight)

            return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
        }

        //Returns the whole number of calendar months between two dates, optionally including part months
        fun calendarMonthsBetween(d1: Long, d2: Long, includePartMonths: Boolean = false): Int {
            val cal1 = GregorianCalendar().apply {
                time = Date(min(d1, d2))
                setTimeToStartOfDay()
            }
            val cal2 = GregorianCalendar().apply {
                time = Date(max(d1, d2))
                setTimeToStartOfDay()
            }

            var months = 0
            while (cal1.time < cal2.time) {
                cal1.add(MONTH, 1)
                if (includePartMonths || !cal1.after(cal2)) {
                    months++
                }
            }

            return if (d1 < d2) months else -months
        }

        private fun GregorianCalendar.setTimeToStartOfDay() {
            set(HOUR_OF_DAY, 0)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
        }
    }
}


fun Calendar.setTimeToStartOfDay() {
    set(HOUR_OF_DAY, 0)
    set(MINUTE, 0)
    set(SECOND, 0)
    set(MILLISECOND, 0)
}