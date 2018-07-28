package com.elite.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elite.R
import com.elite.app_session.SessionManager
import kotlinx.android.synthetic.main.fragment_logout.view.*

class LogoutFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null

    companion object {

        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): LogoutFragment {
            val fragment = LogoutFragment()
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
        var view : View = inflater!!.inflate(R.layout.fragment_logout, container, false)

        var session = SessionManager(context!!)
        view.logout.setOnClickListener( {
            session.logout()
        })

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

    }


}
