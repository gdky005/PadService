package com.gdky005.padservice.dao;

import com.gdky005.padservice.emnu.KuwoProgramEmnu;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.callback.Callback;

/**
 * Created by WangQing on 16/2/29.
 */
public class KuwoDao extends BaseDao {

    /**
     * 获取 酷我音乐调频 的列表
     * @param callback
     */
    public void getKWYYTPList(Callback callback) {
        GetBuilder builder = getKuwoParmasBuilder(KuwoProgramEmnu.KWYYTP);

        addRequest(builder, callback);
    }

    /**
     * 获取 莫萱日记 的列表
     * @param callback
     */
    public void getMXRJList(Callback callback) {
        GetBuilder builder = getKuwoParmasBuilder(KuwoProgramEmnu.MXRJ);

        addRequest(builder, callback);
    }

    /**
     * 获取 爆笑糗事段子 的列表
     * @param callback
     */
    public void getBXQSDZList(Callback callback) {
        GetBuilder builder = getKuwoParmasBuilder(KuwoProgramEmnu.BXQSDZ);

        addRequest(builder, callback);
    }

    /**
     * 获取吐小槽扒新闻的列表
     *
     * @param callback
     */
    public void getTXCBXWList(Callback callback) {
        GetBuilder builder = getKuwoParmasBuilder(KuwoProgramEmnu.TXCBXW);

        addRequest(builder, callback);
    }

    /**
     * 获取某个音频的具体播放地址
     *
     * @param id
     * @param callback
     */
    public void getProgramData(String programId, String id, Callback callback) {
        String url = RequestApi.KUWO_AUDIO_DATA_URL;

        GetBuilder builder = new GetBuilder()
                .url(url)
                .addParams("programId", programId)
                .addParams("mid", id)
                .addParams("format", FORMAT_FIELD);

        addRequest(builder, callback);
    }


    /**
     * 获取 酷我音乐的通用参数
     *
     * @param programEmnu
     * @return
     */
    public GetBuilder getKuwoParmasBuilder(KuwoProgramEmnu programEmnu) {
        String url = RequestApi.KUWO_LIST_URL;

        GetBuilder builder = new GetBuilder()
                .url(url)
                .addParams("flag", "2")
                .addParams("listid", programEmnu.toString())
                .addParams("pn", "0")
                .addParams("rn", "2");  //只获取最近的数据
//
//       GetBuilder builder = new GetBuilder()
//                .url(url)
//                .addParams("pid", programEmnu.toString())
//                .addParams("op", "getlistinfo")
//                .addParams("pn", "0")
//                .addParams("rn", "2")   //只获取最近两条
//                .addParams("encode", CHARSET_NAME)
//                .addParams("keyset", "pl2012")
//                .addParams("indentity", "kuwo")
//                .addParams("callback", "json")
//                .addParams("r", String.valueOf(System.currentTimeMillis()));
        return builder;
    }

}
