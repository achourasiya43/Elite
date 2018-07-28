package com.elite.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.elite.R
import com.elite.WebService.WebServices
import com.elite.adapter.common.CategoryAdapter
import com.elite.app_session.SessionManager
import com.elite.custom_listner.MyListner
import com.elite.custom_listner.getCategory
import com.elite.fcm_model.UserInfoFCM
import com.elite.helper.Constant
import com.elite.helper.Helper
import com.elite.helper.ValidData
import com.elite.model.CategoryInfo
import com.elite.model.Country
import com.elite.model.UserInfo
import com.elite.util.Utils
import com.elite.volleymultipart.AppHelper
import com.elite.volleymultipart.VolleyMultipartRequest
import com.elite.volleymultipart.VolleySingleton
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.mvc.imagepicker.ImagePicker
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_for_category.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap

class ProfileFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    private var countries = ArrayList<Country>()
    var categoryAdapter: CategoryAdapter? = null
    private var catList:  ArrayList<CategoryInfo.AllCategoryListBean> = arrayListOf()
    private var sendcategory:String =""
    private var sendcategoryId = ""
    private var catId:String = ""
    private var profileImageBitmap:Bitmap ?= null
    private var lat:Double  ?= null
    private var lng:Double  ?= null


    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view:View = inflater!!.inflate(R.layout.fragment_profile, container, false)
        countries.addAll(Utils.loadCountries(context!!))

        var session  = SessionManager(context!!)
        var userInfo:UserInfo ? = session.user

        disableComponents(view)
        setProfileValues(view, userInfo!!)


        view.tv_Update.setOnClickListener{

            view.tv_category.isSelected = true

            if(userInfo.userDetail?.userType.equals("1")){
                if(isValid(view,session)) UpdateProfile(WebServices.Update_Doctor_profile,userInfo!!,view)

            }else if(userInfo.userDetail?.userType.equals("2")){

                if(isValid(view,session)) UpdateProfile(WebServices.Update_Agency_profile,userInfo!!,view)

            }else Toasty.error(context!!,"Something went wrong").show()



        }

        view.iv_edit.setOnClickListener{
            view.tv_Update.visibility = View.VISIBLE
            view.iv_edit.visibility = View.GONE
            enableComponents(view)
        }

        view.tv_country_code.setOnClickListener{
            var helper = Helper(context!!)
            helper.SelectCountry(countries,object :MyListner{
                override fun getcountry(countryCode: String) {
                    view.tv_country_code.text = countryCode

                }
            })
        }

        view.iv_profile.setOnClickListener {
            getPermissionAndPicImage()
        }

        view.ly_location.setOnClickListener {
            try {
                view.tv_location.setEnabled(false)
                val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(context!! as Activity?)
                startActivityForResult(intent, Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE)
            } catch (e: GooglePlayServicesRepairableException) {
            } catch (e: GooglePlayServicesNotAvailableException) {
            }
        }

        view.ly_category.setOnClickListener {
            dialog(session)
            view.tv_category.isSelected = true
        }

        view.iv_back.setOnClickListener {
            activity?.onBackPressed()
        }
        return view
    }

    private fun setProfileValues(view:View,userInfo:UserInfo){
        var allcategory = ""

        if(userInfo?.userDetail?.userType.equals("1")){
            view.ly_specialist.visibility = View.VISIBLE
            view.ly_category.visibility = View.GONE

        }else if(userInfo.userDetail?.userType.equals("2")){
            view.ly_specialist.visibility = View.GONE
            view.ly_category.visibility = View.VISIBLE
        }


        view.ed_fullname.setText(userInfo.userDetail?.name)

        view.ed_email.setText(userInfo.userDetail?.email)
        view.ed_specialist.setText(userInfo?.userDetail?.specialist)
        view.ed_phone.setText(userInfo?.userDetail?.contactNumber)
        view.tv_location.text = userInfo?.userDetail?.location?.trim()
        view.tv_country_code.setText(userInfo.userDetail?.countryCode)

        view.tv_header_name.setText(userInfo?.userDetail?.name)

        for (args in userInfo?.userDetail?.categoryDetails!!){
            allcategory = args.name.toString()+","+ allcategory
            catId = args.id.toString()+","+ catId
        }
        catId= catId.trimEnd(',')
        allcategory= allcategory.trimEnd(',')
        view.tv_category.setText(allcategory)

        Picasso.with(context!!).load(userInfo?.userDetail?.profileImage)
                .into(view.iv_profile, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        view.progress_img.visibility = View.GONE
                    }

                    override fun onError() {

                    }
                })
       // Picasso.with(context!!).load(userInfo?.userDetail?.profileImage).into(view.iv_profile)

    }

    private fun enableComponents(view: View) {
        view.ed_fullname.isEnabled = true
        view.ed_specialist.isEnabled = true
        view.ly_category.isEnabled = true
        view.ed_phone.isEnabled = true
        view.tv_country_code.isEnabled = true
        view.ly_location.isEnabled = true
        view.iv_profile.isEnabled = true

        view.iv_drop_down.visibility = View.VISIBLE
        view.iv_camera_icon.visibility = View.VISIBLE
    }

    private fun disableComponents(view: View) {
        view.ed_fullname.isEnabled = false
        view.ed_specialist.isEnabled = false
        view.ly_category.isEnabled = false
        view.ed_phone.isEnabled = false
        view.tv_country_code.isEnabled = false
        view.ly_location.isEnabled = false
        view.iv_profile.isEnabled = false

        view.iv_drop_down.visibility = View.GONE
        view.iv_camera_icon.visibility = View.GONE
    }

    private fun getPermissionAndPicImage() {
        if (Build.VERSION.SDK_INT >= 23) {

            if (context!!.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 105)
            } else {
                ImagePicker.pickImage(this)
            }
        } else {
            ImagePicker.pickImage(this)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 234) {

                profileImageBitmap = ImagePicker.getImageFromResult(context!!, requestCode, resultCode, data)
                if (profileImageBitmap != null)
                    view!!.iv_profile.setImageBitmap(profileImageBitmap)
            }
        }

        if (requestCode == Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            view!!.tv_location.setEnabled(true)
            if (resultCode == -1) {
                val place = PlaceAutocomplete.getPlace(context!!, data)
                val latLng = place.latLng
                var address = place.address.toString()
                lat = latLng.latitude
                lng = latLng.longitude
                view!!.tv_location.setText(address).toString().trim()
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(context!!, data)
            }
        }
    }


    private fun dialog(session: SessionManager) {

        val openDialog = Dialog(context!!)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setCancelable(false);
        openDialog.setContentView(R.layout.dialog_for_category)
        openDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        if(catList.size>0){
            categoryAdapter?.notifyDataSetChanged()
        }else{
            callCategory(openDialog,session)

        }
        categoryAdapter = CategoryAdapter(context!!, catList, object : getCategory {
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
        openDialog.done_action.setOnClickListener( {
            openDialog.dismiss()

            var catName:String= sendcategory.trimEnd(',')
            catId = sendcategoryId.trimEnd(',')

            view!!.tv_category.text =   catName
        })

        openDialog.show()
    }

    fun UpdateProfile(Url:String,userInfo: UserInfo,view:View) {
        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, Url, Response.Listener<NetworkResponse> { response ->
                val resultResponse = String(response.data)
                try {
                    view.spin_kit.visibility = View.GONE
                    val jsonObject = JSONObject(resultResponse)
                    if (jsonObject != null) {

                        val gson = Gson()
                        var userinfo: UserInfo = gson.fromJson(resultResponse, UserInfo::class.java)


                        val status: String? = userinfo.status
                        var message  = userinfo.message
                        if (status == "success") {
                            view?.tv_header_name.setText(view.ed_fullname.text.toString())
                            view.tv_Update.visibility = View.GONE
                            disableComponents(view)

                            var session: SessionManager? = SessionManager(context!!)
                            session!!.createSession(userinfo)

                            updateProfile(view,session)
                            Toasty.success(context!!, ""+message?.trim(), Toast.LENGTH_SHORT).show()
                            view.iv_edit.visibility = View.VISIBLE

                        } else {
                            view.spin_kit.visibility = View.GONE
                            Toasty.error(context!!, message?.trim() + "").show()
                        }
                    }

                } catch (e: JSONException) {
                    view.spin_kit.visibility = View.GONE
                    Toasty.error(context!!, "something went wrong").show()
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                view.spin_kit.visibility = View.GONE
                Utils.sessionExDialog(context!!)
                val networkResponse = error.networkResponse
                var errorMessage = "Unknown error"
                if (networkResponse == null) {
                    if (error.javaClass == TimeoutError::class.java) {
                        errorMessage = "Request timeout"
                    } else if (error.javaClass == NoConnectionError::class.java) {
                        errorMessage = "Failed to connect server"
                    }
                } else {
                    view.spin_kit.visibility = View.GONE
                    val result = String(networkResponse.data)

                }
                Log.i("Error", errorMessage)
                error.printStackTrace()
            }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params.put("fullName", view.ed_fullname.text.toString())

                    if(userInfo.userDetail?.userType.equals("1")){
                        params.put("specialist", view.ed_specialist.text.toString().trim())
                    }else if(userInfo.userDetail?.userType.equals("2")){
                        params.put("categoryId", catId)
                    }

                    params.put("contactNumber",view.ed_phone.text.toString())
                    params.put("countryCode", view.tv_country_code.text.toString())
                    params.put("location",  view.tv_location.text.toString())
                    params.put("email",  view.ed_email.text.toString())


                    if(lat == null && lng == null){
                        params.put("latitude", userInfo.userDetail?.latitude.toString())
                        params.put("longitude", userInfo.userDetail?.longitude.toString())
                    }else{
                        params.put("latitude", ""+lat)
                        params.put("longitude", ""+lng)
                    }

                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    var authToken: String = userInfo.userDetail?.authToken.toString()

                    params.put("authToken",authToken)
                    return params
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
            VolleySingleton.getInstance(activity?.baseContext).addToRequestQueue(multipartRequest)
        } else {
            Toasty.error(context!!, "Please Check internet connection.!").show()

        }
    }

    private fun callCategory(openDialog: Dialog, session: SessionManager) {
        if (Utils.isConnectingToInternet(context!!)) {
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

                                for(args in session.user?.userDetail?.categoryDetails!!){
                                    args.ischecked = true
                                    var name = args.name

                                    for(ar in catList){
                                        if(ar.name.equals(name)){
                                            ar.ischecked = true
                                            sendcategory = ar.name.toString()+","+sendcategory
                                            sendcategoryId = ar.id.toString()+","+sendcategoryId
                                        }
                                    }
                                }
                                categoryAdapter!!.notifyDataSetChanged()

                            } else {
                                Toasty.error(context!!, message + "").show()
                            }

                        } catch (e: JSONException) {
                            e.printStackTrace()
                            openDialog.dismiss()
                            Toasty.error(context!!,"Something went wrong please try again later").show()
                            openDialog.progress_bar.visibility = View.GONE
                        }
                    },
                    Response.ErrorListener {
                        openDialog.dismiss()
                        Toasty.error(context!!,"Something went wrong please try again later").show()
                        openDialog.progress_bar.visibility = View.GONE
                    }) {
            }
            val requestQueue = Volley.newRequestQueue(context!!)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(context!!, getString(R.string.plz_check_internat_connection)).show()
        }
    }

    private fun isValid(view:View,session:SessionManager): Boolean {
        val v = ValidData()

        if (!v.isNullValue(view.ed_fullname)) {
            Toasty.error(context!!,getString(R.string.enter_full_name)).show()
            view.ed_fullname.requestFocus()
            return false

        }  else if (!v.isNullValue(view.ed_specialist) && session.user?.userDetail?.userType.equals("1")) {
            Toasty.error(context!!,getString(R.string.enter_specialist)).show()
            view.ed_specialist.requestFocus()
            return false
        }
        else if (!v.isNullValue(view.ed_email)) {
            view.ed_email.setError("Enter email address")
            view.ed_email.requestFocus()
            return false;

        } else if (!v.isEmailValid(view.ed_email)) {
            view.ed_email.setError("Enter valid email address")
            view.ed_email.requestFocus()
            return false;

        }
        else if (!v.isNullValue(view.tv_category) && session.user?.userDetail?.userType.equals("2")) {
            Toasty.error(context!!,getString(R.string.select_category)).show()
            return false

        }
        else if (!v.isNullValue(view.tv_country_code)) {
            Toasty.error(context!!,getString(R.string.enter_country_code)).show()
            view.ed_phone.requestFocus()
            return false

        } else if (!v.isNullValue(view.ed_phone)) {
            Toasty.error(context!!,getString(R.string.enter_number)).show()
            view.ed_phone.requestFocus()
            return false

        } else if (!v.isPhoneValid(view.ed_phone)) {
            Toasty.error(context!!,getString(R.string.valid_number)).show()
            view.ed_phone.requestFocus()
            return false
        } else if (!v.isNullValue(view.tv_location)) {
            Toasty.error(context!!,getString(R.string.location)).show()
            view.ed_phone.requestFocus()
            return false
        }
        return true
    }

    private fun updateProfile(view: View, session: SessionManager?) {


        var user  = UserInfoFCM()

        user.uid = session?.user?.userDetail?.id.toString()
        user.email = session?.user?.userDetail?.email.toString()
        user.name = view.ed_fullname.text.toString()
        user.firebaseToken = FirebaseInstanceId.getInstance().token!!
        user.profilePic = session?.user?.userDetail?.profileImage.toString()
        user.userType = session?.user?.userDetail?.userType.toString()

   /*     var userInfoFCM = UserInfoFCM (Uid!!, session?.user?.userDetail?.email.toString(),
        view.ed_fullname.text.toString(),FirebaseInstanceId.getInstance().token!!,session?.user?.userDetail?.profileImage.toString()
                ,session?.user?.userDetail?.userType.toString())
*/
        FirebaseDatabase.getInstance().reference.child(Constant.ARG_USERS).child(user.uid).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //Toast.makeText(context!!, "Profile update uuccessfully", Toast.LENGTH_SHORT).show()

            }
        }

    }
}
