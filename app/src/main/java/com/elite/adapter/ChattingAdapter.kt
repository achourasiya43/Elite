package com.elite.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elite.R
import com.elite.app_session.SessionManager
import com.elite.fcm_model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.chat_left_side_view.view.*
import kotlinx.android.synthetic.main.chat_right_side_view.view.*

/**
 * Created by anil on 12/12/17.
 */

private val VIEW_TYPE_ME  = 1
private val VIEW_TYPE_OTHER = 2


class ChattingAdapter (mContext:Context,chatList : ArrayList<Chat>,var otherSideImage:String,var myImg:String) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    private var chatList = chatList
    var mContext = mContext
    var sessioin = SessionManager(mContext)
    var uid = sessioin.user?.userDetail?.id



    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        var v : View? =  null

        when(viewType){
            VIEW_TYPE_ME -> {
                 v = LayoutInflater.from(parent?.context).inflate(R.layout.chat_right_side_view,parent,false)
                return MyChatViewHolder(v!!,mContext)
            }
            VIEW_TYPE_OTHER ->{
                 v = LayoutInflater.from(parent?.context).inflate(R.layout.chat_left_side_view,parent,false)
                return OtherChatViewHolder(v!!,mContext)
            }
        }

        return OtherChatViewHolder(v!!,mContext)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        var chat = chatList.get(position)
        if (!TextUtils.equals(chatList.get(position).uid, uid)) {
            holder as MyChatViewHolder
            holder.bind(chat,myImg)
        } else {
            holder as OtherChatViewHolder
            holder.bind1(chat,otherSideImage)

        }


    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
         if (!TextUtils.equals(chatList.get(position).uid,uid )) {
             return VIEW_TYPE_ME
        } else {
             return VIEW_TYPE_OTHER
        }
    }

    class MyChatViewHolder(itemView: View, mContext: Context):RecyclerView.ViewHolder(itemView){
        var mContext = mContext
        fun bind(chat: Chat, myImg: String) = with(itemView){
            itemView.time_ago_.text = converteTimestamp(chat.timestamp)

            if (chat.message.startsWith("https://firebasestorage.googleapis.com/") || chat.message.startsWith("content://")) {
                Picasso.with(context).load(chat.message).into(itemView.chat_my_image_view)
                itemView.chat_my_image_view.visibility = View.VISIBLE
                itemView.my_message.visibility = View.GONE
            } else {
                itemView.my_message.visibility = View.VISIBLE
                itemView.my_message.text = chat.message
                itemView.chat_my_image_view.visibility = View.GONE
            }


            if(!myImg.equals("") && myImg != null){
                Picasso.with(mContext).load(myImg).into(itemView.mySideImage)
            }

        }
        fun converteTimestamp(mileSegundos: String): CharSequence {
            return DateUtils.getRelativeTimeSpanString(java.lang.Long.parseLong(mileSegundos), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
        }
    }

    class OtherChatViewHolder(itemView: View, mContext: Context):RecyclerView.ViewHolder(itemView){
        var mContext = mContext
        fun bind1(chat: Chat, otherSideImage: String) = with(itemView){

                itemView.time_ago.text =  converteTimestamp(chat.timestamp)


            if (chat.message.startsWith("https://firebasestorage.googleapis.com/") || chat.message.startsWith("content://")) {
                Picasso.with(context).load(chat.message).into(itemView.chat_other_image_view)
                itemView.chat_other_image_view.visibility = View.VISIBLE
                itemView.other_message.visibility = View.GONE
            } else {
                itemView.other_message.visibility = View.VISIBLE
                itemView.other_message.text = chat.message
                itemView.chat_other_image_view.visibility = View.GONE
            }

            if(!otherSideImage.equals("") && otherSideImage != null){
                Picasso.with(mContext).load(otherSideImage).into(itemView.profile_image_other_side)
            }
        }

        fun converteTimestamp(mileSegundos: String): CharSequence {
            return DateUtils.getRelativeTimeSpanString(java.lang.Long.parseLong(mileSegundos), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
        }
    }
}