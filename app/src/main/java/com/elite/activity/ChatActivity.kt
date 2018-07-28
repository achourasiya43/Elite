package com.elite.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.elite.R
import com.elite.adapter.ChattingAdapter
import com.elite.app_session.SessionManager
import com.elite.fcm.FcmNotificationBuilder
import com.elite.fcm_model.Chat
import com.elite.fcm_model.UserInfoFCM
import com.elite.helper.Constant
import com.github.ybq.android.spinkit.style.Circle
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.mvc.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.ByteArrayOutputStream
import java.util.*


class ChatActivity : AppCompatActivity() {

    private var chatList = ArrayList<Chat>()
    private var chatAdapter : ChattingAdapter ?= null
    private var session:SessionManager ?= null
    var chatData: Chat  = Chat()
    private val TAG = "ChatActivity"
    var myUid :String= ""
    var title : String =""
    var userInfoFcm = UserInfoFCM()
    var DatabaseReference  = FirebaseDatabase.getInstance()
    var profileImageBitmap: Bitmap? = null
    private var image_FirebaseURL: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        session = SessionManager(this)

        myUid = session?.user?.userDetail?.id!!

        var threeBounce  = Circle()
        spin_kit.setIndeterminateDrawable(threeBounce)

        if(intent != null){
            chatData =  intent.getSerializableExtra("tranferChatInfo") as Chat
            if(title != null && !title.equals("")){
                title = intent.getStringExtra("title")
            }

            getChat(myUid, chatData.uid)
            gettingDataFromUserTable(chatData.uid)
            title_header.text = chatData.name

        }



        iv_back.setOnClickListener {
            onBackPressed()
        }

        send_message.setOnClickListener {
            sendMessage(myUid, chatData.uid)
        }

