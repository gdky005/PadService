package com.gdky005.padservice.dao.bean;

/**
 * Created by WangQing on 16/2/29.
 */
public class KuwoDataBean {


    /**
     * url : http://other.web.rh01.sycdn.kuwo.cn/b416ea84bdb4c3eb28ffe561bd1f1afc/56d408fd/resource/n3/69/22/26734815.mp3
     * isok : 1
     */

    private String url;
    private String isok;
    private String programId;
    private String mid;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setIsok(String isok) {
        this.isok = isok;
    }

    public String getUrl() {
        return url;
    }

    public String getIsok() {
        return isok;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    @Override
    public String toString() {
        return "{" +
                "isok:'" + isok + '\'' +
                ", url:'" + url + '\'' +
                ", programId:'" + programId + '\'' +
                ", mid:'" + mid + '\'' +
                '}';
    }
}
