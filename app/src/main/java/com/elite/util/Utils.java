package com.elite.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.elite.activity.LoginActivity;
import com.elite.model.Country;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mindiii on 23/9/17.
 */

public class Utils {

    public static List<Country> loadCountries(Context context){
        try{
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "country_code.json"));
            List<Country> countries = new ArrayList<>();
            for(int i=0;i<array.length();i++){
                Country country = gson.fromJson(array.getString(i), Country.class);
                countries.add(country);
            }
            return countries;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }



    }

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }}
        return false;
    }


    public static  void sessionExDialog(final Context mContext){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setMessage("Your session is expaire please login again");
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialogBuilder.setCancelable(true);

                 Intent showLogin = new Intent(mContext, LoginActivity.class);
                showLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                showLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(showLogin);
            }
        });
        alertDialogBuilder.show();}

    private static String loadJSONFromAsset(Context context, String jsonFileName) {
        String json = null;
        InputStream is=null;
        try {
            AssetManager manager = context.getAssets();
            Log.d("path","path "+jsonFileName);
            is = manager.open(jsonFileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }



}
