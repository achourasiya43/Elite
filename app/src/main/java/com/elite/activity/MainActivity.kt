package com.elite.activity

import android.app.Dialog
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.util.Log
import android.view.View
import android.view.Window
import com.elite.R
import com.elite.app_session.SessionManager
import com.elite.fragment.*
import com.elite.fragment.agency_side.AllRequestFragment
import com.elite.fragment.doctor_side.AllPostFragment
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.delete_post_layout.*
import kotlinx.android.synthetic.main.tab_bar_layout.*
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.elite.WebService.WebServices
import com.elite.fcm_model.UserInfoFCM
import com.elite.fragment.agency_side.JobDetailsFragment
import com.elite.fragment.doctor_side.LookingForFragment
import com.elite.helper.Constant
import com.elite.model.FinishedInfo
import com.elite.util.Utils
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_chat.view.*
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.util.*


class MainActivity : AppCompatActivity(),View.OnClickListener{
    var session : SessionManager ?= null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         session = SessionManager(this)
        iv_post_tab.setOnClickListener(this)
        iv_chat_tab.setOnClickListener(this)
        iv_notification_tab.setOnClickListener(this)
        iv_setting_tab.setOnClickListener(this)
        iv_logout_tab.setOnClickListener(this)

        terminal(session?.user?.userDetail?.userType.toString())



        var type  = intent.getStringExtra("type")
        var reference_id  = intent.getStringExtra("reference_id")

