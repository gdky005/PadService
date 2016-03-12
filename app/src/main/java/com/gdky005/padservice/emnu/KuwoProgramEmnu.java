package com.gdky005.padservice.emnu;

/**
 * 酷我的数据源
 *
 * Created by WangQing on 16/2/29.
 */
public enum KuwoProgramEmnu {

    /**
     * 酷我音乐调频
     */
    KWYYTP(5),
    /**
     * 莫萱日记
     */
    MXRJ(2),
    /**
     * 吐小槽扒新闻
     */
    TXCBXW(1);

    private int value;

    KuwoProgramEmnu(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
