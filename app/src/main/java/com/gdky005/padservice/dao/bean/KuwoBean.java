package com.gdky005.padservice.dao.bean;

/**
 * Created by WangQing on 16/3/8.
 */
public class KuwoBean {

    private String mid;
    private String programId;
    private String url;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "{" +
                "mid:'" + mid + '\'' +
                ", programId:'" + programId + '\'' +
                ", url:'" + url + '\'' +
                '}';
    }
}
