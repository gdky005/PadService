package com.gdky005.padservice.dao.callback;

import com.alibaba.fastjson.JSON;
import com.gdky005.padservice.dao.bean.KuwoDataBean;
import com.zhy.http.okhttp.callback.Callback;

import okhttp3.Response;

/**
 * Created by WangQing on 16/2/29.
 */
public abstract class ProgramMusicDataCallback extends Callback<KuwoDataBean> {

    @Override
    public KuwoDataBean parseNetworkResponse(Response response) throws Exception {
        String responseData = response.body().string();
        KuwoDataBean programMusicDataCallback = JSON.parseObject(responseData,
                KuwoDataBean.class);
        return programMusicDataCallback;
    }
}
