package com.elite.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elite.R
import com.elite.activity.ChatActivity
import com.elite.app_session.SessionManager
import com.elite.fcm_model.Chat
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.chat_list_item.view.*
import java.util.*


/**
 * Created by anil on 12/12/17.
 */
class ChatListAdapter (var mContext: Context, histortList: ArrayList<Chat>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var histortList = histortList




    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        holder as ViewHolder
        var chat  = histortList.get(position)
        holder.bind(position,chat)
    }

    override fun getItemCount(): Int {
        return histortList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.chat_list_item,parent,false)
        return ViewHolder(v,mContext)
    }

    class ViewHolder(itemView: View, mContext: Context): RecyclerView.ViewHolder(itemView){
        var session  = SessionManager(mContext)

        fun bind(position: Int, chat: Chat) = with(itemView){


            if(chat.message.startsWith("https://firebasestorage.googleapis.com/") ){
                itemView.last_meassage.text = "Image uploaded"
            }else{
                itemView.last_meassage.text = chat.message
            }
            itemView.date_time.text = converteTimestamp(chat.timestamp)
            itemView.looking_for.text = "Looking for " + chat.category_name
            itemView.opponent_name.text = chat.name

            if(!chat.profilePic.equals("")) Picasso.with(context).load(chat.profilePic).into(itemView.image_opponents)



            itemView.setOnClickListener {

                var intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("tranferChatInfo",chat)
                print(""+chat)
                intent.putExtra("title",chat.name)
                context.startActivity(intent)
            }
        }

        fun converteTimestamp(mileSegundos: String): CharSequence {
            return DateUtils.getRelativeTimeSpanString(java.lang.Long.parseLong(mileSegundos), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
        }
    }


}