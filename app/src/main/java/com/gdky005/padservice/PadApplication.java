package com.gdky005.padservice;

import android.app.Application;
import android.content.Context;

import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by WangQing on 16/2/29.
 */
public class PadApplication extends Application {

    public static Context context;

    public OkHttpUtils mOkHttpUtils;
    public OkHttpClient okHttpClient;


    @Override
    public void onCreate() {
        super.onCreate();

        context = this;


        mOkHttpUtils = OkHttpUtils.getInstance();
        mOkHttpUtils.debug(getString(R.string.app_name));

        okHttpClient = mOkHttpUtils.getOkHttpClient();
        okHttpClient.newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS);
        okHttpClient.newBuilder().readTimeout(15000, TimeUnit.MILLISECONDS);
        okHttpClient.newBuilder().writeTimeout(15000, TimeUnit.MILLISECONDS);

        mOkHttpUtils.setOkHttpClient(okHttpClient);


    }

    public static Context getContext() {
        return context;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public OkHttpUtils getOkHttpUtils() {
        return mOkHttpUtils;
    }
}
