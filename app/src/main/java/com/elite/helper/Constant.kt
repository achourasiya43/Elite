package com.elite.helper

import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import android.view.inputmethod.InputMethodManager
import java.security.MessageDigest
import java.util.Calendar


object Constant {
    // key for run time permissions
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    val REQUEST_CODE_PICK_CONTACTS = 100
    val MY_PERMISSIONS_REQUEST_CAMERA = 101
    val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102
    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    val ACCESS_FINE_LOCATION = 104
    val REQUEST_CAMERA = 0
    val SELECT_FILE = 1

    val DOCTOR_TYPE = 1
    val AGENCY_TYPE = 2
    var usertype = 0

    val ForLookingViewType = 1
    val CheckBoxViewType = 0


    val JobDetailsHome:Int = 1
    val JobDetailsSetting:Int = 2

    val LookingForHome:Int = 1
    val LookingFoSetting:Int = 2



    val ID = "id"
    val USERNAME = "userName"
    val PROFILEIMAGE = "profileImage"
    val LOGINTYPE = "loginType"
    val PARAMETERS = "parameters"

    //FireBase....................................................
    val ARG_USERS = "users"
    val ARG_RECEIVER = "receiver"
    val ARG_RECEIVER_UID = "receiver_uid"
    val ARG_CHAT_ROOMS = "chat_rooms"
    val ARG_FIREBASE_TOKEN = "firebaseToken"
    val ARG_FRIENDS = "friends"
    val ARG_UID = "uid"

    val ARG_HISTORY = "chat_history"

    val currentDate: String
        get() {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val month1 = month + 1
            return day.toString() + "-" + month1 + "-" + year

        }

    val currentTime: String
        get() {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR)
            val munite = c.get(Calendar.MINUTE)
            return hour.toString() + ":" + munite

        }

    fun getHashKey(packageName: String, context: Activity): String {
        val info: PackageInfo
        var something = ""
        try {
            info = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest
                md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                something = String(Base64.encode(md.digest(), 0))

                Log.e("hash key", something)
            }
        } catch (e1: Exception) {
            Log.e("name not found", e1.toString())
        }

        return something
    }

    fun hideSoftKeyboard(activity: Activity) {
        try {
            val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}
