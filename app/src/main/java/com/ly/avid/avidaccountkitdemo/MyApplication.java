package com.ly.avid.avidaccountkitdemo;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.hola.sdk.HolaAnalysis;

import ly.avid.accountkit.AvidUserManager;

/**
 * Created by Holaverse on 2017/2/8.
 */

public class MyApplication extends Application {

    public static final String PRODUCT_ID = "11111";
    public static final String CHANNEL_ID = "22222";

    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(getApplicationContext());
        HolaAnalysis.init(this, PRODUCT_ID, CHANNEL_ID);
        AvidUserManager.init(this, null);
        AvidUserManager.setAccountkitThemeResId(R.style.Theme_Avid_AppLogin_Yellow);
    }
}
