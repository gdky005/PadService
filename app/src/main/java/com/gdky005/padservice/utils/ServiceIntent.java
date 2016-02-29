package com.gdky005.padservice.utils;

import android.content.Context;
import android.content.Intent;

import com.gdky005.padservice.MainActivity;
import com.gdky005.padservice.PadApplication;

/**
 * Created by WangQing on 16/2/29.
 */
public class ServiceIntent {

    /**
     * 获取PadService Intent
     *
     * @param context
     * @return
     */
    public static Intent getPadIntent(Context context) {

        if (context == null) {
            context = PadApplication.getContext();
        }

        Intent intent = new Intent(MainActivity.PAD_SERVICE_FLAG);
        intent.setPackage(context.getPackageName());
        return intent;
    }
}
