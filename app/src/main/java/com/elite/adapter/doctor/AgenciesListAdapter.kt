package com.elite.adapter.doctor

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elite.R
import com.elite.activity.ChatActivity
import com.elite.fcm_model.Chat
import com.elite.model.agency_side.InteresedAgencyInfo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.agencies_list_item.view.*

/**
 * Created by anil on 27/11/17.
 */
class AgenciesListAdapter(interestedAgencyList: ArrayList<InteresedAgencyInfo.InterestedAgencyListBean>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var interestedAgencyList = interestedAgencyList


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        var v = LayoutInflater.from(parent?.context).inflate(R.layout.agencies_list_item,parent,false)
        return ViewHolder(v)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.bind(position,interestedAgencyList)

    }

    override fun getItemCount(): Int {
      return interestedAgencyList.size
    }

    class ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){

        fun bind(position: Int, interestedAgencyList: ArrayList<InteresedAgencyInfo.InterestedAgencyListBean>) = with(itemView){

            var agencyInfo = interestedAgencyList.get(position)
            name.text = agencyInfo.name
            email.text = agencyInfo.email
            location.text = agencyInfo.location
            contect_number.text = agencyInfo.contactNumber
            day_ago.text = agencyInfo.day

            if(agencyInfo.profileImage != null && !agencyInfo.equals("")){
                Picasso.with(context).load(agencyInfo.profileImage).placeholder(R.drawable.app_icon).into(profile_img)
            }

           chat_button.setOnClickListener {
               var chat = Chat()

               chat!!.uid = agencyInfo.id.toString()
               chat!!.firebaseId = agencyInfo.firebase_id.toString()
               chat!!.firebaseToken = agencyInfo.deviceToken.toString()
               chat!!.category_Id = agencyInfo.category_id.toString()
               chat!!.name = agencyInfo.name.toString()
               chat!!.category_name = agencyInfo.job_title.toString()
               chat!!.profilePic = agencyInfo.profileImage.toString()

               var intent = Intent(context, ChatActivity::class.java)
               intent.putExtra("tranferChatInfo",chat)
               context.startActivity(intent)

            }
        }

    }

}