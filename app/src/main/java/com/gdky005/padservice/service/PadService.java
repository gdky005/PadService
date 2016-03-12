package com.gdky005.padservice.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.gdky005.padservice.PadApplication;
import com.gdky005.padservice.dao.bean.KuwoBean;
import com.gdky005.padservice.dao.bean.KuwoKBean;
import com.gdky005.padservice.emnu.KuwoProgramEmnu;
import com.gdky005.padservice.utils.KuwoDataUtils;
import com.gdky005.padservice.utils.L;
import com.gdky005.padservice.utils.NotifyUtils;
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
public class PadService extends BaseService implements LivePlayerManager.OnLiveEndListener {

    public static final String NOTIFICATION_SUCCESS_PLAY_MUSIC_FLAG =
            "notification_success_play_music_flag";
    public static final String TIME_BEAN_FLAG = "time_bean_flag";

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
        mLivePlayerManager = LivePlayerManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand");
        kuwoDataUtils = new KuwoDataUtils(context);

        requestCount = 0;
        mp3Count = 0;
        currentProgram = 0;

        context = PadApplication.getContext();

        mp3BeanMap = new HashMap();


//        preparePlayMusic(true);

//        testPlayData();


        //保证服务不被杀死
        return Service.START_STICKY;
    }

    private void testPlayData() {
        //三剑客 音频
        String mp3UrlSJK = "http://image.kaolafm" +
                ".net/mz/audios/201603/b860d25b-87e1-4901-9fd1-44ce4ddc6e61.mp3";
        //某一时刻的直播
        String m3u8UrlZB = "http://ugclbs.kaolafm.com/aaf30a55ec51aa6f/1403152088_1534064922/playlist.m3u8";
        //酷音调频 mp3
        String mp3Url = "http://other.web.rh01.sycdn.kuwo.cn/4e2e51ac77f4b29e60e82141b8216838/56dd74f1/resource/n1/95/36/170925567.mp3";
        //酷音调频 aar
        String aarUrl = "http://other.web.rh03.sycdn.kuwo.cn/0895137d37b65e2927054c07a5ed7126/56dd752e/resource/a3/68/48/4079355865.aac";

        mLivePlayerManager.start(mp3UrlSJK);
    }

    @Subscriber(tag = NOTIFICATION_SUCCESS_PLAY_MUSIC_FLAG)
    private synchronized void preparePlayMusic(boolean isPlay) {
        kuwoDataUtils.initRequestData();
    }



    /**
     * 从并发的请求里面获取 对应真实的音频地址
     *
     * @param musiclistEntity
     */
    @Subscriber(tag = KuwoDataUtils.ONE_NOTIFICATION_ONCE_REQUEST_FLAG)
    public synchronized void getRequestData(KuwoKBean musiclistEntity) {
        if (requestCount <= KuwoProgramEmnu.values().length) {

            if (KuwoProgramEmnu.values().length == ++requestCount) {
                kuwoDataUtils.getMp3AddressForMap();
                return;
            }
        } else {
            requestCount = 0;
        }

    }

    @Subscriber(tag = KuwoDataUtils.ONE_NOTIFICATION_FOR_ONCE_MUSIC_DATA_FLAG)
    public synchronized void getMp3Map(KuwoBean kuwoBean) {

        if (mp3Count <= KuwoProgramEmnu.values().length) {

            if (KuwoProgramEmnu.values().length == ++mp3Count) {
                playMusic();
                return;
            }
        } else {
            mp3Count = 0;
        }

    }

    /**
     * 开始逐个播放音乐
     */
    private void playMusic() {
        mp3BeanMap = sortMp3Address();

        if (mp3BeanMap != null && mp3BeanMap.size() > currentProgram) {
            KuwoBean kuwoBean = (KuwoBean) mp3BeanMap.get(currentProgram++);
            if (kuwoBean != null && !TextUtils.isEmpty(kuwoBean.getUrl())) {
                playMusic(kuwoBean.getUrl());
            } else {
                mLivePlayerManager.reset();
                L.i("播放列表已经播完");
                NotifyUtils.showNotify(context, "暂无播放节目", "无", "");
            }
        } else {
            mLivePlayerManager.reset();
            currentProgram = 0;
            NotifyUtils.showNotify(context, "今日播放完成", "已经播完", "");
            L.i("播放声音的列表为null，或是播放列表已经播完");
        }
    }

    private void playMusic(String url) {
        NotifyUtils.showNotify(context, "正在播放", "播放音频中", "");
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
            }
            L.i("测试{}", mp3BeanMap.toString());
            return mp3BeanMap;
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

    @Override
    public void onLiveEnd() {
        playMusic();
    }

    public class PadBinder extends Binder {

        String pad_tag = "pad_tag";

        public PadService getService() {
            L.i("getService ---> {}", PadService.class.getSimpleName());
            return PadService.this;
        }

    }

}
