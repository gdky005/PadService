package com.gdky005.padservice.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

import com.gdky005.padservice.receiver.AlarmReceiver;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by WangQing on 16/3/8.
 */
public class AlarmUtils {

    public static void startAlarm(Context context, int hour, int minute) {
        hour = 7;
        minute = 30;

        //每天定时执行任务
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);

        long firstTime = SystemClock.elapsedRealtime(); // 开机之后到现在的运行时间(包括睡眠时间)
        long systemTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
// 这里时区需要设置一下，不然会有8个小时的时间差
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
// 选择的定时时间
        long selectTime = calendar.getTimeInMillis();
// 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if(systemTime > selectTime) {
            Toast.makeText(context,"设置的时间小于当前时间", Toast.LENGTH_SHORT).show();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }
// 计算现在时间到设定时间的时间差
        long time = selectTime - systemTime;
        firstTime += time;
// 进行闹铃注册
        AlarmManager manager = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstTime, 60 * 1000, sender);
        L.i("time ==== " + time +", selectTime ===== "
                + selectTime + ", systemTime ==== " + systemTime +", firstTime === " + firstTime);

        Toast.makeText(context, "设置重复闹铃成功! ", Toast.LENGTH_LONG).show();
    }

    private void defaultAlarm(Context context) {
//        //发送闹钟请求
//        Intent intent = new Intent(context, AlarmReceiver.class);
//        intent.setAction("something");
//        intent.setType("something");
//        intent.setData(Uri.EMPTY);
//        intent.addCategory("something");
//        intent.setClass(context, AlarmReceiver.class);
//// 以上给intent设置的四个属性是用来区分你发给系统的闹钟请求的，当你想取消掉之前发的闹钟请求，这四个属性，必须严格相等，所以你需要一些比较独特的属性，比如服务器返回给你的json中某些特定字段。
////当然intent中也可以放一些你要传递的消息。
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
////alarmCount是你需要记录的闹钟数量，必须保证你所发的alarmCount不能相同，最后一个参数填0就可以。
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        am.set(AlarmManager.RTC_WAKEUP, 10000, pendingIntent);
//
////这样闹钟的请求就发送出去了。time是你要被提醒的时间，单位毫秒，注意不是时间差。第一个参数提醒的需求用我给出的就可以，感兴趣的朋友，可以去google一下，这方面的资料非常多，一共有种，看一下就知道区别了。
////取消闹钟请求
//        Intent intent = new Intent(mContext, AlarmReceiver.class);
//        intent.setAction("something");
//        intent.setType(something);
//        intent.setData(Uri.EMPTY);
//        intent.addCategory(something);
//        intent.setClass(context, AlarmReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, alarmCount, intent, 0);
////alarmCount对应到你设定时的alarmCount,
//        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//        am.cancel(pendingIntent);
    }
}
