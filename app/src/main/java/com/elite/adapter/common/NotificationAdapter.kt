package com.elite.adapter.common

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elite.R
import com.elite.fragment.agency_side.AllRequestFragment
import com.elite.fragment.agency_side.JobDetailsFragment
import com.elite.fragment.doctor_side.LookingForFragment
import com.elite.helper.Constant
import com.elite.model.FinishedInfo
import com.elite.model.NotificationInfo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.notification_item_layout.view.*

/**
 * Created by anil on 5/12/17.
 */
class NotificationAdapter(var list: List<NotificationInfo.NotificationListBean>?, var activity: FragmentActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        holder as ViewHolder
        var listItem = list!!.get(position)
        holder.bind(listItem)


    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val v : View = LayoutInflater.from(parent?.context).inflate(R.layout.notification_item_layout,parent,false)
        return ViewHolder(v,activity)
    }

    override fun getItemCount(): Int {

        return list!!.size
    }

    class ViewHolder(itemView: View, var activity: FragmentActivity):RecyclerView.ViewHolder(itemView){

        fun bind(notificationItem: NotificationInfo.NotificationListBean) = with(itemView){
            itemView.name.text = notificationItem.name
            itemView.day_ago.text = notificationItem.day
            itemView.looking_for.text = notificationItem.notification_message?.body
            itemView.new_message.text = notificationItem!!.notification_message!!.title


            if(!notificationItem.profileImage.equals("")){

                Picasso.with(context).load(notificationItem.profileImage).placeholder(R.drawable.app_icon).into(itemView.profile_img)
            }

            itemView.setOnClickListener {
               var type =  notificationItem.notification_message?.type
               val reference_id =  notificationItem.notification_message?.reference_id.toString()

                if(type != null){
                    if(type.equals("post_create") || type.equals("post_update")){
                        addFragment(JobDetailsFragment.newInstance(reference_id, 0, 0),true,R.id.fragment_place)

                    }else if(type.equals("post_interest")){
                        var finishedInfo = FinishedInfo()
                        addFragment(LookingForFragment.newInstance(reference_id, "0", Constant.LookingForHome,finishedInfo),true,R.id.fragment_place)

                    }else if(type.equals("post_finish")){
                        addFragment(JobDetailsFragment.newInstance(reference_id, 0, 2),true,R.id.fragment_place)

                    }else if(type.equals("post_delete")){
                        addFragment(AllRequestFragment.newInstance("", ""),true,R.id.fragment_place)

                    }
                }
            }
        }

        fun addFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
            val backStackName = fragment.javaClass.name
            val fragmentManager = activity.supportFragmentManager
            val fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0)
            if (!fragmentPopped) {
                val transaction = fragmentManager.beginTransaction()
                transaction.add(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET)
                if (addToBackStack)
                    transaction.addToBackStack(backStackName)
                transaction.commit()
            }
        }

    }
}