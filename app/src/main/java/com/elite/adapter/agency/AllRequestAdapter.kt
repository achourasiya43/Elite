package com.elite.adapter.agency

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elite.R
import com.elite.fragment.agency_side.JobDetailsFragment
import com.elite.helper.Constant
import com.elite.model.agency_side.AllRequestInfo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.all_requested_item.view.*

/**
 * Created by anil on 25/11/17.
 */

class AllRequestAdapter (reqlist: ArrayList<AllRequestInfo.DoctorRequestJobListBean>?, activity: FragmentActivity, context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var activity = activity
    var context = context
    var reqList = reqlist

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
       var v:View = LayoutInflater.from(parent?.context).inflate(R.layout.all_requested_item,parent,false)
        return ViewHoder(v,activity)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val viewHolder= holder as ViewHoder
        viewHolder.bind(reqList,position)

    }

    override fun getItemCount(): Int {
        return reqList!!.size
    }

    fun deleteItem(position:Int){
        reqList?.removeAt(position)
        notifyDataSetChanged()
    }

    class ViewHoder (itemView: View,activity: FragmentActivity):RecyclerView.ViewHolder(itemView){

        var activity = activity

        fun bind(reqList: ArrayList<AllRequestInfo.DoctorRequestJobListBean>?, position: Int) = with (itemView){
            name.text = reqList?.get(position)?.doctorName
            contactNumber.text = reqList?.get(position)?.countryCode +"-"+ reqList?.get(position)?.contactNumber
            looking_for.text = reqList?.get(position)?.job_title
            vacancy_number.text = reqList?.get(position)?.vacancy_number+" "+"person"
            day_ago.text = reqList?.get(adapterPosition)?.day
            val path = reqList?.get(position)?.thumbImage
            if( path != null && !path.equals("")){
                Picasso.with(context).load(path).resize(100,100).placeholder(R.drawable.app_icon).into(iv_doctor)
            }else{

            }

            setOnClickListener {
                addFragment(JobDetailsFragment.newInstance(reqList?.get(position)?.post_id.toString(), adapterPosition, Constant.JobDetailsHome),true,R.id.fragment_place)
            }

        }

        fun addFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
            val backStackName = fragment.javaClass.name
            val fragmentManager = activity.supportFragmentManager
            val fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0)
            if (!fragmentPopped) {
                val transaction = fragmentManager.beginTransaction()
                transaction.add(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                if (addToBackStack)
                    transaction.addToBackStack(backStackName)
                transaction.commit()
            }
        }

    }



}