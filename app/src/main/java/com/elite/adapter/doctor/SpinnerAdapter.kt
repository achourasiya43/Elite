package com.elite.adapter.doctor

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import com.elite.R
import com.elite.model.doctor_side.ItemData
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.finish_item_layout.view.*

import java.util.ArrayList

/**
 * Created by anil on 4/12/17.
 */

class SpinnerAdapter(context: Context, internal var groupid: Int, id: Int, internal var list: ArrayList<ItemData>) :
        ArrayAdapter<ItemData>(context, id, list) {
    internal var inflater: LayoutInflater

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = inflater.inflate(groupid, parent, false)

        if(!list.get(position).agencyImg.equals("")){
            Picasso.with(context).load(list.get(position).agencyImg).placeholder(R.drawable.agency_icon).into(itemView.img)
        }

        itemView.txt.text = list.get(position).agency


        return itemView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)

    }
}
