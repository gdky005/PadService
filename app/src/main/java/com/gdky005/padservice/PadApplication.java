package com.gdky005.padservice;

import android.app.Application;
import android.content.Context;

/**
 * Created by WangQing on 16/2/29.
 */
public class PadApplication extends Application {

    public Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;
    }
}
