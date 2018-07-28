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
import com.elite.fragment.doctor_side.LookingForFragment
import com.elite.helper.Constant
import com.elite.model.FinishedInfo
import com.elite.model.agency_side.FinishedJobInfo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.finished_list_item_layout.view.*

/**
 * Created by anil on 5/12/17.
 */
class FinishedListAdapter(activity:FragmentActivity, list: ArrayList<FinishedJobInfo.FinishedJobListAgencySideBean>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var activity = activity
    var list = list

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val v:View = LayoutInflater.from(parent?.context).inflate(R.layout.finished_list_item_layout,parent,false)
        return ViewHolder(v,activity)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        holder as ViewHolder
        var listinfo = list.get(position)
        holder.bind(position,listinfo)
    }


    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View, activity: FragmentActivity) : RecyclerView.ViewHolder(itemView){

        var activity= activity

        fun bind(position: Int, listinfo: FinishedJobInfo.FinishedJobListAgencySideBean) = with(itemView){
            itemView.txt_finish_by.text = "Finished by doctor"
            itemView.location.text = listinfo.location
            itemView.salary.text = listinfo.salary
            itemView.date.text = listinfo.need_from_date
            itemView.name.text = listinfo.name
            if(listinfo.countryCode == null  || listinfo.contactNumber == null){
                itemView.phone.text = "none"
            }else{
                itemView.phone.text = listinfo.countryCode +"-"+listinfo.contactNumber
            }
            itemView.category.text = "Looking for "+listinfo.job_title
           // itemView.category.text = "Not given by server side"


            if(!listinfo.profileImage.equals("") && listinfo.profileImage != null){
                Picasso.with(context).load(listinfo.profileImage).placeholder(R.drawable.app_icon).into(itemView.profile_img)
            }

            var finishedInfo = FinishedInfo()
            finishedInfo.name = listinfo.name
            finishedInfo.contactNumber = listinfo.contactNumber
            finishedInfo.profileImage = listinfo.profileImage

            itemView.setOnClickListener {
                addFragment(LookingForFragment.newInstance(listinfo.id.toString(), "",Constant.LookingFoSetting,finishedInfo),true,R.id.fragment_place)

            }

            itemView.iv_chat.setOnClickListener {
                var chat = Chat()

                chat!!.uid = listinfo.id.toString()
                chat!!.firebaseId = listinfo.firebase_id.toString()
                chat!!.firebaseToken = listinfo.deviceToken.toString()
                chat!!.category_Id = listinfo.category_id.toString()
                chat!!.name = listinfo.name.toString()
                chat!!.category_name = listinfo.job_title.toString()
                chat!!.profilePic = listinfo.profileImage.toString()

                var intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("tranferChatInfo",chat)
                context.startActivity(intent)
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