package com.elite.adapter.doctor

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elite.R
import com.elite.fragment.doctor_side.LookingForFragment
import com.elite.helper.Constant
import com.elite.model.FinishedInfo
import kotlinx.android.synthetic.main.all_post_item_layout.view.*
import java.util.*
import com.elite.model.doctor_side.DrListInfo
import java.util.logging.Handler

/**
 * Created by anil on 22/11/17.
 */
class AllPostListAdapter(var list: ArrayList<DrListInfo.AllDoctorVacancyListBean>,var activity: FragmentActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {





    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        var v :View = LayoutInflater.from(parent?.context).inflate(R.layout.all_post_item_layout,parent,false)
        return ViewHolder(v, activity)
    }

    override fun onBindViewHolder(holder:RecyclerView.ViewHolder?, position: Int) {

        val allpost = list.get(position)
        val viewHolder =  holder as ViewHolder
        viewHolder.bind(allpost,position)


    }

    override fun getItemCount(): Int {
        return list.size
    }


    class ViewHolder (itemview: View, activity: FragmentActivity): RecyclerView.ViewHolder(itemview){

        var activity = activity
        var runnable :Runnable ?= null
        var handler1:Handler ? = null

        fun bind(allpost: DrListInfo.AllDoctorVacancyListBean, position: Int) = with(itemView){

            category.text = allpost.job_title
            salary.text = allpost.salary
            vacancy.text = allpost.vacancy_number
            qualification.text = allpost.qualification
            time_ago.text = allpost.day
            tv_interested.text = allpost.interested + " "+"Agency"

            var finishedInfo = FinishedInfo()

            setOnClickListener {
                addFragment(LookingForFragment.newInstance(allpost.id!!, allpost.interested!!, Constant.LookingForHome,finishedInfo),true,R.id.fragment_place)
/*
                if(handler1==null){
                    handler1 : Handler()
                }

                runnable = object : Runnable {
                    override fun run() {
                        handler1 = null;
                    }}

                handler.postDelayed(runnable, 800)*/

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