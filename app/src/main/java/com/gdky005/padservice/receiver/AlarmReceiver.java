package com.gdky005.padservice.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.gdky005.padservice.MainActivity;
import com.gdky005.padservice.R;
import com.gdky005.padservice.service.PadService;
import com.gdky005.padservice.utils.L;

import org.simple.eventbus.EventBus;

/**
 * Created by WangQing on 16/2/29.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private NotificationManager manager;
    int i = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        L.i("收到闹钟提醒了");

        long time = System.currentTimeMillis();

        EventBus.getDefault().post(true, PadService.NOTIFICATION_SUCCESS_PLAY_MUSIC_FLAG);

        manager = (NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        //例如这个id就是你传过来的
        String id = intent.getStringExtra("id");
        //MainActivity是你点击通知时想要跳转的Activity
        Intent playIntent = new Intent(context, MainActivity.class);
        playIntent.putExtra("id", id);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, String.valueOf(time).hashCode(), playIntent,
                PendingIntent.FLAG_ONE_SHOT);
//                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("title：" + context.getString(R.string.app_name))
                .setContentText("提醒时间:" + time)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSubText("包名：" + context
                        .getPackageName());
        manager.notify(1, builder.build());
    }
}
