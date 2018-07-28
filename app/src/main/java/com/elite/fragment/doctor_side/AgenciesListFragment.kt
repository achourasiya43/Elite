package com.elite.fragment.doctor_side

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
import com.elite.adapter.doctor.AgenciesListAdapter
import com.elite.app_session.SessionManager
import com.elite.model.agency_side.InteresedAgencyInfo
import com.elite.util.Utils
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_agencies_list.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class AgenciesListFragment : Fragment() {


    private var post_id: String = ""
    private var mParam2: String? = null
    private var agenciesListAdapter:AgenciesListAdapter ?= null
    private var session : SessionManager ?= null
    private var interestedAgencyList =  ArrayList<InteresedAgencyInfo.InterestedAgencyListBean>()

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): AgenciesListFragment {
            val fragment = AgenciesListFragment()
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
            post_id = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
        session = SessionManager(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_agencies_list, container, false)
        agenciesListAdapter = AgenciesListAdapter(interestedAgencyList)
        view.recycler_view.adapter = agenciesListAdapter

        interestedAgency(view,post_id)
        return view
    }

    private fun interestedAgency(view:View,post_id:String) {

        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.InterestedAgency,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            var jsonObject: JSONObject = JSONObject(response)
                            var status_:String = jsonObject.getString("status")
                            val message: String? = jsonObject.getString("message")

                            if(status_.equals("success")){
                                val gson = Gson()
                                var interestedAgencyInfo: InteresedAgencyInfo = gson.fromJson(response, InteresedAgencyInfo::class.java)
                                var status: String? = interestedAgencyInfo?.status
                                val message: String? = interestedAgencyInfo?.message
                                if (status == "success") {
                                    interestedAgencyList.addAll(interestedAgencyInfo.InterestedAgencyList)
                                    agenciesListAdapter!!.notifyDataSetChanged()

                                }
                            }else {
                                if(interestedAgencyList.size == 0){
                                    view.txt_no_agency_interested.visibility = View.VISIBLE
                                }else view.txt_no_agency_interested.visibility = View.GONE
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
