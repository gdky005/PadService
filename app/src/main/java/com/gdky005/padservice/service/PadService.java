package com.gdky005.padservice.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.gdky005.padservice.PadApplication;
import com.gdky005.padservice.dao.KuwoDao;
import com.gdky005.padservice.dao.bean.KuwoProgramBean;
import com.gdky005.padservice.dao.callback.ProgramListCallback;
import com.gdky005.padservice.receiver.AlarmReceiver;
import com.gdky005.padservice.utils.L;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimeZone;

import okhttp3.Call;

/**
 * Service 启动对话框 ，需要加系统参数，并在Manifest里面配置对应的权限
 * <p/>
 * Created by WangQing on 16/2/19.
 */
public class PadService extends BaseService {

    public IBinder mBinder = new PadBinder();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand");

        context = PadApplication.getContext();

        startAlarm(0, 0);

        initData();


        //保证服务不被杀死
        return Service.START_STICKY;
    }

    private void startAlarm(int hour, int minute) {
        hour = 7;
        minute = 30;

//        if (hour == 0)
//            hour = 7;
//        if (minute == 0)
//            minute = 30;

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
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstTime, 60 * 1000, sender);
        L.i("time ==== " + time +", selectTime ===== "
                + selectTime + ", systemTime ==== " + systemTime +", firstTime === " + firstTime);
        Toast.makeText(context, "设置重复闹铃成功! ", Toast.LENGTH_LONG).show();



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

//这样闹钟的请求就发送出去了。time是你要被提醒的时间，单位毫秒，注意不是时间差。第一个参数提醒的需求用我给出的就可以，感兴趣的朋友，可以去google一下，这方面的资料非常多，一共有种，看一下就知道区别了。
//取消闹钟请求
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

    //初始化数据
    private void initData() {
        kuwoDao = new KuwoDao();
        idMaps = new Hashtable();

        ProgramListCallback programListCallback = new ProgramListCallback() {
            @Override
            public void onError(Call call, Exception e) {
                L.e("错误信息是：" + e);
            }

            @Override
            public void onResponse(KuwoProgramBean response) {
                KuwoProgramBean.MusiclistEntity musicEntity = response.getMusiclist().get(0);
                String name = musicEntity.getName();
                String albumId = musicEntity.getAlbumid();

                if (!idMaps.containsKey(albumId)) {
                    idMaps.put(albumId, musicEntity);
                }

            }
        };

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                getMp3AddressForMap();
            }
        }, 5000);

        kuwoDao.getKWYYTPList(programListCallback);
        kuwoDao.getMXRJList(programListCallback);
        kuwoDao.getTXCBXWList(programListCallback);
    }

    private void getMp3AddressForMap() {
        if (idMaps.size() > 0) {
            Iterator<String> keys = idMaps.keySet().iterator();

            while (keys.hasNext()) {
                String key = keys.next(); // 代表id
                KuwoProgramBean.MusiclistEntity value = (KuwoProgramBean.MusiclistEntity)
                        idMaps.get(key); //数据实体
                L.i("获取的音频数据是：id->{}, 数据实体->{}",  key,  value.getName());


                //数据格式是：
//                        I: 获取的音频数据是：id->537590, 数据实体->小情侣地铁接吻秀恩爱持续7站......(吐小曹扒新闻2月29日)
//                        I: 获取的音频数据是：id->531242, 数据实体->你是什么样子，你所看到的世界就是什么样子（莫萱日记2月29日）
//                        I: 获取的音频数据是：id->161281, 数据实体->说一次让你难忘的邂逅(酷我音乐调频Vol.206)

            }


        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        log("onBind");

        IBinder mIBinder = null;
        if (mIBinder == null) {
            mIBinder = new PadBinder();
            return mIBinder; //也可以像上面几个语句那样重新new一个IBinder
//        //如果这边不返回一个IBinder的接口实例，那么ServiceConnection中的onServiceConnected就不会被调用
//        //那么bind所具有的传递数据的功能也就体现不出来~\(≧▽≦)/~啦啦啦（这个返回值是被作为onServiceConnected中的第二个参数的）

        }

        return mIBinder;
    }

    public class PadBinder extends Binder {

        String pad_tag = "pad_tag";

        public PadService getService() {
            L.i("getService ---> {}",  PadService.class.getSimpleName());
            return PadService.this;
        }

    }

}
