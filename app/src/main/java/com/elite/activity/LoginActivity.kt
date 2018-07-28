package com.elite.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.elite.R
import com.elite.WebService.WebServices
import com.elite.app_session.SessionManager
import com.elite.helper.Constant
import com.elite.helper.ValidData
import com.elite.model.UserInfo
import com.elite.util.Utils
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Window
import com.android.volley.*
import com.elite.adapter.common.CategoryAdapter
import com.elite.custom_listner.getCategory
import com.elite.fcm_model.UserInfoFCM
import com.elite.model.CategoryInfo
import com.elite.volleymultipart.VolleyMultipartRequest
import com.elite.volleymultipart.VolleySingleton
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.dialog_for_category.*
import org.json.JSONObject
import java.util.*


class LoginActivity : AppCompatActivity() {

    val RC_SIGN_IN = 101
    var mGoogleSignInClient : GoogleSignInClient?= null
    var categoryAdapter: CategoryAdapter? = null
    private var catList: ArrayList<CategoryInfo.AllCategoryListBean> = arrayListOf()
    private var sendcategory:String =""
    private var sendcategoryId = ""
    private var catId:String = ""

    var name:String = ""
    var email:String = ""
    var profileImg = ""
    var socialId = ""
    var DeviceToken = ""

    private var callbackManager: CallbackManager? = null

