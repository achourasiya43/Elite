package com.elite.fragment.agency_side

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import com.elite.adapter.agency.AllRequestAdapter
import com.elite.app_session.SessionManager
import com.elite.endlessRecyclerView.EndlessRecyclerViewScrollListener
import com.elite.fragment.agency_side.JobDetailsFragment.a.setListner
import com.elite.model.agency_side.AllRequestInfo
import com.elite.util.Utils
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_all_request.view.*
import org.json.JSONException
import java.util.HashMap


class AllRequestFragment : Fragment(), JobDetailsFragment.Myclick{



    private var mParam1: String? = null
    private var mParam2: String? = null
    private var requestList = ArrayList<AllRequestInfo.DoctorRequestJobListBean>()
    private var allreuestadapter :AllRequestAdapter ?= null
    var session:SessionManager ?= null
    var allrequestInfo: AllRequestInfo? = null
    var jobDetailsFragment:JobDetailsFragment = JobDetailsFragment()
    private var linearLayoutManager: LinearLayoutManager ?= null
    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    private var start:Int = 0
    private var limit:Int= 20


    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"


        fun newInstance(param1: String, param2: String): AllRequestFragment {
            val fragment = AllRequestFragment()
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

        val view :View  = inflater!!.inflate(R.layout.fragment_all_request, container, false)
        getAllRequest(view)
        setListner(this)


        linearLayoutManager = LinearLayoutManager(context)
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager!!) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                loadNextDataFromApi(totalItemsCount)
            }
        }
        view.recycler_view.layoutManager = linearLayoutManager
        view.recycler_view.addOnScrollListener(scrollListener)
        allreuestadapter = AllRequestAdapter(requestList, context!! as FragmentActivity,context!!)
        view.recycler_view.adapter = allreuestadapter

        return view
    }

    private fun loadNextDataFromApi(totalItemsCount: Int) {
        start = start+20
        getAllRequest(view!!)


    }

    override fun itemRemove(position: Int) {
        //allrequestInfo!!.doctorRequestJobList!!.removeAt(position)
      //  allreuestadapter!!.notifyDataSetChanged()
        allreuestadapter!!.deleteItem(position)
    }


    private fun getAllRequest(view:View) {

        if (Utils.isConnectingToInternet(context!!)) {
            view.no_data_found.visibility = View.GONE
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.AllRequest,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            val gson = Gson()

                             allrequestInfo = gson.fromJson(response, AllRequestInfo::class.java)

                            val status: String? = allrequestInfo?.status
                            val message: String? = allrequestInfo?.message
                            if (status == "success") {

                                requestList.addAll(allrequestInfo?.doctorRequestJobList!!)
                                allreuestadapter?.notifyDataSetChanged()

                            } else {
                                if(requestList.size < 0){
                                    Toasty.error(context!!, message + "").show()
                                }
                                if(requestList.size == 0){
                                    view.no_data_found.visibility = View.VISIBLE
                                }

                                //Toasty.error(context!!, message + "").show()
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
                    val id =  session?.user?.userDetail?.id.toString()
                    params.put("userId",id)
                    params.put("start",start.toString())
                    params.put("limit",limit.toString())

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
