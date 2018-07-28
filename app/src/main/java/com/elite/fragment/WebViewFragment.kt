package com.elite.fragment

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient

import com.elite.R
import kotlinx.android.synthetic.main.fragment_web_view.view.*


class WebViewFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    val mywebviewURL = "http://drive.google.com/viewerng/viewer?embedded=true&url="+"http://gnmtechnology.com/elite/uploads/content/termsfeed-terms-conditions.pdf"

    companion object {

        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): WebViewFragment {
            val fragment = WebViewFragment()
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

        val view = inflater.inflate(R.layout.fragment_web_view, container, false)
       // view?.spin_kit?.visibility = View.VISIBLE

        view!!.webview.getSettings().setJavaScriptEnabled(true)
        view!!.webview.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

            }

        })

        view!!.webview.loadUrl(mywebviewURL)

        view.iv_back.setOnClickListener {
            activity?.onBackPressed()
        }
        return view
    }


    override fun onStart() {
        super.onStart()

    }

}
