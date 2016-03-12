package com.gdky005.padservice.dao.callback;

import com.alibaba.fastjson.JSON;
import com.gdky005.padservice.dao.bean.KuwoKBean;
import com.zhy.http.okhttp.callback.Callback;

import okhttp3.HttpUrl;
import okhttp3.Response;

/**
 * Created by WangQing on 16/2/29.
 */
public abstract class ProgramKListCallback extends Callback<KuwoKBean> {
    @Override
    public KuwoKBean parseNetworkResponse(Response response) throws Exception {

        HttpUrl httpUrl = response.request().url();
        String programId = httpUrl.queryParameter("listid");

        byte[] bytes = response.body().bytes();
        String responseData = new String(bytes);

        KuwoKBean kuwoKBean = JSON.parseObject(responseData, KuwoKBean.class);
        kuwoKBean.setProgramId(Integer.parseInt(programId));

        return kuwoKBean;
    }

//    @Override
//    public KuwoProgramBean parseNetworkResponse(Response response) throws Exception {
//        KuwoProgramBean kuwoProgramBean = null;
//
//        byte[] bytes = response.body().bytes();
//        String responseData = new String(bytes);
//
//        L.i("从服务器获取的列表是：" + responseData);
//
//        if (!TextUtils.isEmpty(responseData) && responseData.contains("jsondata")) {
//
//            String start = "try{var jsondata=";
//            String end = "; json(jsondata);}catch(e){jsonError(e)}";
//
//            String kuwoListData = responseData.substring(start.length(),
//                    responseData.length() - end.length());
//
//            kuwoProgramBean = JSON.parseObject(kuwoListData, KuwoProgramBean.class);
//        }
//
//
//        return kuwoProgramBean;
//    }
}
