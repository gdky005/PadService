package com.gdky005.padservice;

import android.test.ActivityInstrumentationTestCase2;

import com.gdky005.padservice.utils.AlarmUtils;
import com.gdky005.padservice.utils.L;

import java.text.SimpleDateFormat;

/**
 * Created by WangQing on 16/3/12.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest(Class<MainActivity> activityClass) {
        super(MainActivity.class);
    }

    public void testAlarm() {
        long time = System.currentTimeMillis();
        SimpleDateFormat formatH = new SimpleDateFormat("HH");
        SimpleDateFormat formatM = new SimpleDateFormat("mm");
        SimpleDateFormat formatS = new SimpleDateFormat("ss");
        int h = Integer.parseInt(formatH.format(time));
        int m = Integer.parseInt(formatM.format(time));
        int s = Integer.parseInt(formatS.format(time));

        if (s == 56) {
            s = 55;
        }

        if (m == 60) {
            m = 0;
        }


//        L.i("当前的时间是：{}时{}分{}秒", h, m, s);
//        s+=5;
        s++;
        L.i("当前的设置闹钟时间是：{}时{}分{}秒", h, m, s);
        AlarmUtils.startAlarm(getInstrumentation().getContext(), 60 * 1000, h, m, s);
    }
}