package com.elite.activity

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.elite.R
import com.elite.helper.Constant
import com.elite.helper.Helper
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)


       var helper = Helper(this)

       var  manager : LocationManager  = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            helper.buildAlertMessageNoGps();
        }

        iv_doctor.setOnClickListener({
            iv_doctor.setImageResource(R.drawable.active_new_doctor_icon)
            iv_agency.setImageResource(R.drawable.inactive_new_agency_logo)
            Constant.usertype = Constant.DOCTOR_TYPE


        })

        iv_agency.setOnClickListener({
            iv_doctor.setImageResource(R.drawable.inactive_new_doctor_icon)
            iv_agency.setImageResource(R.drawable.active_new_agency_icon)
            Constant.usertype = Constant.AGENCY_TYPE



        })

        tv_continue.setOnClickListener({
            if( Constant.usertype == 0){
                Toasty.error(this,"Select user Type").show()
            }else{
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

        })
    }

    override fun onResume() {
        super.onResume()
        Constant.usertype = 0
        iv_doctor.setImageResource(R.drawable.inactive_new_doctor_icon)
        iv_agency.setImageResource(R.drawable.inactive_new_agency_logo)
    }
}