    private var mAuth:  FirebaseAuth ?= null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.g_token))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()
        DeviceToken = FirebaseInstanceId.getInstance().token.toString()

        FacebookSdk.sdkInitialize(getApplicationContext())
        callbackManager = CallbackManager.Factory.create()

        fb_login.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                    this, Arrays.asList("public_profile", "email"))
        }

        LoginManager.getInstance().registerCallback(callbackManager,object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult?) {
                spin_kit.visibility = View.VISIBLE


                val sSocialId = loginResult?.getAccessToken()?.getUserId()
                val refreshedToken = FirebaseInstanceId.getInstance().token

                val request = GraphRequest.newMeRequest(loginResult?.getAccessToken()) { `object`, response ->
                    try {
                        val jsonObject = JSONObject(`object`.toString())
                        var email_fb = ""
                        if (`object`.has("email")) {
                            email_fb = `object`.getString("email")
                        }

                        var socialId_ = `object`.getString("id")
                        val firstname = `object`.getString("first_name")
                        val lastname = `object`.getString("last_name")
                        val fullname = firstname + " " + lastname
                        val profileImage = "https://graph.facebook.com/$sSocialId/picture?type=large"
                        val deviceToken:String = FirebaseInstanceId.getInstance().token.toString()

                        val params = HashMap<String, String>()
                        params.put("deviceToken",deviceToken)

                        name =  fullname
                        email = email_fb
                        profileImg =  profileImage
                        socialId =   socialId_

                        if(socialId != null &&  !socialId.equals("null")){
                            ///handleFacebookAccessToken(loginResult?.getAccessToken()!!,name,profileImg)
                            doSocialRegistration(this@LoginActivity,name,email,"",socialId,"facebook",profileImg,"")


                        }


                    } catch (e: JSONException) {
                        e.printStackTrace()

                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id, first_name, last_name, email, picture")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                Toasty.error(this@LoginActivity,"Cancel login").show()
            }

            override fun onError(exception: FacebookException?) {
                if (exception is FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut()
                    }
                }
                exception?.printStackTrace()
            }

        })

        iv_google_sign.setOnClickListener {
            signIn();

        }

        tv_login.setOnClickListener( {
            var email: String = ed_email.text.toString()
            var password: String = ed_password.text.toString()

            if (isValid()) {
                LoginToServer(email,password)



            }
        })

        var session: SessionManager? = SessionManager(this)
        ed_email.setText(session!!.getemail)
        ed_password.setText(session.getpass)
        remindme.isChecked = session.getIschecked


        tv_sign_up.setOnClickListener( {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        })

        tv_forgot_pass.setOnClickListener( {
            var intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        })

        iv_back.setOnClickListener({
            onBackPressed()
        })

    }

    private fun dialog(f_id: String) {
        val openDialog = Dialog(this)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
       // openDialog.setCancelable(false);
        openDialog.setContentView(R.layout.dialog_for_category)
        openDialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        if(catList.size>0){
            categoryAdapter?.notifyDataSetChanged()
        }else{
            callCategory(openDialog)
        }

        categoryAdapter = CategoryAdapter(this, catList, object : getCategory {
            override fun getValue(cateInfo: CategoryInfo.AllCategoryListBean) {

                if (!cateInfo.ischecked) {
                    cateInfo.ischecked = true
                    sendcategory = cateInfo.name.toString() + "," + sendcategory
                    sendcategoryId = cateInfo.id.toString() + "," + sendcategoryId

                } else {
                    if (sendcategory.contains("" + cateInfo.name)) {
                        sendcategory = sendcategory.replace(cateInfo.name + ",", "")
                        sendcategoryId = sendcategoryId.replace(cateInfo.id + ",", "")
                    }
                    cateInfo.ischecked = false
                }
                categoryAdapter?.notifyDataSetChanged()
            }

        }, Constant.CheckBoxViewType)
        openDialog.recycler_view.adapter = categoryAdapter


        openDialog.done_action.setOnClickListener(View.OnClickListener {

            var catName:String= sendcategory.trimEnd(',')
            catId= sendcategoryId.trimEnd(',')
            if(socialId != null && !socialId.equals("null")) {
                if(!catId.equals(""))
                doSocialRegistration(this, name, email, catId, socialId, "google", profileImg, f_id)
            }
            if(catId.equals("")){
                Toasty.error(this,"Select category").show()
            }else openDialog.dismiss()
        })

        openDialog.show()
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient?.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        var intent  = Intent(this,WelcomeActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === RC_SIGN_IN) {


            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
        callbackManager?.onActivityResult(requestCode, resultCode, data)

    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            name =  account?.displayName.toString()
            email = account?.email.toString()
            profileImg =   account?.photoUrl.toString()
            socialId =    account?.id.toString()
            doSocialRegistration(this, name, email, "", socialId, "google", profileImg, "")

        } catch (e: ApiException) {
            Log.w("LoginActivity.TAG", "signInResult:failed code=" + e.statusCode)

        }

    }

    fun updateUI(account: GoogleSignInAccount?, f_id: String?){
        name =  account?.displayName.toString()
        email = account?.email.toString()
        profileImg =   account?.photoUrl.toString()
        socialId =    account?.id.toString()

        if(socialId != null &&  !socialId.equals("null")){
             doSocialRegistration(this, name, email, "", socialId, "google", profileImg, f_id!!)


        }

    }

    private fun LoginToServer(email: String, password: String) {
        var session: SessionManager? = SessionManager(this)

        if (remindme.isChecked) {
            if(!email.equals("") && !password.equals(""))
                session?.saveEmailPass(email, password, true)
        } else {
            session!!.uncheck()
        }

        if (Utils.isConnectingToInternet(this@LoginActivity)) {
            spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.Login_Url,
                    Response.Listener { response ->
                        spin_kit.visibility = View.GONE
                        try {
                            val gson = Gson()

                            var userinfo: UserInfo = gson.fromJson(response, UserInfo::class.java)

                            val status: String? = userinfo.status
                            val message: String? = userinfo.message
                            if (status == "success") {

                                session?.saveOldPassword(ed_password.text.toString().trim())
                                session?.createSession(userinfo)
                                val i: Int? = userinfo.userDetail?.userType?.toIntOrNull()
                                Constant.usertype = i!!
                                session?.saveIsNotificationActive(userinfo.userDetail?.Is_notify.toString())

                                loginFirebaseDataBase(email)



                            } else {
                                Toasty.error(this, message + "").show()
                            }

                        } catch (e: JSONException) {
                            spin_kit.visibility = View.GONE
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        Toasty.error(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                        spin_kit.visibility = View.GONE
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("email", email)
                    params.put("password", password)
                    params.put("userType", ""+Constant.usertype)
                    params.put("deviceToken", DeviceToken)
                    params.put("deviceType", "2")

                    return params
                }
            }


            val requestQueue = Volley.newRequestQueue(this)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(this, "Please Check internet connection.!").show()
        }
    }

    private fun isValid(): Boolean {
        val v = ValidData()

        if (!v.isNullValue(ed_email)) {
            ed_email.setError("Enter email address")
            ed_email.requestFocus()
            return false

        } else if (!v.isEmailValid(ed_email)) {
            ed_email.setError("Enter valid email address")
            ed_email.requestFocus()
            return false

        } else if (!v.isNullValue(ed_password)) {
            ed_password.setError("Please enter password")
            ed_password.requestFocus()
            return false

        } else if (!v.isPasswordValid(ed_password)) {
            ed_password.setError("Atleast 6 characters required")
            ed_password.requestFocus()
            return false
        }
        return true
    }

    fun doSocialRegistration(context: Activity, ed_username: String,
                             ed_email: String, ed_specialist: String, socialId: String, socialType: String, profileImg: String, f_id: String) {
        if (Utils.isConnectingToInternet(context)) {
            spin_kit.visibility = View.VISIBLE
            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, WebServices.Registration_Url, Response.Listener<NetworkResponse> { response ->
                val resultResponse = String(response.data)
                try {
                    spin_kit.visibility = View.GONE
                    val jsonObject = JSONObject(resultResponse)
                    if (jsonObject != null) {

                        val gson = Gson()
                        var userinfo: UserInfo = gson.fromJson(resultResponse, UserInfo::class.java)

                        val status: String? = userinfo.status
                        val message: String? = userinfo.message

                        if (status == "success") {
                            if(userinfo.userDetail?.userType.equals("1")){
                                var session: SessionManager? = SessionManager(this)
                                session!!.createSession(userinfo)

                                //data write
                                loginFirebaseDataBase(email)


                            }else if(userinfo.userDetail?.userType.equals("2")){
                                if(userinfo.userDetail?.categoryDetails?.size == 0){
                                    dialog(f_id)
                                }else {
                                    var session: SessionManager? = SessionManager(this)
                                    session!!.createSession(userinfo)

                                    loginFirebaseDataBase(email)
                                }
                            }
                            addUserFirebaseDatabase(email,name,userinfo.userDetail?.profileImage.toString(),userinfo.userDetail?.userType.toString(),userinfo.userDetail?.id.toString())

                        } else {
                            spin_kit.visibility = View.GONE
                            Toasty.error(this, message.toString().trim() + "").show()
                        }
                    }

                } catch (e: JSONException) {
                    spin_kit.visibility = View.GONE
                    Toasty.error(this, "something went wrong").show()
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                spin_kit.visibility = View.GONE
                val networkResponse = error.networkResponse
                var errorMessage = "Unknown error"
                if (networkResponse == null) {
                    if (error.javaClass == TimeoutError::class.java) {
                        errorMessage = "Request timeout"
                    } else if (error.javaClass == NoConnectionError::class.java) {
                        errorMessage = "Failed to connect server"
                    }
                } else {
                    spin_kit.visibility = View.GONE
                    String(networkResponse.data)

                }
                Log.i("Error", errorMessage)
                error.printStackTrace()
            }) {
                override fun getParams(): MutableMap<String, String> {
                    val map = HashMap<String, String>()
                    map.put("fullName", ed_username)
                    map.put("email", ed_email)
                    // map.put("password", ed_password)
                    map.put("userType", ""+Constant.usertype)
                    map.put("deviceType", "2")
                    map.put("deviceToken", DeviceToken)
                    if(Constant.usertype == 1){
                        map.put("specialist", ed_specialist)
                        map.put("categoryId", "")
                    }else if(Constant.usertype == 2){
                        map.put("categoryId", catId)
                        map.put("specialist", "")
                    }
                    map.put("contactNumber", "")
                    map.put("location", "")
                    map.put("latitude", "")
                    map.put("longitude", "")
                    map.put("countryCode", "")
                    map.put("profileImage", profileImg)

                    map.put("socialId",socialId)
                    map.put("socialType",socialType)
                    map.put("f_id",f_id)
                    return map
                }


            }
            multipartRequest.setRetryPolicy(DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
            VolleySingleton.getInstance(baseContext).addToRequestQueue(multipartRequest)
        } else {
            Toasty.error(this, "Please Check internet connection.!").show()

        }
    }

    private fun callCategory(openDialog: Dialog) {
        if (Utils.isConnectingToInternet(this)) {
            openDialog.progress_bar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.GET, WebServices.Category_url,
                    Response.Listener { response ->
                        openDialog.progress_bar.visibility = View.GONE
                        try {
                            val gson = Gson()
                            var categoryinfo: CategoryInfo = gson.fromJson(response, CategoryInfo::class.java)

                            val status: String? = categoryinfo.status
                            val message: String? = categoryinfo.message
                            if (status == "success") {

                                for (item in categoryinfo.allCategoryList!!){
                                    catList.add(item)
                                }
                                categoryAdapter!!.notifyDataSetChanged()

                            } else {
                                Toasty.error(this, message + "").show()
                            }

                        } catch (e: JSONException) {
                            openDialog.dismiss()
                            Toasty.error(this,"Something went wrong please try again later").show()
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        openDialog.progress_bar.visibility = View.GONE
                        openDialog.dismiss()
                        Toasty.error(this,"Something went wrong please try again later").show()
                    }) {
            }
            val requestQueue = Volley.newRequestQueue(this)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(this, "Please Check internet connection.!").show()
        }
    }

    private fun loginFirebaseDataBase(email: String) {
        spin_kit.visibility = View.VISIBLE
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, "123456")
                .addOnCompleteListener(this@LoginActivity) { task ->
                    Log.d("TAG", "performFirebaseLogin:onComplete:" + task.isSuccessful)
                    if (task.isSuccessful) {
                        spin_kit.visibility = View.GONE
                        //updateFirebaseToken(task.getResult().getUser().getUid(), token);
                         val intent = Intent(this, MainActivity::class.java)
                         startActivity(intent)
                         finish()
                    } else {
                        firebaseChatRegister(email)
                    }
                }
    }

    fun firebaseChatRegister(email: String) {
        spin_kit.visibility = View.VISIBLE
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, "123456")
                .addOnCompleteListener(this, { task ->
                    Log.e("TAG", "performFirebaseRegistration:onComplete:" + task.isSuccessful)
                    if (task.isSuccessful) {
                        spin_kit.visibility = View.GONE
                        //updateFirebaseToken(task.getResult().getUser().getUid(), token);
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        loginFirebaseDataBase(email)
                        //spin_kit.visibility = View.GONE
                    }
                })
    }

    private fun addUserFirebaseDatabase(email: String, name: String, image: String, userType: String,uid :String) {
        val firebaseToken = FirebaseInstanceId.getInstance().token
        val firebaseUid = FirebaseAuth.getInstance().uid
        val database = FirebaseDatabase.getInstance().reference

        // val user = UserInfoFCM(firebaseUid!!, email, name, firebaseToken!!,image,userType)
        var user  = UserInfoFCM()

        user.uid = uid
        user.firebaseId = firebaseUid.toString()
        user.email = email
        user.name = name
        user.firebaseToken = firebaseToken.toString()
        user.profilePic = image
        user.userType =userType

        database.child(Constant.ARG_USERS)
                .child( user.uid)
                .setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                       /* val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()*/
                    } else {
                        Toasty.error(this, "data not store at firebase server", Toast.LENGTH_SHORT).show()
                    }
                }
    }

}
