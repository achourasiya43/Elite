package com.elite.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.elite.R
import com.elite.WebService.WebServices
import com.elite.activity.LoginActivity
import com.elite.app_session.SessionManager
import com.elite.custom_listner.MyClickListner
import com.elite.fragment.agency_side.Finished_and_Interested_job_Fragment
import com.elite.helper.Helper
import com.elite.model.UserInfo
import com.elite.util.Utils
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_setting.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap


class SettingFragment : Fragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view:View =  inflater!!.inflate(R.layout.fragment_setting, container, false)

        view.ly_profile.setOnClickListener {
            addFragment(ProfileFragment.newInstance("",""),true,R.id.fragment_place)
        }

        var session = SessionManager(context!!)
        var type:String = session.user?.userDetail?.userType.toString()

        if(type.equals("1")){
            view.txt_finish_job.text = "Finished Jobs"
        }else if(type.equals("2")){
            view.txt_finish_job.text = "Finished & Interested job"
        }


        view.ly_change_password.setOnClickListener {
            var helper = Helper(context!!)
            helper.changePasswordDialog(object : MyClickListner {
                override fun getPassword(oldPassword: String, newPassword: String, openDialog: Dialog) {
                    var session = SessionManager(context!!)
                    var oldPasswordSession = session.getOldPassword

                    if(!oldPasswordSession.equals(oldPassword)){
                        Toasty.error(context!!,"Please enter valid old password").show()
                        return
                    }else if(oldPasswordSession.equals(newPassword)){
                        Toasty.error(context!!,"Old password and new dose not same").show()
                        return
                    }else{
                        changePassword(view,oldPassword,newPassword,session,openDialog)
                    }

                }
            })
        }

        view.ly_about_us.setOnClickListener {
            addFragment(AboutUsFragment.newInstance("",""),true,R.id.fragment_place)

        }

        view.ly_finish.setOnClickListener {
            addFragment(Finished_and_Interested_job_Fragment.newInstance("",""),true,R.id.fragment_place)
        }

        view.term_condition.setOnClickListener {
            addFragment(WebViewFragment.newInstance("",""),true,R.id.fragment_place)
        }

        var isNotifyStatus = session.getIsNotifyStauts

        if(isNotifyStatus.equals("1")){
            view.notification_toggle.isChecked = true
        }else {
            view.notification_toggle.isChecked = false
        }

        view.notification_toggle.setOnClickListener {
            if(view.notification_toggle.isChecked){
                notifationOnOff("1",session,view)

            }else{
                notifationOnOff("0",session,view)

            }
        }

        return view


    }

    companion object {

        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): SettingFragment {
            val fragment = SettingFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }


    fun addFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentManager = getFragmentManager()
        val fragmentPopped = fragmentManager!!.popBackStackImmediate(backStackName, 0)
        if (!fragmentPopped) {
            val transaction = fragmentManager.beginTransaction()
            transaction.add(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }
    }

    private fun notifationOnOff(status: String, session: SessionManager, view: View) {

        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.NotificationOnOff,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        var obj = JSONObject(response)
                        var status:String = obj.getString("status").toString()
                        var message:String = obj.get("message").toString()
                        if(status.equals("success")){
                            try {
                                if(message.equals("Off")){
                                    session?.saveIsNotificationActive("0")
                                    Toast.makeText(context!!,"Notification off",Toast.LENGTH_SHORT).show()
                                }
                                else if(message.equals("On")){
                                    session?.saveIsNotificationActive("1")
                                    Toast.makeText(context!!,"Notification on",Toast.LENGTH_SHORT).show()
                                }

                            } catch (e: JSONException) {
                                view.spin_kit.visibility = View.GONE
                                Toasty.error(context!!,"Something went wrong").show()
                                e.printStackTrace()
                            }
                        }
                    },
                    Response.ErrorListener {
                        view.spin_kit.visibility = View.GONE
                        Utils.sessionExDialog(context!!)
                        // Toasty.error(context!!, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("Is_notify", status)
                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    var authToken :String = session?.user?.userDetail?.authToken.toString()
                    params.put("authToken", authToken)
                    return params
                }
            }


            val requestQueue = Volley.newRequestQueue(context!!)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(context!!, "Please Check internet connection.!").show()
        }
    }


    private fun changePassword(view: View, oldPassword: String, newPassword: String, session: SessionManager, openDialog: Dialog) {

        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.ChangePassword,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            val gson = Gson()

                            var userinfo: UserInfo = gson.fromJson(response, UserInfo::class.java)

                            val status: String? = userinfo.status
                            val message: String? = userinfo.message
                            if (status == "success") {
                                session.saveOldPassword(newPassword)
                                openDialog.dismiss()
                                var intent  = Intent(activity,LoginActivity::class.java)
                                startActivity(intent)
                                activity?.finish()
                                Toasty.success(context!!, message+"", Toast.LENGTH_SHORT).show()

                            } else {
                              //  Toasty.error(context!!, message + "").show()
                            }

                        } catch (e: JSONException) {
                            view.spin_kit.visibility = View.GONE
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        Utils.sessionExDialog(context!!)
                        view.spin_kit.visibility = View.GONE
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("oldPassword", oldPassword)
                    params.put("newPassword", newPassword)


                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String,String>()
                    var authToken = session.user?.userDetail?.authToken.toString()
                    params.put("authToken",authToken)
                    return params
                }
            }


            val requestQueue = Volley.newRequestQueue(context!!)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(context!!, "Please Check internet connection.!").show()
        }
    }

}
