package com.elite.adapter.common

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elite.R
import com.elite.custom_listner.getCategory
import com.elite.model.CategoryInfo
import kotlinx.android.synthetic.main.category_item.view.*

/**
 * Created by Vicky on 12-Nov-17.
 */
class CategoryAdapter(mcontext: Context, catList: ArrayList<CategoryInfo.AllCategoryListBean>, listner: getCategory,viewType:Int)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mcontext: Context = mcontext
    var catList = catList
    var listner: getCategory = listner
    var viewType:Int = viewType


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

        if(viewType == 0){
            var view: View = LayoutInflater.from(parent!!.getContext()).inflate(R.layout.category_item, parent, false)
            return ViewHolder(view);
        }
        else if(viewType == 1){
            var view: View = LayoutInflater.from(parent!!.getContext()).inflate(R.layout.list_item, parent, false)
            return ViewHolderForLooking(view);
        }
        return null!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        var category: ArrayList<CategoryInfo.AllCategoryListBean> = catList

        if(viewType == 0){

            var viewholder   = holder as ViewHolder
            viewholder.bind(category.get(position), listner)

        }else if(viewType == 1){

            var viewforlooking   = holder as ViewHolderForLooking
            viewforlooking.bindForLooking(category.get(position), listner)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return viewType

    }

    override fun getItemCount(): Int {

        return catList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: CategoryInfo.AllCategoryListBean, listner: getCategory) = with(itemView) {
            chackbox.setClickable(false)

            if (item.ischecked) {
                chackbox.isChecked = true
            } else {
                chackbox.isChecked = false
            }

            category_name.text = item.name

            setOnClickListener {
                listner.getValue(item)
            }
        }
    }

    class ViewHolderForLooking(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindForLooking(item: CategoryInfo.AllCategoryListBean, listner: getCategory) = with(itemView) {


            category_name.text = item.name.toString().trim()

            setOnClickListener {
                listner.getValue(item)
            }
        }
    }
}


