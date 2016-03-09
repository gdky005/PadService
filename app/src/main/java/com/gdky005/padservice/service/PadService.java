package com.gdky005.padservice.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.gdky005.padservice.PadApplication;
import com.gdky005.padservice.dao.bean.KuwoBean;
import com.gdky005.padservice.emnu.KuwoProgramEmnu;
import com.gdky005.padservice.utils.AlarmUtils;
import com.gdky005.padservice.utils.KuwoDataUtils;
import com.gdky005.padservice.utils.L;
import com.kaolafm.live.utils.LivePlayerManager;

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
    private Map mp3Map;
    private Map mp3BeanList;

    private Handler handler = new Handler();
    public IBinder mBinder = new PadBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand");
        context = PadApplication.getContext();
        mLivePlayerManager = LivePlayerManager.getInstance(this);
        mp3BeanList = new HashMap();

        AlarmUtils.startAlarm(context, 0, 0);


        kuwoDataUtils = new KuwoDataUtils(context);

        kuwoDataUtils.initRequestData();

        //6秒后获取已经获取的音频数据列表
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                mp3Map = kuwoDataUtils.getMp3Map();


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
                            if (KuwoProgramEmnu.values()[i].toString().equals(programId)){
                                mp3BeanList.put(KuwoProgramEmnu.values()[i].ordinal(), kuwoBean);
                                break;
                            }
                        }
//
                        L.i("测试{}", mp3BeanList.toString());
                    }
                }

                KuwoBean kuwoBean = (KuwoBean) mp3BeanList.get(0);
                mLivePlayerManager.start(kuwoBean.getUrl());
            }
        }, 10000);


        //保证服务不被杀死
        return Service.START_STICKY;
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
