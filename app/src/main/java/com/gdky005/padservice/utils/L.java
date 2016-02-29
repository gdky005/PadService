package com.gdky005.padservice.utils;

import android.util.Log;

/**
 * Created by WangQing on 16/2/29.
 */
public class L {
    private static final String TAG = "TAG";

    public static void i(String message) {
        Log.i(TAG, message);
    }

    public static void e(String message) {
        Log.e(TAG, message);
    }
}
