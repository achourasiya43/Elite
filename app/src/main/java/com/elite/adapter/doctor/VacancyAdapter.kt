package com.elite.adapter.doctor

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elite.R
import com.elite.custom_listner.VacancyListner
import kotlinx.android.synthetic.main.vacancy_item.view.*

/**
 * Created by anil on 22/11/17.
 */
class VacancyAdapter(mContext:Context,vacancyList:ArrayList<String>,listner : VacancyListner) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mContext = mContext
    var listner = listner
    var vacancyList = vacancyList


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
      var v: View = LayoutInflater.from(parent?.getContext()).inflate(R.layout.vacancy_item,parent,false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        var viewHolder = holder as ViewHolder

        viewHolder.bind(vacancyList,position,listner)
    }


    override fun getItemCount(): Int {
        return vacancyList.size
    }

    class ViewHolder(Itemview:View): RecyclerView.ViewHolder(Itemview){

        fun bind(vacancyList: ArrayList<String>, position: Int, listner: VacancyListner) = with(itemView){

            tv_vacancy_count_item.text = vacancyList.get(position)

            setOnClickListener {
                listner.getValue(vacancyList.get(position))
            }
        }

    }
}