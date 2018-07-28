package com.elite.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.elite.R
import com.elite.WebService.WebServices
import com.elite.adapter.common.CategoryAdapter
import com.elite.app_session.SessionManager
import com.elite.custom_listner.getCategory
import com.elite.fcm_model.UserInfoFCM
import com.elite.helper.Constant
import com.elite.helper.Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE
import com.elite.helper.ValidData
import com.elite.model.CategoryInfo
import com.elite.model.Country
import com.elite.model.UserInfo
import com.elite.util.Utils
import com.elite.volleymultipart.AppHelper
import com.elite.volleymultipart.VolleyMultipartRequest
import com.elite.volleymultipart.VolleySingleton
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.mvc.imagepicker.ImagePicker
import es.dmoral.toasty.Toasty
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.dialog_for_category.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class RegisterActivity : AppCompatActivity(), View.OnClickListener, LocationListener,  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private val TAG = "Elite"
    private var INTERVAL = (1000 * 10).toLong()
    private var FASTEST_INTERVAL = (1000 * 5).toLong()
    internal var mLocationRequest: LocationRequest = LocationRequest()
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal var lat: Double? = null
    internal var lng:Double? = null
    internal var lmgr: LocationManager?=null

    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var isGPSEnable: Boolean = false
    private var sendcategory:String =""
    private var sendcategoryId = ""
    private var catId:String = ""
    var countryCode: String = ""
    private var codeId:String = ""
    private var countries = ArrayList<Country>()
    private var profileImageBitmap: Bitmap? = null
    private var address: String = ""
    var categoryAdapter: CategoryAdapter? = null
    private var catList:  ArrayList<CategoryInfo.AllCategoryListBean> = arrayListOf()

    private var storage: FirebaseStorage? = null
    private var app: FirebaseApp? = null
    var deviceToken:String = ""
    var session :SessionManager ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        session = SessionManager(this@RegisterActivity)


        if (Constant.usertype == 2) {
            iv_profile.setImageResource(R.drawable.agency)
            user_icon.setImageResource(R.drawable.agency_icon)
            ly_specialist.visibility = View.GONE
            ly_category.visibility = View.VISIBLE

        }
        else if (Constant.usertype == 1) {
            ly_specialist.visibility = View.VISIBLE
            ly_category.visibility = View.GONE
        }
        else if(Constant.usertype == 0){
            scrollView_register.visibility = View.GONE
            Toasty.error(this,"Something went wrong. Select user type again").show()
        }

        deviceToken = FirebaseInstanceId.getInstance().token.toString()
        app = FirebaseApp.getInstance()
        storage = FirebaseStorage.getInstance(app!!)

        tv_login.setOnClickListener(this)
        iv_profile.setOnClickListener(this)
        tv_sign_up.setOnClickListener(this)
        tv_country_code.setOnClickListener(this)
        tv_location.setOnClickListener(this)
        iv_back.setOnClickListener(this)
        ly_category.setOnClickListener(this)

        countries.addAll(Utils.loadCountries(this))
        GetCountryZipCode()

        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        lmgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun isValid(): Boolean {
        val v = ValidData()

        if (!v.isNullValue(ed_username)) {
            ed_username.setError("Please enter fullname")
            ed_username.requestFocus()
            return false;

        } else if (!v.isNullValue(ed_email)) {
            ed_email.setError("Enter email address")
            ed_email.requestFocus()
            return false;

        } else if (!v.isEmailValid(ed_email)) {
            ed_email.setError("Enter valid email address")
            ed_email.requestFocus()
            return false;

        } else if (!v.isNullValue(ed_password)) {
            ed_password.setError("Enter password")
            ed_password.requestFocus()
            return false;

        } else if (!v.isPasswordValid(ed_password)) {
            ed_password.setError("Password atleast 6 characters required")
            ed_password.requestFocus()
            return false;
        }
        else if (!v.isNullValue(ed_specialist) && Constant.usertype == 1) {
            ed_specialist.setError("Please enter specialist")
            ed_specialist.requestFocus()
            return false;
        }
        else if (!v.isNullValue(tv_specialist) && Constant.usertype == 2) {
            Toasty.error(this,"Please select category").show()
            return false;

        }
        else if (!v.isNullValue(ed_phone)) {
            ed_phone.setError("Enter phone number")
            ed_phone.requestFocus()
            return false;

        } else if (isphonevalid(ed_phone.text.toString(), codeId.toUpperCase()) == false) {
            ed_phone.setError("Enter vaild number")
            ed_phone.requestFocus()
            return false;
        } else if (!v.isNullValue(tv_location)) {
            Toasty.error(this, "Select location").show()
            return false;
        }
        return true
    }

    fun isphonevalid(phone: String, code: String): Boolean {
        val phoneUtil = PhoneNumberUtil.createInstance(this)
        var isValidet: Boolean = false
        try {
            val swissNumberProto = phoneUtil.parse(phone, code)
            isValidet = phoneUtil.isValidNumber(swissNumberProto) // returns true


        } catch (e: NumberParseException) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }
        return isValidet
    }

    private fun getPermissionAndPicImage() {
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

    private fun dialog() {
        val openDialog = Dialog(this)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setCancelable(false);
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
            openDialog.dismiss()

            var catName:String= sendcategory.trimEnd(',')
            catId= sendcategoryId.trimEnd(',')

            tv_specialist.text =   catName
        })

        openDialog.show()
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
                if (profileImageBitmap != null)
                    iv_profile.setImageBitmap(profileImageBitmap)
            }
        }
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            tv_location.setEnabled(true)
            if (resultCode == -1) {
                val place = PlaceAutocomplete.getPlace(this, data)
                val latLng = place.latLng
                address = place.address.toString()
                lat = latLng.latitude
                lng = latLng.longitude
                tv_location.setText(address).toString().trim()
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                PlaceAutocomplete.getStatus(this, data)
            }
        }
    }

    private fun SelectCountry() {
        val list = ArrayList<String>()
        for (country in countries) {
            list.add(country.country_name + " (+" + country.phone_code + ")")
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select your country");
        val mEntries = list.toTypedArray<CharSequence>()
        builder.setItems(mEntries) { dialog, position ->
            tv_country_code.text = String.format("+%s", countries[position].phone_code)
            countryCode = "+" + countries[position].phone_code
            codeId =  countries[position].code.toString()

            dialog.dismiss()
        }

        builder.setNegativeButton("cancel") { dialog, which -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
    }

    override fun onClick(p0: View?) {
        when (p0) {
            tv_login -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                return
            }
            tv_sign_up -> {
                if (isValid()) {
                    isInternetisGpsEnable()
                }
            }
            iv_profile -> {
                getPermissionAndPicImage()

                stopLocationUpdates()
                mGoogleApiClient?.disconnect()
            }
            tv_country_code -> {
                SelectCountry()

            }
            tv_location -> {
                try {
                    tv_location.setEnabled(false)
                    mGoogleApiClient?.disconnect()
                    stopLocationUpdates()
                    val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: GooglePlayServicesRepairableException) {
                } catch (e: GooglePlayServicesNotAvailableException) {
                }

            }
            ly_category -> {

                dialog()
                tv_specialist.isSelected = true
            }
            iv_back -> {
                onBackPressed()

            }
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        var intent = Intent(this ,WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun GetCountryZipCode(): String {
        var CountryID = ""
        var CountryZipCode = ""
        val manager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        CountryID = manager.simCountryIso.toUpperCase()
        if (CountryID == "") {
            tv_country_code.text = "+1"
            countryCode = CountryID.toString()
            codeId = CountryID
            return ""
        }
        for (i in countries.indices) {
            val country = countries[i]
            if (CountryID.equals(country.code, ignoreCase = true)) {
                CountryZipCode = countries[i].phone_code.toString()
                countryCode = "+" + country.phone_code
                codeId =  country.code.toString()
                tv_country_code.text = String.format("+%s", country.phone_code)
                break
            }
        }
        return CountryZipCode + " " + CountryID
    }

    fun doRegistration(context: Activity, ed_username: String, ed_email: String, ed_password: String,
                       ed_specialist: String, ed_phone: String, f_id: String) {
        if (Utils.isConnectingToInternet(context)) {
            spin_kit.visibility = View.VISIBLE
            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, WebServices.Registration_Url,Response.Listener<NetworkResponse> { response ->
                val resultResponse = String(response.data)
                try {

                    val jsonObject = JSONObject(resultResponse)
                    if (jsonObject != null) {

                        val gson = Gson()
                        var userinfo: UserInfo = gson.fromJson(resultResponse, UserInfo::class.java)

                        val status: String? = userinfo.status
                        val message: String? = userinfo.message
                        if (status == "success") {

                            var session: SessionManager? = SessionManager(this)
                            session?.saveOldPassword(ed_password)
                            session!!.createSession(userinfo)
                            //Toasty.success(this, "User Register Successfully", Toast.LENGTH_SHORT).show()
                            firebaseChatRegister(ed_email)


                        } else {
                            spin_kit.visibility = View.GONE
                            Toasty.error(this, message.toString().trim()  + "").show()
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
                    map.put("password", ed_password)
                    map.put("userType", ""+Constant.usertype)
                    map.put("deviceType", "2")
                    map.put("deviceToken", deviceToken)
                    if(Constant.usertype == 1){
                        map.put("specialist", ed_specialist)
                        map.put("categoryId", "")
                    }else if(Constant.usertype == 2){
                        map.put("categoryId", catId)
                        map.put("specialist", "")
                    }
                    map.put("contactNumber", ed_phone)
                    map.put("location", tv_location.text.toString())
                    map.put("latitude", ""+lat)
                    map.put("longitude", ""+lng)
                    map.put("countryCode", countryCode)
                    map.put("f_id", f_id)
                    return map
                }

                override fun getByteData(): MutableMap<String, DataPart> {
                    val params = HashMap<String, DataPart>()
                    if (profileImageBitmap != null) {
                        params.put("profileImage", VolleyMultipartRequest.DataPart("profilePic.jpg", AppHelper.getFileDataFromBitmap(profileImageBitmap), "image/jpeg"))
                    }
                    return params
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
                                Toasty.error(this, message.toString().trim()  + "").show()
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

    override fun onLocationChanged(p0: Location?) {
        lat = p0?.getLatitude()
        lng = p0?.getLongitude()
        if (lat != null && lng != null) {
            var str =  getCompleteAddressString(lat!!,lng!!)
            tv_location.text = str.trim()
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        }
    }

    override fun onConnected(p0: Bundle?) {
        if(lat == null && lng == null){
            startLocationUpdates()
        }

    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient?.disconnect()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    protected fun startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        Constant.ACCESS_FINE_LOCATION)
            } else {
                val pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
            }
        } else {
            val pendingResult = LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this) }
        Log.d(TAG, "Location update started ..............: ")
    }

    protected fun stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        Log.d(TAG, "Location update stopped .......................")
    }

    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                Log.w("My Current address", strReturnedAddress.toString())
            } else {
                Toasty.error(this,"No Address returned!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toasty.error(this,"Canont get Address!")
        }
        return strAdd
    }

    fun isInternetisGpsEnable(): Boolean {

        isGPSEnable = lmgr!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (Utils.isConnectingToInternet(this) && isGPSEnable) {
            var ed_username = ed_username.text.toString()
            var ed_email = ed_email.text.toString()
            var ed_password = ed_password.text.toString()
            var ed_specialist = ed_specialist.text.toString()
            var ed_phone = ed_phone.text.toString()

            if (lat != null && lng != null) {
                val currentAddress = getCompleteAddressString(lat!!, lng!!)
                if (currentAddress != "") {
                    address = currentAddress
                    //firebaseChatRegister(ed_email,"123456")
                    doRegistration(this, ed_username, ed_email, ed_password, ed_specialist, ed_phone,"")

                }
            }

        } else {
            if (!Utils.isConnectingToInternet(this)) {
                Toasty.error(this, "Please Check internet connection.!").show()
            } else {
                val ab = android.support.v7.app.AlertDialog.Builder(this)
                ab.setTitle(R.string.gps_not_enable)
                ab.setMessage(R.string.do_you_want_to_enable)
                ab.setPositiveButton(R.string.settings, DialogInterface.OnClickListener { dialog, which ->
                    isGPSEnable = true
                    val `in` = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(`in`)
                })

                ab.setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                    dialog.dismiss()
                }
                ab.show()
            }
        }
        return false
    }

    fun firebaseChatRegister(email: String) {
        var ed_username = ed_username.text.toString()
        var ed_email = ed_email.text.toString()
        var userinfo:UserInfo = session?.user!!

        spin_kit.visibility = View.VISIBLE
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, "123456")
                .addOnCompleteListener(this, { task ->
                    Log.e("TAG", "performFirebaseRegistration:onComplete:" + task.isSuccessful)
                    if (task.isSuccessful) {
                        var f_id = FirebaseAuth.getInstance().uid
                        spin_kit.visibility = View.GONE
                        addUserFirebaseDatabase(ed_email,ed_username,userinfo?.userDetail?.profileImage.toString(),userinfo?.userDetail?.userType.toString())

                    } else {
                        loginFirebaseDataBase(email)
                        spin_kit.visibility = View.GONE
                    }
                })
    }

    private fun loginFirebaseDataBase(email: String) {
        val ed_username = ed_username.text.toString()
        val ed_email = ed_email.text.toString()
        var userinfo:UserInfo = session?.user!!

        spin_kit.visibility = View.VISIBLE
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, "123456")
                .addOnCompleteListener(this@RegisterActivity) { task ->
                    Log.d("TAG", "performFirebaseLogin:onComplete:" + task.isSuccessful)
                    if (task.isSuccessful) {
                        var f_id = FirebaseAuth.getInstance().uid
                        spin_kit.visibility = View.GONE
                        addUserFirebaseDatabase(ed_email,ed_username,userinfo?.userDetail?.profileImage.toString(),userinfo?.userDetail?.userType.toString())
                    } else {
                        firebaseChatRegister(email)
                        spin_kit.visibility = View.GONE
                        Toasty.error(this,"Sorry FirebaseError Try Again").show()
                    }
                }
    }

    private fun addUserFirebaseDatabase(email: String, name: String, image: String, userType: String) {
        val firebaseToken = FirebaseInstanceId.getInstance().token
        val firebaseUid = FirebaseAuth.getInstance().uid
        val database = FirebaseDatabase.getInstance().reference
        var userinfo:UserInfo = session?.user!!
       // val user = UserInfoFCM(firebaseUid!!, email, name, firebaseToken!!,image,userType)
        var user  = UserInfoFCM()

        user.uid = userinfo.userDetail?.id!!
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
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toasty.error(this, "data not store at firebase server", Toast.LENGTH_SHORT).show()
                    }
                }
    }
}
