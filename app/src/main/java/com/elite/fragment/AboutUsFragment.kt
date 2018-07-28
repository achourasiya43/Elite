package com.elite.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
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
import com.elite.app_session.SessionManager
import com.elite.util.Utils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_about_us.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap


class AboutUsFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null

    companion object {

        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): AboutUsFragment {
            val fragment = AboutUsFragment()
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
        var view = inflater.inflate(R.layout.fragment_about_us, container, false)
        aboutUsText(view)
        view.iv_back.setOnClickListener {
            activity?.onBackPressed()
        }
        return view
    }

    private fun aboutUsText( view: View) {

        if (Utils.isConnectingToInternet(context!!)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.AboutUs,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        var obj = JSONObject(response)
                        var status:String = obj.getString("status").toString()
                        var message:String = obj.getString("message").toString()
                        if(status.equals("success")){
                            try {

                                var content = obj.getString("content")
                                view.text_aboutUs.text = content

                            } catch (e: JSONException) {
                                view.spin_kit.visibility = View.GONE
                                Toasty.error(context!!,"Something went wrong").show()
                                e.printStackTrace()
                            }
                        }
                    },
                    Response.ErrorListener {
                        view.spin_kit.visibility = View.GONE
                        Utils.sessionExDialog(context!!)
                        // Toasty.error(context!!, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("page_type", "about_us")
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
