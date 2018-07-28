package com.elite.fragment.agency_side

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.elite.R
import com.elite.WebService.WebServices
import com.elite.adapter.agency.FinishedListAdapter
import com.elite.adapter.agency.InterestedListAdapter
import com.elite.adapter.doctor.DoctorFinishedListAdapter
import com.elite.app_session.SessionManager
import com.elite.endlessRecyclerView.EndlessRecyclerViewScrollListener
import com.elite.model.agency_side.InterestedInfo
import com.elite.model.doctor_side.DoctorFinishedInfo
import com.elite.model.agency_side.FinishedJobInfo
import com.elite.util.Utils
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_finished_and__interested_job_.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap


class Finished_and_Interested_job_Fragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null

    var interestedAdapter: InterestedListAdapter ?=  null
    var finishedAdapter: FinishedListAdapter ? =null
    var DoctorfinishedAdapter: DoctorFinishedListAdapter? =null

    var interestedInfo  = InterestedInfo()
    var finishedJobInfo = FinishedJobInfo()
    var doctorFinishedJobInfo = DoctorFinishedInfo()

    var interestedList = ArrayList<InterestedInfo.DoctorListInterestedByAgencyBean>()
    var finishedJobList = ArrayList<FinishedJobInfo.FinishedJobListAgencySideBean>()
    var doctorFinishedList = ArrayList<DoctorFinishedInfo.DoctorFinishedJobListBean>()

    var session : SessionManager ?= null
    var interested_tab = 1
    var finished_tab_agency = 2
    var finished_tab_doctor = 3
    var tab_type = 0



    private var linearLayoutManager: LinearLayoutManager ?= null
    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    private var start:Int = 0
    private var limit:Int= 20

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): Finished_and_Interested_job_Fragment {
            val fragment = Finished_and_Interested_job_Fragment()
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
        session = SessionManager(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_finished_and__interested_job_, container, false)


        val userType = session?.user?.userDetail?.userType

        if(userType.equals("1")){
            view.interested_finishe_ly.visibility = View.GONE
            view.txt_header.text = resources.getString(R.string.finished_dr_side)
            DoctorSideFinishedList(view)
            tab_type = 3

        }
        else if(userType.equals("2")) {
            InterestedList(view)
            view.interested_finishe_ly.visibility = View.VISIBLE
            view.txt_header.text = resources.getString(R.string.finished)
            tab_type = 1
        }
        else
        {
            view.interested_finishe_ly.visibility = View.GONE
        }
        view.tv_interested_tab.isEnabled = false

        view.iv_back.setOnClickListener {
            activity?.onBackPressed()
        }

        linearLayoutManager = LinearLayoutManager(context)
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager!!) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                if(tab_type == interested_tab){
                    loadNextDataFromApi_interested(totalItemsCount)

                }else if(tab_type == finished_tab_agency){
                    loadNextDataFromApi_AgncySideFinished(totalItemsCount)

                }else if(tab_type == finished_tab_doctor){
                    loadNextDataFromApi_finishedDr(totalItemsCount)
                }

            }
        }
        view.recycler_view.layoutManager = linearLayoutManager
        view.recycler_view.addOnScrollListener(scrollListener)


        view.tv_interested_tab.setOnClickListener {
            view.tv_interested_tab.setTextColor(ContextCompat.getColor(context!!,R.color.white))
            view.tv_interested_tab.setBackgroundResource(R.drawable.extra_left_side_rounded_primary_border)
            view.tv_finished_tab.setTextColor(ContextCompat.getColor(context!!,R.color.light_gray))
            view.tv_finished_tab.setBackgroundResource(R.drawable.extra_right_side_rounded_gray_border)

            tab_type = 1
            start = 0
            InterestedList(view)
            scrollListener?.resetState()
            finishedJobList.clear()
            finishedAdapter?.notifyDataSetChanged()

            view.tv_interested_tab.isEnabled = false


        }

        view.tv_finished_tab.setOnClickListener {
            view.tv_finished_tab.setTextColor(ContextCompat.getColor(context!!,R.color.white))
            view.tv_finished_tab.setBackgroundResource(R.drawable.extra_right_side_rounded_primary_border)
            view.tv_interested_tab.setTextColor(ContextCompat.getColor(context!!,R.color.light_gray))
            view.tv_interested_tab.setBackgroundResource(R.drawable.extra_left_side_rounded_gray_border)

            tab_type = 2
            start = 0
            AgncySideFinishedList(view)
            scrollListener?.resetState()
            interestedList.clear()
            interestedAdapter?.notifyDataSetChanged()


            view.tv_finished_tab.isEnabled = false

        }

        return view
    }

    private fun loadNextDataFromApi_interested(totalItemsCount: Int) {
        start = start+20
        InterestedList(view!!)
    }

    private fun loadNextDataFromApi_AgncySideFinished(totalItemsCount: Int) {
        start = start+20
        AgncySideFinishedList(view!!)

    }
    private fun loadNextDataFromApi_finishedDr(totalItemsCount: Int) {
        start = start+20
        DoctorSideFinishedList(view!!)
    }


    private fun InterestedList(view: View) {
        var session = SessionManager(context!!)
        view.no_data_found.visibility = View.GONE
        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.AgencySideInterested,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            var jsonObject: JSONObject = JSONObject(response)
                            var status_:String = jsonObject.getString("status")
                            val message: String? = jsonObject.getString("message")
                            view.tv_finished_tab.isEnabled = true

                            if(status_.equals("success")){
                                var gson = Gson()
                                interestedInfo = gson.fromJson(response, InterestedInfo::class.java)
                                if(activity != null){
                                    interestedList.addAll( interestedInfo.doctorListInterestedByAgency)
                                    interestedAdapter = InterestedListAdapter(activity!!, interestedList)
                                    view.recycler_view.adapter = interestedAdapter
                                    if(interestedInfo.doctorListInterestedByAgency.size == 0){
                                        view.no_data_found.visibility = View.VISIBLE
                                    }
                                    view.no_data_found.visibility = View.GONE
                                }

                            }
                            else {
                                if(activity!=null){
                                    interestedAdapter = InterestedListAdapter(activity!!,interestedList)
                                    view.recycler_view.adapter = interestedAdapter
                                    if(interestedList.size < 0){
                                        Toasty.error(context!!, message + "").show()
                                    }
                                    if(interestedList.size == 0){
                                        view.no_data_found.visibility = View.VISIBLE
                                    }
                                }
                            }
                        } catch (e: JSONException) {
                            view.spin_kit.visibility = View.GONE
                            Toasty.error(context!!, "Something went wrong" + "").show()
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        view.spin_kit.visibility = View.GONE

                        Utils.sessionExDialog(context!!)
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    val agency_id:String = session?.user?.userDetail?.id.toString()
                    params.put("agency_id",agency_id)
                    params.put("start",start.toString())
                    params.put("limit",limit.toString())
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

    private fun AgncySideFinishedList(view: View) {
        var session = SessionManager(context!!)
        view.no_data_found.visibility = View.GONE
        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.AgencySideFinished,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            var jsonObject = JSONObject(response)
                            var status_:String = jsonObject.getString("status")
                            val message: String? = jsonObject.getString("message")
                            view.tv_interested_tab.isEnabled = true
                            if(status_.equals("success")){
                                var gson = Gson()
                                finishedJobInfo = gson.fromJson(response, FinishedJobInfo::class.java)
                                finishedJobList.addAll(finishedJobInfo.finishedJobListAgencySide)
                                finishedAdapter = FinishedListAdapter(activity!!, finishedJobList)
                                view.recycler_view.adapter = finishedAdapter
                                view.no_data_found.visibility = View.GONE

                            }
                            else {
                                finishedAdapter = FinishedListAdapter(activity!!,finishedJobList)
                                view.recycler_view.adapter = finishedAdapter
                                if(interestedList.size < 0){
                                    Toasty.error(context!!, message + "").show()
                                }
                                if(finishedJobList.size == 0){
                                    view.no_data_found.visibility = View.VISIBLE
                                }

                            }
                        } catch (e: JSONException) {
                            view.spin_kit.visibility = View.GONE
                            Toasty.error(context!!, "Something went wrong" + "").show()
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        view.spin_kit.visibility = View.GONE
                        Utils.sessionExDialog(context!!)
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    val agency_id:String = session?.user?.userDetail?.id.toString()
                    params.put("agency_id",agency_id)
                    params.put("start",start.toString())
                    params.put("limit",limit.toString())
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

    private fun DoctorSideFinishedList(view: View) {
        var session = SessionManager(context!!)
        view.no_data_found.visibility = View.GONE
        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.DoctorSideFinished,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            var jsonObject = JSONObject(response)
                            var status_:String = jsonObject.getString("status")
                            val message: String? = jsonObject.getString("message")

                            if(status_.equals("success")){
                                var gson = Gson()
                                doctorFinishedJobInfo = gson.fromJson(response, DoctorFinishedInfo::class.java)
                                if(activity != null){
                                    doctorFinishedList.addAll( doctorFinishedJobInfo.doctorFinishedJobList)
                                    DoctorfinishedAdapter = DoctorFinishedListAdapter(activity!!,doctorFinishedList)
                                    view.recycler_view.adapter = DoctorfinishedAdapter
                                }
                                view.no_data_found.visibility = View.GONE
                            }
                            else {
                                DoctorfinishedAdapter = DoctorFinishedListAdapter(activity!!, doctorFinishedList)
                                view.recycler_view.adapter = DoctorfinishedAdapter
                                if(doctorFinishedList.size < 0){
                                    Toasty.error(context!!, message + "").show()
                                }
                                if(doctorFinishedList.size == 0){
                                    view.no_data_found.visibility = View.VISIBLE
                                }

                            }
                        } catch (e: JSONException) {
                            view.spin_kit.visibility = View.GONE
                            Toasty.error(context!!, "Something went wrong" + "").show()
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        view.spin_kit.visibility = View.GONE
                        Utils.sessionExDialog(context!!)
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    val doctor_id:String = session?.user?.userDetail?.id.toString()
                    params.put("doctor_id",doctor_id)
                    params.put("start",start.toString())
                    params.put("limit",limit.toString())

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
