package com.gdky005.padservice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gdky005.padservice.utils.L;
import com.gdky005.padservice.utils.ServiceIntent;

/**
 * Android手机在启动的过程中会触发一个Standard Broadcast Action，名字叫android.intent.action.BOOT_COMPLETED(只会触发一次)
 *
 * Created by WangQing on 16/2/29.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = ServiceIntent.getPadIntent(context);
        context.startService(service);

        L.i("开机自动服务自动启动");

    }
}
