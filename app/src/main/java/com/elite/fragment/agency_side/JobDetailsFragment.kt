package com.elite.fragment.agency_side

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.elite.R
import com.elite.WebService.WebServices
import com.elite.activity.ChatActivity
import com.elite.app_session.SessionManager
import com.elite.fcm_model.Chat
import com.elite.fragment.agency_side.JobDetailsFragment.ob.myclick
import com.elite.helper.Constant
import com.elite.model.doctor_side.DoctorDetailsInfo
import com.elite.util.Utils
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_job_details.view.*
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.HashMap

class JobDetailsFragment : Fragment() {

    private var post_id: String = ""
    private var position: Int? = null
    private var jobDetailsType : Int  = 0
    var drDetailsInfo: DoctorDetailsInfo? = null
    var session : SessionManager ?= null
    var chat :Chat = Chat()

    object ob{
        var myclick:Myclick ?= null
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: Int, jobDetailsType: Int): JobDetailsFragment {
            val fragment = JobDetailsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putInt(ARG_PARAM2, param2)
            args.putInt("jobDetailsType",jobDetailsType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            post_id = arguments!!.getString(ARG_PARAM1)
            position = arguments!!.getInt(ARG_PARAM2)
            jobDetailsType = arguments!!.getInt("jobDetailsType")
        }
        session = SessionManager(context!!)
    }

    interface Myclick{
        fun itemRemove(position:Int)
    }

    object a{
        fun setListner(myclickListner:Myclick){
            myclick = myclickListner
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view : View = inflater!!.inflate(R.layout.fragment_job_details, container, false)
        getDrDetails(view,post_id)



        view.tv_interested.setOnClickListener {
            interestedAgencyForFinish(view,post_id)

        }
        view.iv_back.setOnClickListener {
            activity?.onBackPressed()
        }

        view.tv_chat.setOnClickListener {
            if(chat != null){
                val intent = Intent(activity,ChatActivity::class.java)
                intent.putExtra("tranferChatInfo",chat)
                startActivity(intent)
            }
        }
        return view
    }

    private fun getDrDetails(view:View,post_id:String) {
        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.ShowDrDetails,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            val gson = Gson()
                            drDetailsInfo = gson.fromJson(response, DoctorDetailsInfo::class.java)
                            var status: String? = drDetailsInfo?.status
                            val message: String? = drDetailsInfo?.message
                            if (status == "success") {
                                for(drDetails in drDetailsInfo?.doctorDetails!!){

                                    chat = Chat()

                                    chat!!.uid = drDetails.user_id.toString()
                                    chat!!.firebaseId = drDetails.firebase_id.toString()
                                    chat!!.firebaseToken = drDetails.deviceToken.toString()
                                    chat!!.category_Id =  drDetails.category_id.toString()
                                    chat!!.name = drDetails.name.toString()
                                    chat!!.category_name = drDetails.job_title.toString()
                                    chat!!.profilePic = drDetails.profileImage.toString()


                                }


                                setComponentsUi(view,drDetailsInfo!!)

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
                        //Toasty.error(context!!,"Somthing went wrong").show()
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("post_id",post_id)

                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    val authToken = session?.user?.userDetail?.authToken
                    params.put("authToken",authToken.toString())
                    return params
                }
            }
            val requestQueue = Volley.newRequestQueue(context!!)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(context!!, "Please Check internet connection.!").show()
        }
    }

    fun setComponentsUi(view:View,drInfo: DoctorDetailsInfo){
        view.main_view.visibility = View.VISIBLE

        for(details in drInfo.doctorDetails!!){
            view.tv_user_name.text = details.name
            view.tv_user_email.text = details.email
            view.tv_user_phone.text =  details.countryCode+"-"+details.contactNumber
            view.job_category.text = details.job_title
            view.vacancy_number.text = details.vacancy_number+" "+"Vacancy"
            view.job_timing.text = details.job_timing
            view.job_location.text = details.location


            if(details.job_type.equals("full_time")){
                view.job_type.text = "Full time"
            }else if(details.job_type.equals("part_time")){
                view.job_type.text = "Part time"
            }else view.job_type.text = details.job_type

            view.qualification.text = details.qualification
            // view.need_from_date.text = details.need_from_date
            view.need_from_date.text = setDateFormat(details.need_from_date.toString())
            view.par_month_salary.text = details.salary
            view.description.text = details.description

            if(details.profileImage != null && !details.profileImage.equals("")) {
                if(context != null){
                    Picasso.with(context!!).load(details.profileImage).placeholder(R.drawable.app_icon).into(view.profile_img)

                }
            }

            if(jobDetailsType == Constant.JobDetailsHome){
                view.tv_interested.visibility = View.VISIBLE

            }
            else if(jobDetailsType == Constant.JobDetailsSetting){
                view.tv_interested.visibility = View.GONE
            }
            else if(details.is_interested.equals("0")){
                view.tv_interested.visibility = View.VISIBLE
            }
            else if(details.is_interested.equals("1")){
                view.tv_interested.visibility = View.GONE
            }

        }

    }

    fun setDateFormat(datee:String):String{
        var date:String = datee

        if(!date.contains("/")){
            var spf = SimpleDateFormat("dd-MM-yyyy")
            val newDate = spf.parse(date)
            spf = SimpleDateFormat("dd-MMM-yyyy")
            date = spf.format(newDate)
        }else{
            var  spf = SimpleDateFormat("dd/MM/yyyy")
            val newDate = spf.parse(date)
            spf = SimpleDateFormat("dd-MMM-yyyy")
            date = spf.format(newDate)
        }

        return date
    }

    private fun interestedAgencyForFinish(view:View,post_id:String) {

        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.InterestedAgencyForFinish,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            val gson = Gson()
                            drDetailsInfo = gson.fromJson(response, DoctorDetailsInfo::class.java)
                            var status: String? = drDetailsInfo?.status
                            val message: String? = drDetailsInfo?.message
                            if (status == "success") {
                                myclick?.itemRemove(position!!)
                                view.tv_interested.visibility = View.GONE
                                Toasty.success(context!!, message + "").show()
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

                    params.put("post_id",post_id)

                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    val authToken = session?.user?.userDetail?.authToken
                    params.put("authToken",authToken.toString())
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
