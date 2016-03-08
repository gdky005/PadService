package com.gdky005.padservice.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.gdky005.padservice.dao.KuwoDao;
import com.gdky005.padservice.dao.bean.KuwoBean;
import com.gdky005.padservice.dao.bean.KuwoDataBean;
import com.gdky005.padservice.dao.bean.KuwoProgramBean;
import com.gdky005.padservice.dao.callback.ProgramListCallback;
import com.gdky005.padservice.dao.callback.ProgramMusicDataCallback;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by WangQing on 16/3/8.
 */
public class KuwoDataUtils {

    public static final int DATA_SUCCESS = 0;

    private boolean isFirstRequestData = true;

    public Map idMaps;
    public Map mp3Map;
    public KuwoDao kuwoDao;
    public Context context;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DATA_SUCCESS:
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getMp3AddressForMap();
                        }
                    }, 5000);

                    break;
            }
        }
    };

    public KuwoDataUtils(Context context) {
        this.context = context;

        kuwoDao = new KuwoDao();
        idMaps = new Hashtable();
        mp3Map = new Hashtable();

        isFirstRequestData = true;

        initRequestData();
    }

    public void initRequestData() {
        ProgramListCallback programListCallback = new ProgramListCallback() {
            @Override
            public void onError(Call call, Exception e) {
                L.e("错误信息是：" + e);
            }

            @Override
            public void onResponse(KuwoProgramBean response) {

                L.json(response.toString());

                //只获取最新的
                String albumId = String.valueOf(response.getMusiclist().get(0).getId());
                String name = String.valueOf(response.getId());

                KuwoProgramBean.MusiclistEntity musicEntity = response.getMusiclist().get(0);

                if (!idMaps.containsKey(name)) {
                    idMaps.put(name, musicEntity);
                }

                if (isFirstRequestData) {
                    handler.sendEmptyMessage(DATA_SUCCESS);
                    isFirstRequestData = false;
                }
            }
        };

        kuwoDao.getKWYYTPList(programListCallback);
        kuwoDao.getMXRJList(programListCallback);
        kuwoDao.getTXCBXWList(programListCallback);
    }

    private void getMp3AddressForMap() {
        if (idMaps.size() > 0) {
            Iterator<String> keys = idMaps.keySet().iterator();

            while (keys.hasNext()) {
                String programId = keys.next(); // 代表id
                KuwoProgramBean.MusiclistEntity musiclistEntity = (KuwoProgramBean.MusiclistEntity)
                        idMaps.get(programId); //数据实体
                String albumId = musiclistEntity.getId(); // 代表id

                L.i("获取的音频数据是：programId->{}, 节目id->{}, 数据实体->{}",
                        programId, albumId,
                        musiclistEntity.getName());

                requestProgramData(programId, musiclistEntity);
            }
        }
    }

    private void requestProgramData(String programId, KuwoProgramBean.MusiclistEntity musiclistEntity) {
        kuwoDao.getProgramData(programId, musiclistEntity.getId(), new ProgramMusicDataCallback() {
            @Override
            public void onError(Call call, Exception e) {
                L.e("获取数据失败");
            }

            @Override
            public void onResponse(KuwoDataBean response) {
                L.json(response.toString());

                String mid = response.getMid();
                String programId = response.getProgramId();
                String url = response.getUrl();

                L.i("当前获取的音频: mid->{}, programe->{}, 地址是：{}", response.getMid(), response
                        .getProgramId(), response
                        .getUrl());

                KuwoBean kuwoBean = new KuwoBean();
                kuwoBean.setMid(mid);
                kuwoBean.setProgramId(programId);
                kuwoBean.setUrl(url);

                mp3Map.put(programId, kuwoBean);

//                        03-08 18:59:48.562 I: ║ 当前获取的音频: mid->6958190, programe->1013437787, 地址是：http://other.web.rh01.sycdn.kuwo.cn/f743cf9dfd8b5c7926d3cb8fa417713b/56deb09e/resource/n1/94/66/3900324722.mp3
//                        03-08 18:59:48.565 I: ║ 当前获取的音频: mid->6951198, programe->1013437783, 地址是：http://other.web.rh01.sycdn.kuwo.cn/42c666014fca3adbb9f0131eb4699d12/56deb09e/resource/n1/9/6/248073288.mp3
//                        03-08 18:59:48.568 I: ║ 当前获取的音频: mid->6958193, programe->1013437785, 地址是：http://other.web.rh01.sycdn.kuwo.cn/17c4de492ddd0eac77014578a73977b7/56deb09e/resource/n2/20/55/4099406980.mp3

            }
        });
    }

    public Map getMp3Map() {
        return mp3Map;
    }
}
