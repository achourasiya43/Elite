package com.elite.app

import android.app.Application
import android.text.TextUtils
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * Created by anil on 7/11/17.
 */
class EliteApp : Application(){


    val TAG = EliteApp::class.java!!.getSimpleName()

    private var mRequestQueue: RequestQueue? = null
    private var mInstance: EliteApp? = null


    fun getInstance(): EliteApp? {
        return mInstance
    }

    override fun onCreate() {
        super.onCreate()
    }


    fun <T> addToRequestQueue(req: Request<T>, tag: String) {
        // set the default tag if tag is empty
        req.tag = if (TextUtils.isEmpty(tag)) TAG else tag
        getRequestQueue().add(req)
    }

    fun getRequestQueue(): RequestQueue {
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(applicationContext)
        return mRequestQueue!!
    }
}