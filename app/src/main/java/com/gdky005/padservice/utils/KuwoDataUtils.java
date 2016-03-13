package com.gdky005.padservice.utils;

import android.content.Context;

import com.gdky005.padservice.dao.KuwoDao;
import com.gdky005.padservice.dao.bean.KuwoDataBean;
import com.gdky005.padservice.dao.bean.KuwoKBean;
import com.gdky005.padservice.dao.callback.ProgramKListCallback;
import com.gdky005.padservice.dao.callback.ProgramMusicDataCallback;

import org.simple.eventbus.EventBus;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by WangQing on 16/3/8.
 */
public class KuwoDataUtils {

    public static final String ONE_NOTIFICATION_ONCE_REQUEST_FLAG = "ONE_NOTIFICATION_ONCE_REQUEST_FLAG";
    public static final String ONE_NOTIFICATION_FOR_ONCE_MUSIC_DATA_FLAG = "one_notification_for_once_music_data_flag";

    public Map idMaps;
    public Map mp3Map;
    public KuwoDao kuwoDao;
    public Context context;

    public KuwoDataUtils(Context context) {
        this.context = context;

        kuwoDao = new KuwoDao();
        idMaps = new Hashtable();
        mp3Map = new Hashtable();
    }

    public void initRequestData() {

        ProgramKListCallback programListCallback = new ProgramKListCallback() {
            @Override
            public void onError(Call call, Exception e) {
                L.e("错误信息是：" + e);
            }

            @Override
            public void onResponse(KuwoKBean response) {
                L.json(response.toString());


                //只获取最新的
                String albumId = response.getMusiclist().get(0).getMusicrid();

                String name = String.valueOf(response.getProgramId());

                KuwoKBean.MusiclistEntity musicEntity = response.getMusiclist().get(0);

                if (!idMaps.containsKey(name)) {
                    idMaps.put(name, musicEntity);
                }


                EventBus.getDefault().post(response, ONE_NOTIFICATION_ONCE_REQUEST_FLAG);
            }
        };

        kuwoDao.getKWYYTPList(programListCallback);
        kuwoDao.getMXRJList(programListCallback);
        kuwoDao.getTXCBXWList(programListCallback);


    }

    public void getMp3AddressForMap() {
        L.i("发起请求：idMaps->{}",idMaps.size());
        if (idMaps.size() > 0) {
            Iterator<String> keys = idMaps.keySet().iterator();

            while (keys.hasNext()) {
                String programId = keys.next(); // 代表id
                KuwoKBean.MusiclistEntity musiclistEntity =
                        (KuwoKBean.MusiclistEntity) idMaps.get(programId); //数据实体
                String albumId = musiclistEntity.getMusicrid(); // 代表id

                L.i("获取的音频数据是：programId->{}, 节目id->{}, 数据实体->{}",
                        programId, albumId,
                        musiclistEntity.getName());

                requestProgramData(programId, musiclistEntity);
            }
        }
    }

    private void requestProgramData(String programId, final KuwoKBean.MusiclistEntity musiclistEntity) {
        L.i("发起请求：programId->{}",programId);
        kuwoDao.getProgramData(programId, musiclistEntity.getMusicrid(), new ProgramMusicDataCallback() {
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

                musiclistEntity.setMid(mid);
                musiclistEntity.setUrl(url);
                musiclistEntity.setProgramId(programId);

                mp3Map.put(programId, musiclistEntity);

                EventBus.getDefault().post(musiclistEntity, ONE_NOTIFICATION_FOR_ONCE_MUSIC_DATA_FLAG);

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
