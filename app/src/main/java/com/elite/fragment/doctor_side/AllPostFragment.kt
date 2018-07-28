package com.elite.fragment.doctor_side

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.elite.R
import com.elite.WebService.WebServices
import com.elite.adapter.doctor.AllPostListAdapter
import com.elite.app_session.SessionManager
import com.elite.endlessRecyclerView.EndlessRecyclerViewScrollListener
import com.elite.helper.Constant
import com.elite.model.FinishedInfo
import com.elite.model.doctor_side.DrListInfo
import com.elite.server_task.ConnectingService
import com.elite.util.Utils
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_all_post.view.*
import org.json.JSONException
import java.util.HashMap


class AllPostFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    var allpostListAdapter : AllPostListAdapter? = null
    var drList:ArrayList<DrListInfo.AllDoctorVacancyListBean> = ArrayList()
    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    var linearLayoutManager: LinearLayoutManager ?= null
    private var start:Int = 0
    private var limit:Int= 20


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view : View  = inflater.inflate(R.layout.fragment_all_post, container, false)
        getAllPost(view)

       var finishedInfo = FinishedInfo()

        view.iv_add_post.setOnClickListener {
            addFragment(LookingForFragment.newInstance("", "", Constant.LookingForHome, finishedInfo),true,R.id.fragment_place)
        }

        linearLayoutManager = LinearLayoutManager(context)
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager!!) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                loadNextDataFromApi(totalItemsCount)
            }
        }
        view.recycler_view.setLayoutManager(linearLayoutManager)
        view.recycler_view.addOnScrollListener(scrollListener)
        allpostListAdapter = AllPostListAdapter(drList, context!! as FragmentActivity)
        view.recycler_view.adapter = allpostListAdapter

        return view
    }

    private fun loadNextDataFromApi(totalItemsCount: Int) {
        start = start+20
        getAllPost(view!!)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }


    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): AllPostFragment {
            val fragment = AllPostFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    fun addFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentManager = activity!!.supportFragmentManager
        val fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0)
        if (!fragmentPopped) {
            val transaction = fragmentManager.beginTransaction()
            transaction.add(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }
    }

    private fun getAllPost(view:View) {
        val session = SessionManager(context!!)

        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.AllDrList,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            val gson = Gson()

                            var drListInfo: DrListInfo = gson.fromJson(response, DrListInfo::class.java)

                            val status: String? = drListInfo.status
                            val message: String? = drListInfo.message
                            if (status == "success") {
                                drList.addAll(drListInfo.allDoctorVacancyList)
                                allpostListAdapter?.notifyDataSetChanged()


                            } else {
                                if(drList.size == 0){
                                    view.txt_no_post_found.visibility  = View.VISIBLE
                                }else view.txt_no_post_found.visibility  = View.GONE
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
                    var drId:String =  session.user?.userDetail?.id.toString()
                    params.put("doctor_id", drId)
                    params.put("start",start.toString())
                    params.put("limit",limit.toString())
                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    var param = HashMap<String,String>()
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


    fun task(){
        val param = HashMap<String, String>()
        var connection = ConnectingService(context!!,object : ConnectingService.ResponceListner{
            override fun onResponse(responce: String, url: String) {
                Log.d("error", responce + "")
            }
            override fun ErrorListener(error: VolleyError) {
                Log.d("error", error.message )

            }
        }).callApi(WebServices.Category_url,Request.Method.GET,param)

    }
}
