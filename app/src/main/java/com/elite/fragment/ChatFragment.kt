package com.elite.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elite.R
import com.elite.adapter.ChatListAdapter
import com.elite.app_session.SessionManager
import com.elite.fcm_model.Chat
import com.elite.fcm_model.UserInfoFCM
import com.elite.helper.Constant
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ChatFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    private var chatListAdapter: ChatListAdapter ?= null
    private var histortList = ArrayList<Chat>()
    var userList = ArrayList<UserInfoFCM>()
    var index = 0
    var session: SessionManager ?= null
    private var map = HashMap<String,Chat>();

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putSerializable(ARG_PARAM1, param1)
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
        val view = inflater!!.inflate(R.layout.fragment_chat, container, false)
        session = SessionManager(context!!)

        chatListAdapter = ChatListAdapter(context!!,histortList)
        view.recycler_view.adapter = chatListAdapter
        //gettingDataFromUserTable(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getChatHistory(view)
    }


    fun  getChatHistory(view: View) {
        //view.no_data_found.visibility = View.GONE
        var myUid = session?.user?.userDetail?.id
        var listKeys = ArrayList<String>()
        view.spin_kit.visibility = View.VISIBLE

        if(histortList.size == 0){
            view.spin_kit.visibility = View.GONE
            view.no_data_found.visibility = View.VISIBLE
        }else {
            view.spin_kit.visibility = View.VISIBLE
            view.no_data_found.visibility = View.GONE
        }


        FirebaseDatabase.getInstance().reference.child(Constant.ARG_HISTORY).child(myUid). limitToLast(20).addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                view.spin_kit.visibility = View.GONE
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
                var key = dataSnapshot?.key

                FirebaseDatabase.getInstance().reference.child(Constant.ARG_HISTORY).child(myUid).child(key). limitToLast(20).addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                        view.spin_kit.visibility = View.GONE
                    }

                    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                    }

                    override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                        view.spin_kit.visibility = View.GONE
                        view.no_data_found.visibility = View.GONE
                        var chat  = p0?.getValue(Chat::class.java)!!

                        var key = dataSnapshot?.key
                        index = listKeys.indexOf(key+"_"+chat.category_Id)
                        gettingDataFromUserTable(view,key!!,chat)

                        //refresh(view, key!!,chat,index)
                    }

                    override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {

                        view.spin_kit.visibility = View.GONE
                        view.no_data_found.visibility = View.GONE

                        var chat:Chat  = dataSnapshot?.getValue(Chat::class.java)!!


                        listKeys.add(chat.uid+"_"+chat.category_Id)

                        gettingDataFromUserTable(view, key!!,chat)


                    }

                    override fun onChildRemoved(p0: DataSnapshot?) {
                    }

                })

            }

            override fun onChildRemoved(p0: DataSnapshot?) {
            }

        })


    }

    private fun gettingDataFromUserTable(view: View, id: String, chat: Chat) {

        view.spin_kit.visibility = View.VISIBLE

        FirebaseDatabase.getInstance().reference.child(Constant.ARG_USERS).child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                view.spin_kit.visibility = View.GONE
                var user = p0?.getValue(UserInfoFCM::class.java)
                userList.add(user!!)

                for (userValue in userList){
                    if(userValue.uid.equals(chat.uid)){
                        chat.profilePic = userValue.profilePic
                        chat.name = userValue.name
                        chat.firebaseToken = userValue.firebaseToken
                    }
                }

                map.put(chat.uid+"_"+chat.category_Id,chat)

                var demoValues : Collection<Chat> = map.values
                histortList = ArrayList<Chat>(demoValues)

                if(chatListAdapter != null && context != null){
                    chatListAdapter = ChatListAdapter(context!!,histortList)
                    view.recycler_view.adapter = chatListAdapter
                    shortList()

                }
            }
        })
    }

    private fun refresh(view: View, id: String, chat: Chat, index: Int) {
        view.spin_kit.visibility = View.GONE
        for (userValue in userList){
            if(userValue.uid.equals(chat.uid)){
                chat.profilePic = userValue.profilePic
                chat.name = userValue.name
                chat.firebaseToken = userValue.firebaseToken
            }
        }

        histortList.set(index,chat)

        // histortList.add(index,chat)
        //shortList()
        chatListAdapter?.notifyDataSetChanged()

    }


    private fun shortList() {
        if(chatListAdapter != null){
            Collections.sort(histortList,object : Comparator<Chat> {
                override fun compare(a1: Chat?, a2: Chat?): Int {
                    if (a1!!.timestamp == null || a2!!.timestamp == null)
                        return -1
                    else {
                        val long1 :Long= a1?.timestamp?.toLong()!!
                        val long2 :Long= a2?.timestamp?.toLong()!!
                        return long2.compareTo(long1)
                    }
                }

            })
            chatListAdapter?.notifyDataSetChanged()
        }

    }



}
