package com.gdky005.padservice;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.UiThreadTest;

import com.gdky005.padservice.dao.KuwoDao;
import com.gdky005.padservice.dao.bean.KuwoDataBean;
import com.gdky005.padservice.dao.bean.KuwoProgramBean;
import com.gdky005.padservice.dao.callback.ProgramListCallback;
import com.gdky005.padservice.dao.callback.ProgramMusicDataCallback;

import okhttp3.Call;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @UiThreadTest
    public void testGetProgramData() {
        KuwoDao kuwoDao = new KuwoDao();


        kuwoDao.getProgramData("6941263", new ProgramMusicDataCallback() {
            @Override
            public void onError(Call call, Exception e) {
                System.out.println("发生错误了：" + e.getMessage());
            }

            @Override
            public void onResponse(KuwoDataBean response) {
                System.out.println("获取的音频是：" + response.getUrl());
            }
        });
    }

    @UiThreadTest
    public void testgetTXCBXWList() {
        KuwoDao kuwoDao = new KuwoDao();

        kuwoDao.getTXCBXWList(new ProgramListCallback() {
            @Override
            public void onError(Call call, Exception e) {
                System.out.println("发生错误了：" + e.getMessage());
            }

            @Override
            public void onResponse(KuwoProgramBean response) {
                System.out.println("获取的音频是：" + response.getMusiclist().get(0).getName());
            }
        });
    }

    @UiThreadTest
    public void testgetKWYYTPList() {
        KuwoDao kuwoDao = new KuwoDao();

        kuwoDao.getKWYYTPList(new ProgramListCallback() {
            @Override
            public void onError(Call call, Exception e) {
                System.out.println("发生错误了：" + e.getMessage());
            }

            @Override
            public void onResponse(KuwoProgramBean response) {
                System.out.println("获取的音频是：" + response.getMusiclist().get(0).getName());
            }
        });
    }

    @UiThreadTest
    public void testgetMXRJList() {
        KuwoDao kuwoDao = new KuwoDao();

        kuwoDao.getMXRJList(new ProgramListCallback() {
            @Override
            public void onError(Call call, Exception e) {
                System.out.println("发生错误了：" + e.getMessage());
            }

            @Override
            public void onResponse(KuwoProgramBean response) {
                System.out.println("获取的音频是：" + response.getMusiclist().get(0).getName());
            }
        });
    }
}