        if(type != null){
            if(type.equals("post_create") || type.equals("post_update")){
                addFragment(JobDetailsFragment.newInstance(reference_id, 0, 0),true,R.id.fragment_place)

            }else if(type.equals("post_interest")){
                var finishedInfo = FinishedInfo()
                addFragment(LookingForFragment.newInstance(reference_id, "0", Constant.LookingForHome,finishedInfo),true,R.id.fragment_place)

            }else if(type.equals("post_finish")){
                addFragment(JobDetailsFragment.newInstance(reference_id, 0, 2),true,R.id.fragment_place)

            }else if(type.equals("post_delete")){
                addFragment(AllRequestFragment.newInstance("", ""),true,R.id.fragment_place)

            }
        }


    }



    override fun onClick(p0: View?) {
      when(p0){
          iv_post_tab ->{
              iv_post_tab.setImageResource(R.drawable.active_post_icon)
              post_view.visibility = View.VISIBLE

              chat_view.visibility = View.INVISIBLE
              notification_view.visibility = View.INVISIBLE
              setting_view.visibility = View.INVISIBLE
              logout_view.visibility = View.INVISIBLE

              iv_chat_tab.setImageResource(R.drawable.inactive_chat_icon)
              iv_notification_tab.setImageResource(R.drawable.inactive_notification_icon)
              iv_setting_tab.setImageResource(R.drawable.inactive_setting_icon)
              iv_logout_tab.setImageResource(R.drawable.inactive_logout_icon)

              if(session?.user?.userDetail?.userType.toString().equals("1")){
                  replaceFragment(AllPostFragment.newInstance("",""),false,R.id.fragment_place)
              }else if(session?.user?.userDetail?.userType.toString().equals("2")){
                  replaceFragment(AllRequestFragment.newInstance("",""),false,R.id.fragment_place)
              }else Toasty.error(this,"Something went wrong").show()



          }
          iv_chat_tab ->{
              iv_chat_tab.setImageResource(R.drawable.active_chat_icon)
              chat_view.visibility = View.VISIBLE

              post_view.visibility = View.INVISIBLE
              notification_view.visibility = View.INVISIBLE
              setting_view.visibility = View.INVISIBLE
              logout_view.visibility = View.INVISIBLE

              iv_post_tab.setImageResource(R.drawable.inactive_post_icon)
              iv_notification_tab.setImageResource(R.drawable.inactive_notification_icon)
              iv_setting_tab.setImageResource(R.drawable.inactive_setting_icon)
              iv_logout_tab.setImageResource(R.drawable.inactive_logout_icon)

              replaceFragment(ChatFragment.newInstance("",""),false,R.id.fragment_place)

          }
          iv_notification_tab ->{
              iv_notification_tab.setImageResource(R.drawable.active_notification_icon)
              notification_view.visibility = View.VISIBLE

              post_view.visibility = View.INVISIBLE
              chat_view.visibility = View.INVISIBLE
              setting_view.visibility = View.INVISIBLE
              logout_view.visibility = View.INVISIBLE

              iv_post_tab.setImageResource(R.drawable.inactive_post_icon)
              iv_chat_tab.setImageResource(R.drawable.inactive_chat_icon)
              iv_setting_tab.setImageResource(R.drawable.inactive_setting_icon)
              iv_logout_tab.setImageResource(R.drawable.inactive_logout_icon)

              replaceFragment(NotificationFragment.newInstance("",""),false,R.id.fragment_place)

          }
          iv_setting_tab ->{
              iv_setting_tab.setImageResource(R.drawable.active_setting_icon)
              setting_view.visibility = View.VISIBLE

              post_view.visibility = View.INVISIBLE
              chat_view.visibility = View.INVISIBLE
              notification_view.visibility = View.INVISIBLE
              logout_view.visibility = View.INVISIBLE

              iv_post_tab.setImageResource(R.drawable.inactive_post_icon)
              iv_chat_tab.setImageResource(R.drawable.inactive_chat_icon)
              iv_notification_tab.setImageResource(R.drawable.inactive_notification_icon)
              iv_logout_tab.setImageResource(R.drawable.inactive_logout_icon)

              replaceFragment(SettingFragment.newInstance("",""),false,R.id.fragment_place)

          }
          iv_logout_tab ->{
              iv_logout_tab.setImageResource(R.drawable.active_logout_icon)
              logout_view.visibility = View.VISIBLE

              post_view.visibility = View.INVISIBLE
              chat_view.visibility = View.INVISIBLE
              notification_view.visibility = View.INVISIBLE
              setting_view.visibility = View.INVISIBLE

              iv_post_tab.setImageResource(R.drawable.inactive_post_icon)
              iv_chat_tab.setImageResource(R.drawable.inactive_chat_icon)
              iv_notification_tab.setImageResource(R.drawable.inactive_notification_icon)
              iv_setting_tab.setImageResource(R.drawable.inactive_setting_icon)
              logoutDialog()
             // replaceFragment(LogoutFragment.newInstance("",""),false,R.id.fragment_place)

          }

      }

    }




    fun terminal(type:String){
        if(type.equals("1")){
            addFragment(AllPostFragment.newInstance("",""),false,R.id.fragment_place)
        }else if(type.equals("2")){
            addFragment(AllRequestFragment.newInstance("",""),false,R.id.fragment_place)
        }else Toasty.error(this,"Something went wrong").show()
    }

    fun addFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentManager = supportFragmentManager
        val fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0)
        if (!fragmentPopped) {
            val transaction = fragmentManager.beginTransaction()
            transaction.add(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fm = supportFragmentManager
        var i = fm.backStackEntryCount
        while (i > 0) {
            fm.popBackStackImmediate()
            i--
        }
        val fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0)
        if (!fragmentPopped) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }
    }

    fun logoutDialog(){
        val logoutDialog = Dialog(this)
        logoutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        logoutDialog.setCancelable(false);
        logoutDialog.setContentView(R.layout.delete_post_layout)
        logoutDialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logoutDialog.text_title.text = "Are you sure want to logout"

        logoutDialog.tv_done.setOnClickListener( {
            logoutApp()

        })

        logoutDialog.tv_cancel_dialog.setOnClickListener({
            logoutDialog.dismiss()

        })
        logoutDialog.show()
    }

    var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {

        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click back again to exit",Toast.LENGTH_SHORT).show()

            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        } else {
            super.onBackPressed()
            return
        }
    }

    private fun logoutApp() {
        var session  = SessionManager(this)

        if (Utils.isConnectingToInternet(this)) {
            //spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.GET, WebServices.LogoutApp_URL,
                    Response.Listener { response ->

                        //spin_kit.visibility = View.GONE
                        var obj = JSONObject(response)
                        var status:String = obj.getString("status").toString()
                        var message:String = obj.get("message").toString()
                        if(status.equals("success")){
                            try {
                                session?.logout()
                                //Toasty.success(this,message).show()
                            } catch (e: JSONException) {
                                Toasty.error(this,"Something went wrong").show()
                                //spin_kit.visibility = View.GONE
                                e.printStackTrace()
                            }
                        }

                    },
                    Response.ErrorListener {
                        session?.logout()
                       // Toasty.error(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                        //spin_kit.visibility = View.GONE
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    var authToken = session?.user?.userDetail?.authToken.toString()
                    params.put("authToken", authToken)
                    return params
                }
            }


            val requestQueue = Volley.newRequestQueue(this)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(this, "Please Check internet connection.!").show()
        }
    }



}