        image.setOnClickListener {
            message.setText("")
            getPermissionAndPicImage()
        }

    }

    fun getPermissionAndPicImage(){
        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 105)
            } else {
                ImagePicker.pickImage(this)
            }
        } else {
            ImagePicker.pickImage(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {//https://stackoverflow.com/questions/36761478/cannot-set-onitemclicklistener-for-spinner-in-android
            Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, Constant.SELECT_FILE)
                    getPermissionAndPicImage()
                } else {
                    Toast.makeText(this, "YOU DENIED PERMISSION CANNOT SELECT IMAGE", Toast.LENGTH_LONG).show()
                }
            }

            Constant.MY_PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, Constant.REQUEST_CAMERA)
                    getPermissionAndPicImage()
                } else {
                    Toast.makeText(this, "YOUR  PERMISSION DENIED ", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 234) {
                profileImageBitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data)
                if (profileImageBitmap != null){
                    val uri = getImageUri(this, profileImageBitmap!!)
                    creatFirebaseProfilePicUrl(uri)
                    spin_kit.visibility = View.VISIBLE
                }

            }
        }
    }


    fun getImageUri(inContext: Context, inImage: Bitmap) : Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    fun creatFirebaseProfilePicUrl(selectedImageUri: Uri) {
        var storageRef: StorageReference
        var storage: FirebaseStorage
        var app: FirebaseApp

        app = FirebaseApp.getInstance()!!
        storage = FirebaseStorage.getInstance(app)

        storageRef = storage.getReference("chat_photos_sys" + getString(R.string.app_name))
        val photoRef = storageRef.child(selectedImageUri.lastPathSegment)
        photoRef.putFile(selectedImageUri).addOnCompleteListener(this,  { task ->
            if (task.isSuccessful) {
                image_FirebaseURL = task.result.downloadUrl
                sendMessage(myUid, chatData.uid)
            }
        })
    }

    private fun sendMessage(senderUid : String ,receiverUid: String) {
        val userInfo  = session?.user?.userDetail
        var chat = Chat()
        var myChat = Chat()

        tv_send_first_message.visibility = View.GONE
        var mesage1 : String = message.getText().toString().trim()
        if(!mesage1.equals("") || image_FirebaseURL != null){


            if(mesage1.equals("")){
                chat.message = image_FirebaseURL.toString()
            }else{
                chat.message = mesage1
            }
            chat.name =  chatData.name
            chat.uid = chatData.uid
            chat.timestamp = Calendar.getInstance().time.time.toString()
            chat.deleteby = ""
            chat.firebaseToken = chatData.firebaseToken
            chat.deleteby = ""
            chat.category_Id = chatData.category_Id
            chat.category_name = chatData.category_name
            chat.profilePic = chatData.profilePic


            myChat.message = mesage1
            myChat.name =  userInfo?.name.toString()
            myChat.uid = myUid
            myChat.timestamp = Calendar.getInstance().time.time.toString()
            myChat.deleteby = ""
            myChat.firebaseToken =  userInfo?.deviceToken.toString()
            myChat.deleteby = ""
            myChat.category_Id = chatData.category_Id
            myChat.category_name = chatData.category_name
            myChat.profilePic = userInfo?.profileImage.toString()


            val room_type_1 = myUid + "_" + receiverUid
            val room_type_2 = receiverUid + "_" + myUid

            if(!senderUid.equals("") && !receiverUid.equals("")){
                DatabaseReference?.reference?.child(Constant.ARG_CHAT_ROOMS)!!.getRef().addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onCancelled(p0: DatabaseError?) {}

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        image_FirebaseURL = null
                        spin_kit.visibility = View.GONE

                        if (dataSnapshot!!.hasChild(room_type_1)) {

                            DatabaseReference?.reference?.child(Constant.ARG_CHAT_ROOMS)?.child(room_type_1)!!.child(chatData.category_Id).child(chat.timestamp).setValue(chat)

                            FirebaseDatabase.getInstance().reference.child(Constant.ARG_HISTORY).
                                    child(chat.uid).child(myUid).child(chatData.category_Id).setValue(myChat)

                            FirebaseDatabase.getInstance().reference.child(Constant.ARG_HISTORY).
                                    child(myUid).child(chat.uid).child(chatData!!.category_Id).setValue(chat)
                            message.setText("")
                        }
                        else if(dataSnapshot.hasChild(room_type_2)){


                            DatabaseReference?.reference?.child(Constant.ARG_CHAT_ROOMS)!!.child(room_type_2).child(chatData!!.category_Id).child(chat.timestamp).setValue(chat)

                            //  FirebaseDatabase.getInstance().reference.child(Constant.ARG_HISTORY).child(room_type_2).child(tranferChatInfo!!.category_id).setValue(chat)

                            DatabaseReference?.reference?.child(Constant.ARG_HISTORY)!!.
                                    child(chat.uid).child(myUid).child(chatData!!.category_Id).setValue(myChat)

                            DatabaseReference?.reference?.child(Constant.ARG_HISTORY)!!.
                                    child(myUid).child(chat.uid).child(chatData!!.category_Id).setValue(chat)
                            message.setText("")

                        } else {

                            DatabaseReference?.reference?.child(Constant.ARG_CHAT_ROOMS)!!.child(room_type_1).child(chatData!!.category_Id).child(chat.timestamp).setValue(chat)

                            // DatabaseReference?.reference?.child(Constant.ARG_HISTORY).child(room_type_1).child(tranferChatInfo!!.category_id).setValue(chat)

                            DatabaseReference?.reference?.child(Constant.ARG_HISTORY)!!.
                                    child(chat.uid).child(myUid).child(chatData!!.category_Id).setValue(myChat)

                            DatabaseReference?.reference?.child(Constant.ARG_HISTORY)!!.
                                    child(myUid).child(chat.uid).child(chatData!!.category_Id).setValue(chat)
                            message.setText("")
                        }

                        sendPushNotificationToReceiver(userInfo?.name.toString(),
                                chat.message,
                                chat,
                                chat.uid, myUid)
                    }

                })

            }

        }



    }

    private fun getChat(senderUid : String ,receiverUid: String ) {
        spin_kit.visibility = View.VISIBLE
        val room_type_1 = senderUid + "_" + receiverUid
        val room_type_2 = receiverUid + "_" + senderUid

        DatabaseReference?.reference?.child(Constant.ARG_CHAT_ROOMS)
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {}

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {

                        if (dataSnapshot!!.hasChild(room_type_1)) {
                            Log.e(TAG, "getMessageFromFirebaseUser: $room_type_1 exists")

                            FirebaseDatabase.getInstance()
                                    .reference
                                    .child(Constant.ARG_CHAT_ROOMS)
                                    .child(room_type_1).child(chatData.category_Id).addChildEventListener(object : ChildEventListener {

                                override fun onCancelled(p0: DatabaseError?) {}

                                override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

                                override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}

                                override fun onChildRemoved(p0: DataSnapshot?) {}

                                override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
                                    val chat = dataSnapshot?.getValue(Chat::class.java)
                                    chatList.add(chat!!)
                                    chatAdapter?.notifyDataSetChanged()
                                    recycler_view.scrollToPosition((chatList.size - 1))
                                    spin_kit.visibility = View.GONE
                                }


                            })

                        }else if (dataSnapshot.hasChild(room_type_2)){
                            FirebaseDatabase.getInstance()
                                    .reference
                                    .child(Constant.ARG_CHAT_ROOMS)
                                    .child(room_type_2) .child(chatData.category_Id).addChildEventListener(object : ChildEventListener {

                                override fun onCancelled(p0: DatabaseError?) {}

                                override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

                                override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}

                                override fun onChildRemoved(p0: DataSnapshot?) {}

                                override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
                                    var chat  = dataSnapshot?.getValue(Chat::class.java)
                                    chatList.add(chat!!)
                                    chatAdapter?.notifyDataSetChanged()
                                    recycler_view.scrollToPosition((chatList.size - 1))
                                    spin_kit.visibility = View.GONE
                                }


                            })
                        }else
                        {
                            tv_send_first_message.visibility = View.VISIBLE
                            spin_kit.visibility = View.GONE

                            FirebaseDatabase.getInstance()
                                    .reference
                                    .child(Constant.ARG_CHAT_ROOMS)
                                    .child(room_type_1) .child(chatData.category_Id).addChildEventListener(object : ChildEventListener {

                                override fun onCancelled(p0: DatabaseError?) {}

                                override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

                                override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}

                                override fun onChildRemoved(p0: DataSnapshot?) {}

                                override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
                                    var chat  = dataSnapshot?.getValue(Chat::class.java)
                                    chatList.add(chat!!)
                                    chatAdapter?.notifyDataSetChanged()
                                    recycler_view.scrollToPosition((chatList.size - 1))
                                    spin_kit.visibility = View.GONE
                                }


                            })
                        }
                    }

                })

    }

    private fun sendPushNotificationToReceiver(username: String,
                                               message: String,
                                               chat: Chat,
                                               firebaseToken: String?,
                                               uId: String) {

        var gson = Gson()
        var json = gson.toJson(chat)

        FcmNotificationBuilder.initialize()
                .title(username)
                .message(message)
                .username(uId)
                .uid(json)
                .firebaseToken(firebaseToken)
                .receiverFirebaseToken(userInfoFcm.firebaseToken).send()
    }

    private fun gettingDataFromUserTable(firebaseUid : String){
        DatabaseReference?.reference?.child(Constant.ARG_USERS)?.child(firebaseUid)
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {}

                    override fun onDataChange(p0: DataSnapshot?) {
                        userInfoFcm = p0?.getValue(UserInfoFCM :: class.java)!!
                        var myImg = session?.user?.userDetail?.profileImage

                        chatAdapter = ChattingAdapter(this@ChatActivity,chatList ,userInfoFcm.profilePic,myImg.toString())
                        recycler_view.adapter = chatAdapter
                    }

                })
    }
}
