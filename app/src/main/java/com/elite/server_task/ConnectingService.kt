package com.elite.server_task

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.elite.WebService.WebServices
import com.elite.app.EliteApp
import java.util.HashMap

/**
 * Created by anil on 24/11/17.
 */
class ConnectingService (mContext:Context,listener: ResponceListner){
    var elite:EliteApp = EliteApp()
    val mContext = mContext
    private var mListener: ResponceListner = listener


    fun callApi(url: String, Method: Int, params: Map<String, String>) {
        callApi(url, Method, params, false)

    }

    fun callApi(url: String, Method: Int, params: Map<String, String>?, isSelfErrorHandle: Boolean) {

        val stringRequest = object : StringRequest(Method, url,
                Response.Listener { response ->
                    println("#" + response)
                    mListener?.onResponse(response, url)
                },
                Response.ErrorListener { error ->
                    if (isSelfErrorHandle)
                        handleError(error)
                    else
                        mListener?.ErrorListener(error)
                }) {

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                return params ?: super.getParams()
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                /* if(session.isLoggedIn()){
                       header.put("authToken", session.getAuthToken());
                   }*/
                return HashMap()
            }
        }

        elite.getInstance()?.addToRequestQueue(stringRequest, elite.TAG)
    }

    private fun handleError(error: VolleyError) {
        handleError(mContext, error)
    }

    private fun handleError(context: Context, error: VolleyError?) {
        if (error != null && error.networkResponse != null) {

            try {
                //if(error.networkResponse.statusCode==400){
                val dialog = Dialog(context)
                showSessionError("session expired", ServerResponseCode.getmeesageCode(error.networkResponse.statusCode), "Ok")
                /* }else {
                    MySnackBar.show(WebServiceAPI.getNetworkMessage(error));
                }*/
            } catch (ex: NullPointerException) {
                ex.printStackTrace()
            }

        } else
            Toast.makeText(context, "Something went wrong. please try again later.", Toast.LENGTH_SHORT).show()
    }

    fun showSessionError(title: String, msg: String, button: String) {
        val builder = android.support.v7.app.AlertDialog.Builder(mContext)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(button) { dialog, id ->
            dialog.dismiss()
            //Mualab.getInstance().getSessionManager().logout();
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }


    interface ResponceListner {
        fun onResponse(responce: String, url: String)
        fun ErrorListener(error: VolleyError)
    }

    fun setListner(listner: ResponceListner) {
        mListener = listner
    }
}