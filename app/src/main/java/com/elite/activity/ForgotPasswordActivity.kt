package com.elite.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.elite.R
import com.elite.WebService.WebServices
import com.elite.app_session.SessionManager
import com.elite.helper.ValidData
import com.elite.model.UserInfo
import com.elite.util.Utils
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_forgot_password.*
import org.json.JSONException
import java.util.HashMap

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        tv_send_mail.setOnClickListener(View.OnClickListener {
           var email = ed_email.text.toString()
            if(isValid()){
                forgotPassword(email)
            }

        })

        iv_back.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun isValid(): Boolean {
        val v = ValidData()

        if (!v.isNullValue(ed_email)) {
            ed_email.setError("Enter email address")
            ed_email.requestFocus()
            return false;

        } else if (!v.isEmailValid(ed_email)) {
            ed_email.setError("Enter valid email address")
            ed_email.requestFocus()
            return false;
        }
        return true
    }
    private fun forgotPassword(email: String) {
        if (Utils.isConnectingToInternet(this)) {
            spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.Forgot_Password,
                    Response.Listener { response ->
                        spin_kit.visibility = View.GONE
                        //  System.out.println("#"+response);
                        try {

                            val gson = Gson()
                            var userinfo: UserInfo = gson.fromJson(response, UserInfo::class.java)

                            val status:String? = userinfo.status
                            val message:String? = userinfo.message
                            if (status == "success") {

                                Toasty.success(this, "Your password has been send to your registered email address", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }else{
                                Toasty.error(this,message+"").show()
                            }

                        } catch (e: JSONException) {
                            spin_kit.visibility = View.GONE
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        spin_kit.visibility = View.GONE
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("email", email)

                    return params
                }
            }


            val requestQueue = Volley.newRequestQueue(this)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(this,"Please Check internet connection.!").show()        }
    }
}
