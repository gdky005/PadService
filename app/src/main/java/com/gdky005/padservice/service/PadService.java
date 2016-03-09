package com.gdky005.padservice.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.gdky005.padservice.PadApplication;
import com.gdky005.padservice.dao.bean.KuwoBean;
import com.gdky005.padservice.dao.bean.KuwoProgramBean;
import com.gdky005.padservice.emnu.KuwoProgramEmnu;
import com.gdky005.padservice.utils.AlarmUtils;
import com.gdky005.padservice.utils.KuwoDataUtils;
import com.gdky005.padservice.utils.L;
import com.kaolafm.live.utils.LivePlayerManager;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Service 启动对话框 ，需要加系统参数，并在Manifest里面配置对应的权限
 * <p/>
 * Created by WangQing on 16/2/19.
 */
public class PadService extends BaseService {

    private LivePlayerManager mLivePlayerManager;
    private KuwoDataUtils kuwoDataUtils;
    private Map mp3BeanMap;

    private int requestCount = 0;
    private int mp3Count = 0;
    private int currentProgram = 0;

    private Handler handler = new Handler();
    public IBinder mBinder = new PadBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand");
        context = PadApplication.getContext();
        mLivePlayerManager = LivePlayerManager.getInstance(this);
        mp3BeanMap = new HashMap();

        requestCount = 0;
        mp3Count = 0;
        currentProgram = 0;

        AlarmUtils.startAlarm(context, 0, 0);

        kuwoDataUtils = new KuwoDataUtils(context);
        kuwoDataUtils.initRequestData();

        //保证服务不被杀死
        return Service.START_STICKY;
    }

    /**
     * 从并发的请求里面获取 对应真实的音频地址
     *
     * @param musiclistEntity
     */
    @Subscriber(tag = KuwoDataUtils.ONE_NOTIFICATION_ONCE_REQUEST_FLAG)
    public void getRequestData(KuwoProgramBean musiclistEntity) {
        if (requestCount <= KuwoProgramEmnu.values().length) {

            if (KuwoProgramEmnu.values().length == requestCount) {
                kuwoDataUtils.getMp3AddressForMap();
                return;
            }

            ++requestCount;
        } else {
            requestCount = 0;
        }

    }

    @Subscriber(tag = KuwoDataUtils.ONE_NOTIFICATION_FOR_ONCE_MUSIC_DATA_FLAG)
    public void getMp3Map(KuwoBean kuwoBean) {

        if (mp3Count <= KuwoProgramEmnu.values().length) {

            if (KuwoProgramEmnu.values().length == mp3Count) {
                playMusic();
                return;
            }

            ++mp3Count;
        } else {
            mp3Count = 0;
        }

    }

    /**
     * 开始逐个播放音乐
     */
    private void playMusic() {
        mp3BeanMap = sortMp3Address();

        if (mp3BeanMap != null) {
            KuwoBean kuwoBean = (KuwoBean) mp3BeanMap.get(currentProgram++);
            if (kuwoBean != null) {
                mLivePlayerManager.addLiveEndListener(new LivePlayerManager.OnLiveEndListener() {
                    @Override
                    public void onLiveEnd() {
                        if (currentProgram <= mp3BeanMap.size()) {
                            playMusic();
                        }
                    }
                });
                playMusic(kuwoBean.getUrl());
            }
        }
    }

    private void playMusic(String url) {
        mLivePlayerManager.start(url);
    }

    private Map sortMp3Address() {
        Map mp3Map = kuwoDataUtils.getMp3Map();

        L.i("你的mp3Map大小是：{}", mp3Map.size());

        if (mp3Map.size() > 0) {
            Iterator<String> keys = mp3Map.keySet().iterator();

            while (keys.hasNext()) {
                String programId = keys.next(); // 代表id
                KuwoBean kuwoBean = (KuwoBean) mp3Map.get(programId);
                String albumId = kuwoBean.getMid(); // 代表id
                String url = kuwoBean.getUrl();

                L.i("酷我音频数据是：programId->{}, 节目id->{}, 音频->{}",
                        programId, albumId, url);

                //对酷我音频的id进行个性化排序
                for (int i = 0; i < KuwoProgramEmnu.values().length; i++) {
                    if (KuwoProgramEmnu.values()[i].toString().equals(programId)) {
                        mp3BeanMap.put(KuwoProgramEmnu.values()[i].ordinal(), kuwoBean);
                        break;
                    }
                }
//
                L.i("测试{}", mp3BeanMap.toString());

                return mp3BeanMap;
            }
        }
        return null;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public class PadBinder extends Binder {

        String pad_tag = "pad_tag";

        public PadService getService() {
            L.i("getService ---> {}", PadService.class.getSimpleName());
            return PadService.this;
        }

    }

}
