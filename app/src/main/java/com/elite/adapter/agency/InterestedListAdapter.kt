package com.elite.adapter.agency

import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elite.R
import com.elite.activity.ChatActivity
import com.elite.fcm_model.Chat
import com.elite.fragment.agency_side.JobDetailsFragment
import com.elite.helper.Constant
import com.elite.model.agency_side.InterestedInfo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.interested_item_layout.view.*
import java.util.ArrayList

/**
 * Created by anil on 4/12/17.
 */
class InterestedListAdapter (activity: FragmentActivity, doctorListInterestedByAgency: ArrayList<InterestedInfo.DoctorListInterestedByAgencyBean>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var activity = activity
    var list = doctorListInterestedByAgency


    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        holder as ViewHoder
        val listInfo: InterestedInfo.DoctorListInterestedByAgencyBean = list.get(position)
        holder.bind(position,listInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        var v: View = LayoutInflater.from(parent?.context).inflate(R.layout.interested_item_layout,parent,false)
        return ViewHoder(v, activity)
    }

    class ViewHoder (itemView1: View, activity: FragmentActivity):RecyclerView.ViewHolder(itemView1){
        var activity = activity
        fun bind(position: Int, listInfo: InterestedInfo.DoctorListInterestedByAgencyBean) = with(itemView){
            itemView.name.text = listInfo.name
            itemView.category.text = "Looking for "+listInfo.job_title
            itemView.contect_number.text = listInfo.countryCode.toString() +"-"+ listInfo.contactNumber.toString()
            itemView.vacancy_number.text = listInfo.vacancy_number+ " Person "

            if(!listInfo.profileImage.equals("") && listInfo.profileImage != null){
                Picasso.with(context).load(listInfo.profileImage).placeholder(R.drawable.app_icon).into(itemView.profile_img)
            }


            itemView.chat_button.setOnClickListener {
                var chat = Chat()

                chat!!.uid = listInfo.id.toString()
                chat!!.firebaseId = listInfo.firebase_id.toString()
                chat!!.firebaseToken = listInfo.deviceToken.toString()
                chat!!.category_Id = listInfo.category_id.toString()
                chat!!.name = listInfo.name.toString()
                chat!!.category_name = listInfo.job_title.toString()
                chat!!.profilePic = listInfo.profileImage.toString()

                var intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("tranferChatInfo",chat)
                context.startActivity(intent)


            }

            itemView.setOnClickListener {
                addFragment(JobDetailsFragment.newInstance(listInfo.post_id.toString(), 0,Constant.JobDetailsSetting),true,R.id.fragment_place)

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