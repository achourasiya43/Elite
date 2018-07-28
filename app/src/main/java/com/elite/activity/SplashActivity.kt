package com.elite.activity

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.elite.R
import com.elite.app_session.SessionManager
import com.elite.helper.Constant
import es.dmoral.toasty.Toasty
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ("" !is String) { // same as !(obj is String)
            print("Not a String")
        }
        var session : SessionManager = SessionManager(this)
        if(session.isLoggedIn){
            val i: Int? = session.user?.userDetail?.userType?.toIntOrNull()
            Constant.usertype = i!!
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }else
        {
            val intent = Intent(this,WelcomeActivity::class.java)
            startActivity(intent)
            demo("")
            finish()
        }


        printHashKey(this)

    }
    fun demo(x: Any) {
        if (x is Intent) {
            Toasty.error(this,""+x);
        }
    }


    fun printHashKey(pContext: Context) {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("Hash Key:", "printHashKey() Hash Key: " + hashKey)
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("", "printHashKey()", e)
        }

    }
}
