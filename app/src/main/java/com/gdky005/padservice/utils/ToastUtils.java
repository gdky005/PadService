package com.gdky005.padservice.utils;

import android.content.Context;
import android.widget.Toast;

import com.gdky005.padservice.PadApplication;

/**
 * Created by WangQing on 16/3/12.
 */
public class ToastUtils {

    public static void showToast(Context context, String msg) {
        if (context == null) {
            context = PadApplication.getContext();
        }

        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
