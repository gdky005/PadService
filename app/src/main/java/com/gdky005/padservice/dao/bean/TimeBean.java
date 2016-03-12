package com.gdky005.padservice.dao.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by WangQing on 16/3/12.
 */
public class TimeBean implements Parcelable {

    private long intervalMillis;
    private int hour;
    private int minute;
    private int second;

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }

    public void setIntervalMillis(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.intervalMillis);
        dest.writeInt(this.hour);
        dest.writeInt(this.minute);
        dest.writeInt(this.second);
    }

    public TimeBean() {
    }

    protected TimeBean(Parcel in) {
        this.intervalMillis = in.readLong();
        this.hour = in.readInt();
        this.minute = in.readInt();
        this.second = in.readInt();
    }

    public static final Creator<TimeBean> CREATOR = new Creator<TimeBean>() {
        public TimeBean createFromParcel(Parcel source) {
            return new TimeBean(source);
        }

        public TimeBean[] newArray(int size) {
            return new TimeBean[size];
        }
    };
}
