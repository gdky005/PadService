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
    KWYYTP(1013437783),
    /**
     * 莫萱日记
     */
    MXRJ(1013437787),
    /**
     * 吐小槽扒新闻
     */
    TXCBXW(1013437785);

    private int value;

    KuwoProgramEmnu(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
