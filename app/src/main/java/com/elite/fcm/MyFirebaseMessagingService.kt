package com.elite.fcm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.elite.R
import com.elite.activity.ChatActivity
import com.elite.activity.MainActivity
import com.elite.fcm_model.Chat
import com.elite.model.UserInfo
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import java.io.IOException
import java.net.URL

/**
 * Created by anil on 8/12/17.
 */

 class MyFirebaseMessagingService: FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        //val notification = remoteMessage?.getNotification()
        val data = remoteMessage?.getData()
        Log.e("FROM", remoteMessage?.getFrom())

        var type = data?.get("type")
        if(type.equals("") || type == null){
            sendChatNotification(data!!)

        }else  sendNotification(data!!)


    }


    private fun sendNotification(data: Map<String, String>) {
        var reference_id = data?.get("reference_id")
        var type = data?.get("type")
        var title = data?.get("title")
        var message = data?.get("body")


        val icon = BitmapFactory.decodeResource(resources, R.drawable.app_icon)
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("type",type)
        intent.putExtra("reference_id",reference_id)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(5, notificationBuilder.build())
    }

    private fun sendChatNotification(data: Map<String, String>) {
        var chat = Chat()
        val gson = Gson()

        var uid = data?.get("username")
        var fcm_token = data?.get("fcm_token")
        var title = data?.get("title")
        var message = data?.get("text")
        var opponentId = data?.get("uid")

        chat.uid = uid.toString()

        chat = gson.fromJson<Chat>(opponentId, Chat::class.java)

    /*    chat.opponent_Uid = opponentId.toString()
        chat.timestamp = "00000"
        chat.opponent_Name = title.toString()
        chat.category_Id = "2"
        chat.opponent_deviceToken = fcm_token.toString()*/







        val icon = BitmapFactory.decodeResource(resources, R.drawable.app_icon)
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("tranferChatInfo",chat)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(5, notificationBuilder.build())
    }

}
