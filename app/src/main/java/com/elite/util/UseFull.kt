package com.elite.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by anil on 5/12/17.
 */
class UseFull {

    fun getDayDifference(departDateTime: String, returnDateTime: String): String {
        val isgrater = false
        var returnDay = ""
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        try {

            val startDate = simpleDateFormat.parse(departDateTime)
            val endDate = simpleDateFormat.parse(returnDateTime)

            //milliseconds
            var different = endDate.time - startDate.time

            println("startDate : " + startDate)
            println("endDate : " + endDate)
            println("different : " + different)

            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val daysInMilli = hoursInMilli * 24

            val elapsedDays = different / daysInMilli
            different = different % daysInMilli

            val elapsedHours = different / hoursInMilli
            different = different % hoursInMilli

            val elapsedMinutes = different / minutesInMilli
            different = different % minutesInMilli

            val elapsedSeconds = different / secondsInMilli

            if (elapsedDays == 0L) {
                if (elapsedHours == 0L) {
                    if (elapsedMinutes == 0L) {
                        returnDay = elapsedSeconds.toString() + " Just now"
                    } else {
                        returnDay = elapsedMinutes.toString() + " Min"
                    }
                } else if (elapsedHours == 1L) {
                    returnDay = elapsedHours.toString() + " Hr"
                } else {
                    returnDay = elapsedHours.toString() + " Hrs"
                }
            } else if (elapsedDays == 1L) {
                returnDay = elapsedDays.toString() + " Day"
            } else {
                returnDay = elapsedDays.toString() + " Days"
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return returnDay
    }

    fun getCurrentDate(): String {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val month1 = month + 1
        return year.toString() + "-" + month1 + "-" + day
    }

    fun getCurrentTime(): String {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val munite = c.get(Calendar.MINUTE)
        val secand = c.get(Calendar.SECOND)
        return hour.toString() + ":" + munite + ":" +secand

    }

}