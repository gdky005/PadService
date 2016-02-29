package com.gdky005.padservice.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gdky005.padservice.PadApplication;
import com.gdky005.padservice.dao.KuwoDao;
import com.gdky005.padservice.dao.bean.KuwoProgramBean;
import com.gdky005.padservice.dao.callback.ProgramListCallback;
import com.gdky005.padservice.utils.L;

import java.util.Hashtable;
import java.util.Iterator;

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

        initData();


        //保证服务不被杀死
        return Service.START_STICKY;
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
                L.i("获取的音频数据是：" + "id->" + key + ", " + "数据实体->" + value.getName());


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
            Log.i("TAG", "getService ---> " + PadService.this);
            return PadService.this;
        }

    }

}
