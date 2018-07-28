package com.elite.helper

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.elite.R
import com.elite.model.Country
import kotlinx.android.synthetic.main.change_password_dialog.*
import java.util.*
import android.app.TimePickerDialog
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.TimePicker
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.elite.WebService.WebServices
import com.elite.adapter.doctor.SpinnerAdapter
import com.elite.app_session.SessionManager
import com.elite.custom_listner.*
import com.elite.model.agency_side.InteresedAgencyInfo
import com.elite.model.doctor_side.ItemData
import com.elite.util.Utils
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.finished_job_dialog_layout.*
import kotlinx.android.synthetic.main.fragment_looking_for.view.*
import kotlinx.android.synthetic.main.get_job_timing_dialog.*
import org.json.JSONException
import org.json.JSONObject


/**
 * Created by anil on 13/11/17.
 */
class Helper(context : Context){

    var mcontext : Context  = context
    var fromDate  = ""
    var toDate  = ""

    var agency_id = ""

    fun buildAlertMessageNoGps() {
        val builder =android.support.v7.app.AlertDialog.Builder(mcontext)
        builder.setTitle(R.string.gps_not_enable)
        builder.setMessage(R.string.do_you_want_to_enable)
                .setCancelable(false)
                .setPositiveButton("Yes", {
                    dialog, id -> mcontext.startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
                .setNegativeButton("No", {
                    dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()
    }

    fun SelectCountry(countries: ArrayList<Country>,code : MyListner):String {
        var countryCode = ""
        val list = ArrayList<String>()
        for (country in countries) {
            list.add(country.country_name + " (+" + country.phone_code + ")")
        }
        val builder = AlertDialog.Builder(mcontext)
        builder.setTitle("Select your country");
        val mEntries = list.toTypedArray<CharSequence>()
        builder.setItems(mEntries) { dialog, position ->
            //tv_country_code.text = String.format("+%s", countries[position].phone_code)
            code.getcountry(String.format("+%s", countries[position].phone_code))
            countryCode  = "+" + countries[position].phone_code
            //codeId =  countries[position].code.toString()

            dialog.dismiss()
        }

        builder.setNegativeButton("cancel") { dialog, which -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
        return countryCode
    }

    fun changePasswordDialog( myClickListner: MyClickListner ) {
        val openDialog = Dialog(mcontext)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setCancelable(false);
        openDialog.setContentView(R.layout.change_password_dialog)
        openDialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        openDialog.tv_ok.setOnClickListener( {


            var oldPassword :String = openDialog.ed_old_password.text.toString()
            var newPassword :String = openDialog.new_password.text.toString()

            if( isValid(openDialog)){
                myClickListner?.getPassword(oldPassword,newPassword,openDialog)
            }

        })
        openDialog.tv_cancel.setOnClickListener({
            openDialog.dismiss()

        })
        openDialog.show()
    }

    fun getJobTiming(fromtime:String,toTime:String,tv_timing:TextView,datePick: getDatePicker) {

        val mcurrentTime = Calendar.getInstance()
        val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mcurrentTime.get(Calendar.MINUTE)
        var mTimePicker: TimePickerDialog

        val timingDialog = Dialog(mcontext)
        timingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        timingDialog.setCancelable(false);
        timingDialog.setContentView(R.layout.get_job_timing_dialog)
        timingDialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        timingDialog.tv_from_time_.text = fromtime
        timingDialog.tv_to_time.text = toTime

        fromDate = fromtime
        toDate = toTime

        timingDialog.tv_from_time_.setOnClickListener( {

            mTimePicker = TimePickerDialog(mcontext,object :TimePickerDialog.OnTimeSetListener{
                override fun onTimeSet(p0: TimePicker?, selectedHour: Int, selectedMinute: Int) {

                    var format: String
                    var selectedHour = selectedHour

                    if (selectedHour == 0)
                    {
                        selectedHour += 12;format = "AM";
                    } else if (selectedHour == 12)
                    {format = "PM";}
                    else if (selectedHour > 12)
                    {selectedHour -= 12;format = "PM";}
                    else {format = "AM";}

                    timingDialog.tv_from_time_.setText(""+selectedHour + ":" + selectedMinute +" "+ format)
                    fromDate = (""+selectedHour + ":" + selectedMinute +" "+ format)
                }

            }, hour, minute, true)
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()

        })

        timingDialog.tv_to_time.setOnClickListener({

            mTimePicker = TimePickerDialog(mcontext,object :TimePickerDialog.OnTimeSetListener{
                override fun onTimeSet(p0: TimePicker?, selectedHour: Int, selectedMinute: Int) {

                    var format: String
                    var selectedHour = selectedHour

                    if (selectedHour == 0)
                    {
                        selectedHour += 12;format = "AM";
                    } else if (selectedHour == 12)
                    {format = "PM";}
                    else if (selectedHour > 12)
                    {selectedHour -= 12;format = "PM";}
                    else {format = "AM";}

                    timingDialog.tv_to_time.setText(""+selectedHour + ":" + selectedMinute +" "+ format)
                    toDate = (""+selectedHour + ":" + selectedMinute +" "+ format)

                }

            }, hour, minute, true)
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()

        })

        timingDialog.tv_cancel_dialog.setOnClickListener({

            if(tv_timing.text.equals("")){
                timingDialog.tv_from_time_.text = ""
                timingDialog.tv_to_time.text = ""
                fromDate = ""
                toDate = ""
            }

            timingDialog.dismiss()

        })

        timingDialog.tv_done.setOnClickListener({
            if(fromDate.equals("")){
                Toasty.error(mcontext,"Select start job time").show()


            }else if(toDate.equals("")){
                Toasty.error(mcontext,"Select end job timing").show()
            }
            else if(fromDate.equals(toDate)){
                Toasty.error(mcontext,"You can't select same job time").show()
            }else{
                datePick.DatePicker(fromDate,toDate)
                timingDialog.dismiss()
            }

        })
        timingDialog.show()
    }

    private fun isValid(openDialog: Dialog): Boolean {
        val v = ValidData()

        if (!v.isNullValue(openDialog.ed_old_password)) {
            openDialog.ed_old_password.setError("Enter old password")
            openDialog.ed_old_password.requestFocus()
            return false

        }  else if (!v.isNullValue(openDialog.new_password)) {
            openDialog.new_password.setError("Enter new password")
            openDialog.new_password.requestFocus()
            return false

        } else if (!v.isPasswordValid(openDialog.new_password)) {
            openDialog.new_password.setError("Atleast 6 characters required")
            openDialog.new_password.requestFocus()
            return false
        }
        return true
    }


    fun finishedDialog(id:String, view: View,listner: FinishedJobListner){

        val list : ArrayList<ItemData> = ArrayList<ItemData>();

        val finishDialog = Dialog(mcontext)
        finishDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        finishDialog.setCancelable(false);
        finishDialog.setContentView(R.layout.finished_job_dialog_layout)
        finishDialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        interestedAgency(view,id,list,finishDialog)

        finishDialog.tv_done_.setOnClickListener( {
            if(!agency_id.equals("")){
                submitToFinishJob(view,finishDialog,id,agency_id,listner)
            }

        })

        finishDialog.tv_cancel_dialog_.setOnClickListener({
            finishDialog.dismiss()

        })

        finishDialog.show()
    }

    fun deletePostDialog(context: Context,view: View, post_id:String,deleteResponce:Delete_Dialog){

        val finishDialog = Dialog(mcontext)
        finishDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        finishDialog.setCancelable(false);
        finishDialog.setContentView(R.layout.delete_post_layout)
        finishDialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        finishDialog.tv_done.setOnClickListener( {
            deletePost(finishDialog,context,view,post_id,deleteResponce)
        })

        finishDialog.tv_cancel_dialog.setOnClickListener({
            finishDialog.dismiss()

        })

        finishDialog.show()
    }

    private fun deletePost(finishDialog:Dialog,context: Context,view: View, post_id:String,deleteResponce:Delete_Dialog) {

        var session  = SessionManager(context)
        if (Utils.isConnectingToInternet(context)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.Delete_Post,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {

                            var jsonObject = JSONObject(response)
                            var status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")

                            deleteResponce.deleteDialog(response)
                            if (status == "success") {
                                Toasty.success(context, message + "").show()
                                finishDialog.dismiss()
                            } else {
                                Toasty.error(context, message + "").show()
                            }
                        } catch (e: JSONException) {
                            view.spin_kit.visibility = View.GONE
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        view.spin_kit.visibility = View.GONE
                        Utils.sessionExDialog(context!!)
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = java.util.HashMap<String, String>()

                    params.put("post_id",post_id)

                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params = java.util.HashMap<String, String>()
                    val authToken = session?.user?.userDetail?.authToken
                    params.put("authToken",authToken.toString())
                    return params
                }
            }
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(context, "Please Check internet connection.!").show()
        }
    }

    private fun interestedAgency(view: View, post_id: String, list: ArrayList<ItemData>, finishDialog: Dialog) {
        var session = SessionManager(mcontext)
        var itemDate = ItemData()

        if (Utils.isConnectingToInternet(mcontext)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.InterestedAgency,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            var jsonObject:JSONObject = JSONObject(response)
                            var status_:String = jsonObject.getString("status")
                            val message: String? = jsonObject.getString("message")

                            if(status_.equals("success")){

                                val gson = Gson()
                                var interestedAgencyInfo: InteresedAgencyInfo = gson.fromJson(response, InteresedAgencyInfo::class.java)
                                var status: String? = interestedAgencyInfo?.status
                                //val message: String? = interestedAgencyInfo?.message
                                if (status == "success") {

                                    for(value in interestedAgencyInfo.InterestedAgencyList ){
                                        itemDate = ItemData()
                                        itemDate.agencyId = value.id.toString()
                                        itemDate.agency = value.name.toString()
                                        itemDate.agencyImg = value.profileImage.toString()
                                        list.add(itemDate)
                                    }

                                    itemDate = ItemData()
                                    itemDate.agencyId = "-1"
                                    itemDate.agency = "Other"
                                    itemDate.agencyImg = "new image"
                                    list.add(itemDate)



                                }
                            }
                            else {
                                itemDate = ItemData()
                                itemDate.agencyId = "-1"
                                itemDate.agency = "Other"
                                itemDate.agencyImg = "new image"
                                list.add(itemDate)
                               // Toasty.error(mcontext, message + "").show()
                            }


                            var adapter  =  SpinnerAdapter(mcontext, R.layout.finish_item_layout,R.id.txt,list);
                            finishDialog.spinner.setAdapter(adapter)
                            finishDialog.spinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener{
                                override fun onNothingSelected(p0: AdapterView<*>?) {
                                }

                                override fun onItemSelected(p0: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                                    agency_id = list.get(pos).agencyId
                                }

                            })
                        } catch (e: JSONException) {
                            view.spin_kit.visibility = View.GONE
                            Toasty.error(mcontext, "Something went wrong" + "").show()
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        view.spin_kit.visibility = View.GONE
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("post_id",post_id)

                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    val authToken = session?.user?.userDetail?.authToken
                    params.put("authToken",authToken.toString())
                    return params
                }
            }
            val requestQueue = Volley.newRequestQueue(mcontext)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(mcontext, "Please Check internet connection.!").show()
        }
    }

    private fun submitToFinishJob(view: View, finishDialog: Dialog,post_id: String,agency_id:String, listner: FinishedJobListner) {
        var session = SessionManager(mcontext)

        if (Utils.isConnectingToInternet(mcontext)) {
            view.spin_kit.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, WebServices.FinishJob,
                    Response.Listener { response ->
                        view.spin_kit.visibility = View.GONE
                        try {
                            var jsonObject:JSONObject = JSONObject(response)
                            var status_:String = jsonObject.getString("status")
                            val message: String? = jsonObject.getString("message")

                            if(status_.equals("success")){
                                finishDialog.dismiss()
                                Toasty.success(mcontext, message + "").show()
                                listner.successFinishedJob()

                            }
                            else {
                                Toasty.error(mcontext, message + "").show()
                            }
                        } catch (e: JSONException) {
                            view.spin_kit.visibility = View.GONE
                            Toasty.error(mcontext, "Something went wrong" + "").show()
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        view.spin_kit.visibility = View.GONE
                        Utils.sessionExDialog(mcontext)
                    }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("agency_id",agency_id)
                    params.put("post_id",post_id)

                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    val authToken = session?.user?.userDetail?.authToken
                    params.put("authToken",authToken.toString())
                    return params
                }
            }
            val requestQueue = Volley.newRequestQueue(mcontext)
            requestQueue.add(stringRequest)

        } else {
            Toasty.error(mcontext, "Please Check internet connection.!").show()
        }
    }
}