package com.elite.fragment.doctor_side

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elite.R
import kotlinx.android.synthetic.main.fragment_looking_for.view.*
import com.elite.helper.Helper
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import java.util.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.elite.WebService.WebServices
import com.elite.adapter.common.CategoryAdapter
import com.elite.adapter.doctor.VacancyAdapter
import com.elite.app_session.SessionManager
import com.elite.custom_listner.*
import com.elite.helper.CalendarUtils
import com.elite.helper.Constant
import com.elite.helper.ValidData
import com.elite.model.DrPostDetails
import com.elite.model.CategoryInfo
import com.elite.model.FinishedInfo
import com.elite.model.LookingForInfo
import com.elite.util.Utils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class LookingForFragment : Fragment() , LocationListener,  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SeekBar.OnSeekBarChangeListener {

    private val TAG = "Elite"
    private var INTERVAL = (1000 * 10).toLong()
    private var FASTEST_INTERVAL = (1000 * 5).toLong()
    internal var mLocationRequest: LocationRequest = LocationRequest()
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal var lat: Double? = null
    internal var lng:Double? = null
    internal var lmgr: LocationManager?=null

    private var vacancyList = ArrayList<String>()
    private var vacancyAdapter : VacancyAdapter?= null

    var categoryAdapter: CategoryAdapter? = null
    private var catList:  ArrayList<CategoryInfo.AllCategoryListBean> = arrayListOf()

    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var post_id: String? = ""
    private var mParam2: String? = ""
    private var fromDate:DatePickerDialog ? = null
    private var isExpand: Boolean = true
    private var isExpandVacancy: Boolean = true
    private var catId : String = ""
    private var drListDetails : DrPostDetails ? = null
    private var day:String = ""
    private var month:String = ""
    private var currentYear:String = ""
    private var helper:Helper ?= null
    private var fromTime:String  = ""
    private var toTime:String  = ""
    private var cal:Calendar?=null
    private   var  lookingFor = 0
    var finishedInfo = FinishedInfo()
    var pro:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            post_id = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
            lookingFor = arguments!!.getInt("lookingFor")
            finishedInfo = arguments!!.getSerializable("finishedInfo") as FinishedInfo
        }

        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(context!!)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        lmgr = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(id: String, param2: String, lookingFor: Int, finishedInfo: FinishedInfo): LookingForFragment {
            val fragment = LookingForFragment()
            val args = Bundle()
            args.putSerializable(ARG_PARAM1, id)
            args.putString(ARG_PARAM2, param2)
            args.putInt("lookingFor", lookingFor)
            args.putSerializable("finishedInfo",finishedInfo)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        helper = Helper(context!!)

        if(post_id != null && !post_id.equals("")){

            getDrDetails(view,post_id)

        }else   view?.scrollView?.visibility = View.VISIBLE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var view: View =  inflater!!.inflate(R.layout.fragment_looking_for, container, false)
        view.seek_bar!!.setOnSeekBarChangeListener(this)
        view.seek_bar.setProgress(0);
        view.seek_bar.incrementProgressBy(100);
        view.seek_bar.setMax(10000)

        callCategory()

        for (list in 1..10){
            vacancyList.add(""+list)
        }

        if(lookingFor == Constant.LookingForHome){
            view.txt_finish_by.visibility = View.GONE
            view.finish_by_dr.visibility = View.GONE

        }else if(lookingFor == Constant.LookingFoSetting){
            view.txt_finish_by.visibility = View.VISIBLE
            view.finish_by_dr.visibility = View.VISIBLE
            view.iv_edit.visibility = View.GONE
            view.finish_dialog.visibility = View.GONE

            view.name.text = finishedInfo.name
            view.phone.text = finishedInfo.contactNumber
            if(finishedInfo.profileImage != null && !finishedInfo.profileImage.equals("")){
                Picasso.with(context).load(finishedInfo.profileImage).placeholder(R.drawable.app_icon).into(view.profile_img)
            }

            view.txt_interest_agency.visibility = View.GONE

        }


        view.ly_job_timing.setOnClickListener {

            helper?.getJobTiming(fromTime,toTime,view.tv_timing,object : getDatePicker {
                override fun DatePicker(fromDate: String, toDate: String) {
                    view.tv_timing.text = fromDate + " to " + toDate
                    fromTime = fromDate
                    toTime = toDate

                }
            })
        }

        view.ly_date.setOnClickListener {
            var cal = Calendar.getInstance()
            if(view.tv_show_date.text.equals("")){
                setDateField(view.tv_show_date,cal)
            }else{
                setDateToCalender(view.tv_show_date,cal)

            }

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

        view.finish_dialog.setOnClickListener {
            for(value in drListDetails?.postList!!)
            helper?.finishedDialog(value.id.toString(),view,object : FinishedJobListner{
                override fun successFinishedJob() {
                    replaceFragment(AllPostFragment.newInstance("",""),false,R.id.fragment_place)
                }

            })

        }

        view.delete_job.setOnClickListener {
            for(value in drListDetails?.postList!!)
                if(value.id.toString() != null && !value.id.toString().equals("")){

                    helper?.deletePostDialog(context!!,view,value.id.toString(),object :Delete_Dialog{
                        override fun deleteDialog(responce: String) {

                            var jsonObject = JSONObject(responce)
                            var status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")

                            if (status == "success") {
                                Toasty.success(context!!, message + "").show()
                                replaceFragment(AllPostFragment.newInstance("",""),false,R.id.fragment_place)

                            } else {
                                Toasty.error(context!!, message + "").show()
                            }
                        }
                    })
                }
        }

        view.iv_edit.setOnClickListener {
            editableComponents(view)
        }

        view.iv_update_post.setOnClickListener {
            for(value in drListDetails?.postList!!)
                if(value.id != null){

                    if (view != null) {
                        val jobTitle:String = view.tv_list_item.text.toString().trim()
                        val numberOfVacancy:String = view.tv_vacancy_item.text.toString().trim()
                        var jobType = ""

                        if(view.radio_button_fulltime.isChecked){
                            jobType = "full_time"

                        }else if(view.radio_button_parttime.isChecked){
                            jobType = "part_time"
                        }

                        val qualification:String = view.ed_qualification.text.toString().trim()

                        val date:String = view.tv_show_date.text.toString().trim()

                        val time:String = view.tv_timing.text.toString().trim()

                        val salary:String = pro.toString()

                        val description:String = view.ed_desciption.text.toString().trim()

                        val location:String = view.tv_location.text.toString().trim()

                        val latitude =  value.latitude.toString().trim()
                        val longitude =  value.longitude.toString().trim()
                        val categoryId =  value.CatId.toString().trim()
                        val postId:String = value.id.toString().trim()

                        if(isValid(view,jobType)){
                            UpdateNewVacancy(view,postId,jobTitle,numberOfVacancy,jobType,qualification,date,
                                    time,salary,description,location,latitude,longitude,categoryId)
                        }}
                }

        }

        view.txt_interest_agency.setOnClickListener {
            for(value in drListDetails?.postList!!)
                addFragment(AgenciesListFragment.newInstance(value.id.toString(),""),true,R.id.fragment_place)

        }

        categoryAdapter = CategoryAdapter(context!!, catList, object : getCategory {
            override fun getValue(cateInfo: CategoryInfo.AllCategoryListBean) {
                view.recycler_view.visibility = View.GONE

                view.tv_list_item.text = cateInfo.name
                catId = cateInfo.id.toString()
                view.tv_category_item.visibility = View.VISIBLE
                isExpand = true
                view.expand_arrow_job.setImageResource(R.drawable.expand_arrow_icon)

            }

        }, Constant.ForLookingViewType)

        view.recycler_view.adapter = categoryAdapter

        view.recycler_view.visibility = View.GONE
        view.tv_category_item.visibility = View.GONE

        view.recycler_view_vacancy.visibility = View.GONE
        view.ly_vacancy_item.visibility = View.GONE

        vacancyAdapter = VacancyAdapter(context!!, vacancyList, object : VacancyListner {
            override fun getValue(value: String) {
                view.recycler_view_vacancy.visibility = View.GONE
                view.tv_vacancy_item.text = value + " " + "Vacancy"
                view.ly_vacancy_item.visibility = View.VISIBLE
                isExpandVacancy = true
                view.expand_arrow_vacancy.setImageResource(R.drawable.expand_arrow_icon)
            }

        })
        view.recycler_view_vacancy.adapter = vacancyAdapter


        view.job_vacancy_expand.setOnClickListener {
            if(isExpandVacancy){
                isExpandVacancy = false
                view.recycler_view_vacancy.visibility = View.VISIBLE
                view.ly_vacancy_item.visibility = View.GONE
                view.expand_arrow_vacancy.setImageResource(R.drawable.collage_arrow_icon)
                view.tv_vacancy_item.setText("")

            }else{
                isExpandVacancy = true
                view.recycler_view_vacancy.visibility = View.GONE
                view.ly_vacancy_item.visibility = View.GONE
                view.expand_arrow_vacancy.setImageResource(R.drawable.expand_arrow_icon)
                view.tv_vacancy_item.setText("")

            }
        }

        view.job_category_expand.setOnClickListener {
            if(isExpand){
                isExpand = false
                view.recycler_view.visibility = View.VISIBLE
                //view.recycler_view.animation = android.animation.Animator.
                view.tv_category_item.visibility = View.GONE
                view.expand_arrow_job.setImageResource(R.drawable.collage_arrow_icon)
                view.tv_list_item.setText("")

            }else{
                isExpand = true
                view.recycler_view.visibility = View.GONE
                view.tv_category_item.visibility = View.GONE
                view.expand_arrow_job.setImageResource(R.drawable.expand_arrow_icon)
                view.tv_list_item.setText("")
            }
        }

        view.iv_back.setOnClickListener {
            activity?.onBackPressed()
        }

        view.tv_post.setOnClickListener {

            val jobTitle:String = view.tv_list_item.text.toString().trim()
            val numberOfVacancy:String = view.tv_vacancy_item.text.toString().trim()
            var jobType = ""

            if(view.radio_button_fulltime.isChecked){
                jobType = "full_time"

            }else if(view.radio_button_parttime.isChecked){
                jobType = "part_time"
            }

            val qualification:String = view.ed_qualification.text.toString().trim()

            val date:String = view.tv_show_date.text.toString().trim()

            val time:String = view.tv_timing.text.toString().trim()

            val salary:String =  pro.toString()

            val description:String = view.ed_desciption.text.toString().trim()

            val location:String = view.tv_location.text.toString().trim()

            if(isValid(view,jobType)){
                postNewVacancy(view,jobTitle,numberOfVacancy,jobType,qualification,date,time,salary,description,location)
            }


        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            view?.tv_location?.setEnabled(true)
            if (resultCode == -1) {
                val place = PlaceAutocomplete.getPlace(context!!, data)
                val latLng = place.latLng
                var address = place.address.toString()
                lat = latLng.latitude
                lng = latLng.longitude
                view?.tv_location?.setText(address).toString().trim()
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(context!!, data)
            }
        }
    }

    private fun setDateField(view: TextView, cal: Calendar) {

        fromDate = DatePickerDialog.newInstance({ datePickerDialog, year, monthOfYear, dayOfMonth ->
            var returnDate = ""+ dayOfMonth + "-" + (monthOfYear + 1) + "-" + year.toString()
            view.setText(returnDate)
            day = ""+dayOfMonth
            month = ""+ (monthOfYear+1)
            currentYear = ""+year
        }, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH),  cal.get(Calendar.YEAR))

        fromDate?.setMinDate(Calendar.getInstance())
        fromDate?.show(activity!!.fragmentManager, "")
        fromDate?.setAccentColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
        fromDate?.setOnCancelListener({
            Log.d("TimePicker", "Dialog was cancelled")
            fromDate?.dismiss()
        })
    }

    private fun setDateToCalender(tvDate: TextView, cal: Calendar) {

        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {

                var returnDate = ""+ dayOfMonth + "-" + (monthOfYear + 1) + "-" + year.toString()

                var dateformate = ""+ dayOfMonth+"/"+(monthOfYear+1)+"/"+year

                val curFormater = SimpleDateFormat("dd/MM/yyyy")
                val dateObj = curFormater.parse(dateformate)

                if(CalendarUtils.isPastDay(dateObj)){
                    Toasty.error(context!!,"You can not select previous date.").show()
                    return;
                }else{
                    day = ""+dayOfMonth
                    month = ""+ (monthOfYear+1)
                    currentYear = ""+year
                }

                tvDate.setText(returnDate);
            }



        }


        val dialog = DatePickerDialog.newInstance(dateSetListener, currentYear. toInt(),month.toInt()-1, day.toInt())
        //dialog?.setMinDate(Calendar.getInstance())
        dialog?.setAccentColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
        dialog?.setOnCancelListener({
            Log.d("TimePicker", "Dialog was cancelled")
            dialog?.dismiss()
        })
        dialog?.show(activity?.fragmentManager, "")
    }

    override fun onLocationChanged(p0: Location?) {
        lat = p0?.latitude
        lng = p0?.longitude
        if (lat != null && lng != null) {
            val str =  getCompleteAddressString(lat!!,lng!!)
            view?.tv_location?.text = str.trim()
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        }
    }

    override fun onConnected(p0: Bundle?) {
        if(lat == null && lng == null){
            if(post_id.equals("")){
            startLocationUpdates()
        }


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

    protected fun stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        Log.d(TAG, "Location update stopped .......................")
    }

    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(context!!, Locale.getDefault())
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
                Toasty.error(context!!,"No Address returned!").show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toasty.error(context!!,"Canont get Address!").show()
        }
        return strAdd
    }

    protected fun startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (context!!.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
        pro = progress
        pro = pro / 100
        pro = pro * 100
        view?.tv_progress_salary?.text = ""+ pro +" USD"

    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {

    }

    private fun callCategory() {
        if (Utils.isConnectingToInternet(context!!)) {
            // openDialog.progress_bar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.GET, WebServices.Category_url,
                    Response.Listener { response ->
                        //openDialog.progress_bar.visibility = View.GONE
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
                                Toasty.error(context!!, message + "").show()
                            }

                        } catch (e: JSONException) {
                            e.printStackTrace()
                            //openDialog.dismiss()
                            Toasty.error(context!!,"Something went wrong please try again later").show()
                            //openDialog.progress_bar.visibility = View.GONE
                        }
                    },
                    Response.ErrorListener {
                        // openDialog.dismiss()
                        Toasty.error(context!!,"Something went wrong please try again later").show()
                        //  openDialog.progress_bar.visibility = View.GONE
                    }) {
            }
            val requestQueue = Volley.newRequestQueue(context!!)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(context!!, "Please Check internet connection.!").show()
        }
    }

    private fun postNewVacancy(view: View, jobTitle: String, numberOfVacancy: String, jobType: String, qualification: String, date: String, time: String, salary: String, description: String, location: String) {

        if (Utils.isConnectingToInternet(context!!)) {
            var session: SessionManager? = SessionManager(context!!)
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.PostNewVacancy,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            val gson = Gson()

                            var lookingfor: LookingForInfo = gson.fromJson(response, LookingForInfo::class.java)

                            val status: String? = lookingfor.status
                            val message: String? = lookingfor.message
                            if (status == "success") {
                                Toasty.success(context!!, message + "").show()
                                replaceFragment(AllPostFragment.newInstance("",""),false,R.id.fragment_place)
                            } else {
                                Toasty.error(context!!, message + "").show()
                            }

                        } catch (e: JSONException) {
                            view.spin_kit.visibility = View.GONE
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        view.spin_kit.visibility = View.GONE
                        Utils.sessionExDialog(context!!)
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("jobTitle", jobTitle)
                    params.put("vacancyNumber", numberOfVacancy+" "+"Vacancy")
                    params.put("jobType", jobType)
                    params.put("qualification", qualification)
                    params.put("need_from_date", date)
                    params.put("jobTiming",time)
                    params.put("salary", salary)
                    params.put("description", description)
                    params.put("location", view.tv_location.text.toString())
                    params.put("latitude", ""+lat)
                    params.put("longitude", ""+lng )
                    params.put("categoryId", catId)

                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String,String>()
                    val authToken = session?.user?.userDetail?.authToken.toString()
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

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fm = activity!!.supportFragmentManager
        var i = fm.backStackEntryCount
        /*   while (i > 0) {
               fm.popBackStackImmediate()
               i--
           }*/
        val fragmentPopped = fragmentManager?.popBackStackImmediate(backStackName, 0)
        if (!fragmentPopped!!) {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(containerId, fragment, backStackName)?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            if (addToBackStack)
                transaction?.addToBackStack(backStackName)
            transaction?.commit()
        }
    }

    private fun isValid(view: View, jobType: String): Boolean {
        val v = ValidData()

        if (!v.isNullValue(view.tv_list_item)) {
            Toasty.error(context!!,"Select job category").show()
            return false

        } else if (!v.isNullValue(view.tv_vacancy_item)) {
            Toasty.error(context!!,"Select number of vacancy").show()
            return false

        } else if (jobType.equals("")) {
            Toasty.error(context!!,"Select type of job").show()
            return false

        } else if (!v.isNullValue(view.ed_qualification)) {
            Toasty.error(context!!,"Enter your qualification").show()
            return false
        }else if (!v.isNullValue(view.tv_show_date)) {
            Toasty.error(context!!,"Select date").show()
            return false
        }else if (!v.isNullValue(view.tv_timing)) {
            Toasty.error(context!!,"Select time").show()
            return false
        }else if (!v.isNullValue(view.tv_progress_salary)) {
            Toasty.error(context!!,"please set expected salary").show()
            return false
        }else if (view.tv_progress_salary.text.equals("0 USD")) {
            Toasty.error(context!!,"please set expected salary ").show()
            return false
        }
        else if (!v.isNullValue(view.ed_desciption)) {
            Toasty.error(context!!,"Enter job description").show()
            return false
        }
        else if (!v.isNullValue(view.tv_location)) {
            Toasty.error(context!!,"Select location").show()
            return false
        }
        return true
    }

    private fun nonEditableComponents(view: View, drListDetails: DrPostDetails.PostListBean){

        view.radio_group.visibility = View.GONE
        view.count_char.visibility = View.GONE

        if(lookingFor != Constant.LookingFoSetting){
            view.iv_edit.visibility = View.VISIBLE
            view.txt_interest_agency.visibility = View.VISIBLE
        }

        view.iv_update_post.visibility = View.GONE

        view.txt_fill_form.visibility = View.GONE


        view.job_category_expand.setBackgroundColor(ContextCompat.getColor(context!!,R.color.medium_gray))
        view.text_job_category.setTextColor(ContextCompat.getColor(context!!,R.color.colorPrimaryDark))
        view.expand_arrow_job.visibility  = View.GONE
        view.job_category_expand.isEnabled = false

        view.job_vacancy_expand.setBackgroundColor(ContextCompat.getColor(context!!,R.color.medium_gray))
        view.text_vacancy_number.setTextColor(ContextCompat.getColor(context!!,R.color.colorPrimaryDark))
        view.expand_arrow_vacancy.visibility  = View.GONE
        view.job_vacancy_expand.isEnabled = false

        if(view.radio_button_fulltime.isChecked){
            view.radio_button_fulltime.isEnabled = false
            view.radio_button_parttime.visibility = View.GONE

            view.selectd_fulltime.visibility = View.VISIBLE
            view.selectd_parttime.visibility = View.GONE
        }
        else if(view.radio_button_parttime.isChecked){
            view.radio_button_parttime.isEnabled = false
            view.radio_button_fulltime.visibility = View.GONE

            view.selectd_fulltime.visibility = View.GONE
            view.selectd_parttime.visibility = View.VISIBLE
        }
        view.ed_qualification.isEnabled = false
        view.ly_date.isEnabled = false
        view.ly_job_timing.isEnabled = false
        view.seek_bar.isEnabled = false
        view.ed_desciption.isEnabled = false
        view.ly_location.isEnabled = false

        view.ly_finish_delete.visibility = View.VISIBLE
        view.tv_post.visibility = View.GONE

        setValueComnants(view, drListDetails)
    }

    private fun editableComponents(view:View){
        view.count_char.visibility = View.VISIBLE

        view.radio_group.visibility = View.VISIBLE
        view.selectd_fulltime.visibility = View.GONE
        view.selectd_parttime.visibility = View.GONE

        view.iv_edit.visibility = View.GONE
        view.iv_update_post.visibility = View.VISIBLE

        view.txt_fill_form.visibility = View.VISIBLE
        view.txt_interest_agency.visibility = View.GONE

     /*   view.job_category_expand.setBackgroundColor(ContextCompat.getColor(context!!,R.color.colorPrimaryDark))
        view.text_job_category.setTextColor(ContextCompat.getColor(context!!,R.color.white))
        view.expand_arrow_job.visibility  = View.VISIBLE*/
        view.job_category_expand.isEnabled = false

        view.job_vacancy_expand.setBackgroundColor(ContextCompat.getColor(context!!,R.color.colorPrimaryDark))
        view.text_vacancy_number.setTextColor(ContextCompat.getColor(context!!,R.color.white))
        view.expand_arrow_vacancy.visibility  = View.VISIBLE
        view.job_vacancy_expand.isEnabled = true

        if(view.radio_button_fulltime.isChecked){
            view.radio_button_fulltime.isEnabled = true
            view.radio_button_parttime.visibility = View.VISIBLE

        }
        else if(view.radio_button_parttime.isChecked){
            view.radio_button_parttime.isEnabled = true
            view.radio_button_fulltime.visibility = View.VISIBLE
        }
        view.ed_qualification.isEnabled = true
        view.ly_date.isEnabled = true
        view.ly_job_timing.isEnabled = true
        view.seek_bar.isEnabled = true
        view.ed_desciption.isEnabled = true
        view.ly_location.isEnabled = true
        view.ly_finish_delete.visibility = View.GONE
    }

    private fun setValueComnants(view:View, drListDetails: DrPostDetails.PostListBean) {
        view.radio_group.visibility = View.GONE
        view.radio_group.visibility = View.GONE

        val CurrentString = drListDetails.need_from_date.toString()
        if(!CurrentString.contains("/")){
            val separated = CurrentString.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            day = separated[0]
            month = separated[1]
            currentYear = separated[2]

        }else{
            val separated = CurrentString.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            day = separated[0]
            month = separated[1]
            currentYear = separated[2]
        }

        view.txt_interested_agency.text = "Interested "+drListDetails.interested_count+" agency"

        view.tv_category_item.visibility = View.VISIBLE
        view.tv_list_item.text = drListDetails.job_title

        view.ly_vacancy_item.visibility = View.VISIBLE
        view.tv_vacancy_item.text = drListDetails.vacancy_number

        if(drListDetails.job_type.equals("full_time")){
            view.radio_button_fulltime.isChecked = true
            view.radio_button_parttime.visibility = View.GONE

            view.selectd_fulltime.visibility = View.VISIBLE
            view.selectd_parttime.visibility = View.GONE
        }
        else if(drListDetails.job_type.equals("part_time")){
            view.radio_button_parttime.isChecked = true
            view.radio_button_fulltime.visibility = View.GONE

            view.selectd_fulltime.visibility = View.GONE
            view.selectd_parttime.visibility = View.VISIBLE
        }
        view.ed_qualification.setText(drListDetails.qualification)
        view.tv_show_date.text = drListDetails.need_from_date
        view.tv_timing.text = drListDetails.job_timing

        val CurrentTime = drListDetails.job_timing.toString()
        val timeSeperator = CurrentTime.split(" to ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        fromTime = timeSeperator[0]
        toTime = timeSeperator[1]

        view.tv_progress_salary.text = drListDetails.salary

        val salary = drListDetails.salary?.replace("[^0-9.]".toRegex(), "")

        view.seek_bar.setProgress(salary!!.toInt())
        view.ed_desciption.setText(drListDetails.description)
        view.tv_location.text = drListDetails.location
        //lat = drListDetails.latitude?.toDouble()
        //lng = drListDetails.longitude?.toDouble()

        if(!drListDetails.finished_by.equals("0")){
            view.finish_dialog.visibility = View.GONE
            view.iv_edit.visibility = View.GONE
        }


    }

    private fun UpdateNewVacancy(view: View, postId:String,jobTitle: String, numberOfVacancy: String,
                                 jobType: String, qualification: String,
                                 date: String, time: String, salary: String, description: String,
                                 location: String, latitude: String, longitude: String, categoryId: String) {
        if (Utils.isConnectingToInternet(context!!)) {
            var session: SessionManager? = SessionManager(context!!)
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.UpdateJob,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            val gson = Gson()

                            var lookingfor: LookingForInfo = gson.fromJson(response, LookingForInfo::class.java)

                            val status: String? = lookingfor.status
                            val message: String? = lookingfor.message
                            if (status == "success") {
                                for(value in drListDetails?.postList!!)
                                    nonEditableComponents(view,value)
                                view.iv_edit.visibility = View.GONE
                                view.iv_update_post.visibility = View.VISIBLE

                                Toasty.success(context!!, message + "").show()
                                replaceFragment(AllPostFragment.newInstance("",""),false,R.id.fragment_place)
                            } else {
                                Toasty.error(context!!, message + "").show()
                            }

                        } catch (e: JSONException) {
                            view.spin_kit.visibility = View.GONE
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        view.spin_kit.visibility = View.GONE
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("postId", postId)
                    params.put("jobTitle", jobTitle)
                    params.put("vacancyNumber", numberOfVacancy)
                    params.put("jobType", jobType)
                    params.put("qualification", qualification)
                    params.put("need_from_date", date)
                    params.put("jobTiming",time)
                    params.put("salary", salary)
                    params.put("description", description)

                    params.put("location", location)

                    if(lat === null || lat == 0.0){
                        //params.put("latitude", ""+latitude)
                    }else{
                        params.put("latitude", ""+lat)
                    }

                    if(lng === null ||  lng == 0.0){
                        // params.put("latitude", ""+longitude)
                    }else{
                        params.put("longitude", ""+lng)
                    }

                    if(catId.equals("")){
                        params.put("categoryId", categoryId)
                    }else params.put("categoryId", catId)

                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String,String>()
                    val authToken = session?.user?.userDetail?.authToken.toString()
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
    fun addFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentManager = activity!!.supportFragmentManager
        val fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0)
        if (!fragmentPopped) {
            val transaction = fragmentManager.beginTransaction()
            transaction.add(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }


    }

    private fun getDrDetails(view: View, post_id: String?) {
        var session = SessionManager(context!!)

        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.DrDetails,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            val gson = Gson()

                            drListDetails  = gson.fromJson(response, DrPostDetails::class.java)

                            val status: String? = drListDetails?.status
                            val message: String? = drListDetails?.message
                            if (status == "success") {
                                view.no_record.visibility = View.GONE
                                view.scrollView.visibility = View.VISIBLE
                                for(value in drListDetails?.postList!!)
                                    nonEditableComponents(view,value)
                            } else {
                                view.no_record.visibility = View.VISIBLE

                                Toasty.error(context!!, message + "").show()
                            }

                        } catch (e: JSONException) {
                            view.spin_kit.visibility = View.GONE
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        view.spin_kit.visibility = View.GONE
                        Utils.sessionExDialog(context!!)
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = java.util.HashMap<String, String>()
                    params.put("post_id", post_id.toString())
                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    var param = java.util.HashMap<String, String>()
                    var authToken : String =  session.user?.userDetail?.authToken.toString()
                    param.put("authToken",authToken)
                    return param
                }
            }


            val requestQueue = Volley.newRequestQueue(context!!)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(context!!, "Please Check internet connection.!").show()
        }
    }
}
