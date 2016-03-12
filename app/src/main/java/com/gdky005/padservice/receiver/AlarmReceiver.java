package com.gdky005.padservice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gdky005.padservice.service.PadService;
import com.gdky005.padservice.utils.L;
import com.gdky005.padservice.utils.NotifyUtils;

import org.simple.eventbus.EventBus;

/**
 * Created by WangQing on 16/2/29.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        L.i("收到闹钟提醒了");
        EventBus.getDefault().post(true, PadService.NOTIFICATION_SUCCESS_PLAY_MUSIC_FLAG);

        NotifyUtils.showNotify(context, "闹钟时间到啦！", "获取最喜欢的节目单", "闹钟");
    }
}
