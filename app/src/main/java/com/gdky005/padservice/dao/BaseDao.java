package com.gdky005.padservice.dao;

import com.zhy.http.okhttp.builder.OkHttpRequestBuilder;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.request.RequestCall;

/**
 * Created by WangQing on 16/2/29.
 */
public class BaseDao {

    /**
     * 格式分为：mp3 和 aac
     */
    public final String FORMAT_FIELD = "aac";
    public final String CHARSET_NAME = "UTF-8";


    public void addRequest(OkHttpRequestBuilder requestBuilder, Callback callback){
        RequestCall requestCall = requestBuilder.build();
        requestCall.execute(callback);
    }

}
