package com.gdky005.padservice;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

/**
 */
public class MyPadApplication extends PadApplication {

    @Override
    public void onCreate() {
        super.onCreate();



        context = this;

        Stetho.initializeWithDefaults(this);

        mOkHttpUtils = OkHttpUtils.getInstance();
        mOkHttpUtils.debug(getString(R.string.app_name));

        okHttpClient = mOkHttpUtils.getOkHttpClient();
        okHttpClient.newBuilder().retryOnConnectionFailure(true);
        okHttpClient.newBuilder().followRedirects(true);
        okHttpClient.newBuilder().addNetworkInterceptor(new StethoInterceptor());
        okHttpClient.newBuilder().connectTimeout(15000, TimeUnit.MILLISECONDS);
        okHttpClient.newBuilder().readTimeout(15000, TimeUnit.MILLISECONDS);
        okHttpClient.newBuilder().writeTimeout(15000, TimeUnit.MILLISECONDS);

        mOkHttpUtils.setOkHttpClient(okHttpClient);


    }
}
