package com.elite.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
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
import com.elite.adapter.common.NotificationAdapter
import com.elite.app_session.SessionManager
import com.elite.endlessRecyclerView.EndlessRecyclerViewScrollListener
import com.elite.model.NotificationInfo
import com.elite.util.Utils
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_notification.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class NotificationFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    var notificationInfo =  NotificationInfo()
    var NotificationList  = ArrayList<NotificationInfo.NotificationListBean>()
    private var linearLayoutManager: LinearLayoutManager?= null
    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    private var start:Int = 0
    private var limit:Int= 20

    var notifiactionAdapter  : NotificationAdapter ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_notification, container, false)

        notificationList(view!!)
      /*  linearLayoutManager = LinearLayoutManager(context)
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager!!) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                loadNextDataFromApi(totalItemsCount)
            }
        }

        view.recycler_view.addOnScrollListener(scrollListener)*/
        linearLayoutManager = LinearLayoutManager(context)
        view.recycler_view.layoutManager = linearLayoutManager
        notifiactionAdapter = NotificationAdapter(NotificationList,activity!!)
        view.recycler_view.adapter = notifiactionAdapter

        return view
    }

    private fun loadNextDataFromApi(totalItemsCount: Int) {
        start = start+20
        notificationList(view!!)

    }

    companion object {

        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"


        fun newInstance(param1: String, param2: String): NotificationFragment {
            val fragment = NotificationFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    private fun notificationList(view: View) {
        view.no_data_found.visibility = View.GONE
        var session  = SessionManager(context!!)

        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.GET, WebServices.NotificationList,
                    Response.Listener { response ->

                        view.spin_kit.visibility = View.GONE
                        var obj = JSONObject(response)
                        var status:String = obj.getString("status").toString()
                        var message:String = obj.get("message").toString()
                        if(status.equals("success")){
                            try {
                                var gson = Gson()
                                notificationInfo = gson.fromJson(response, NotificationInfo::class.java)
                                NotificationList.addAll(notificationInfo.notificationList!!)
                                notifiactionAdapter?.notifyDataSetChanged()

                                //Toasty.success(this,message).show()
                            } catch (e: JSONException) {
                                Toasty.error(context!!,"Something went wrong").show()
                                view.spin_kit.visibility = View.GONE
                                e.printStackTrace()
                            }
                        }else{
                            if(NotificationList.size == 0){
                                view.no_data_found.visibility = View.VISIBLE
                            }
                        }

                    },
                    Response.ErrorListener {
                        if(context != null){
                            Toasty.error(context!!, "Something went wrong").show()
                            view.spin_kit.visibility = View.GONE
                        }
                        Utils.sessionExDialog(context!!)

                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    var authToken = session?.user?.userDetail?.authToken.toString()
                    params.put("authToken", authToken)
                    return params
                }
            }


            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(context!!, "Please Check internet connection.!").show()
        }
    }
}
