package com.gdky005.padservice.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.gdky005.padservice.MainActivity;
import com.gdky005.padservice.R;

/**
 * Created by WangQing on 16/3/12.
 */
public class NotifyUtils {

    /**
     * 显示通知栏
     *
     * @param context
     * @param title     标题
     * @param contextText       内容
     * @param subText   副标题
     */
    public static void showNotify(Context context, String title, String contextText, String subText) {
        long time = System.currentTimeMillis();

//        title = "默认标题";
//        contextText = "默认内容";
//        subText = "副标题";

        NotificationManager manager = (NotificationManager) context.getSystemService(android
                .content.Context.NOTIFICATION_SERVICE);

        //MainActivity是你点击通知时想要跳转的Activity
        Intent playIntent = new Intent(context, MainActivity.class);
//        playIntent.putExtra("id", id);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, String.valueOf(time).hashCode(), playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title)
                .setContentText(contextText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSubText(subText);

        manager.notify(1, builder.build());
    }
}
