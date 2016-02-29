package com.gdky005.padservice.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

import com.gdky005.padservice.dao.KuwoDao;
import com.gdky005.padservice.utils.L;

import java.util.Map;

/**
 * Created by WangQing on 16/2/29.
 */
public abstract class BaseService extends Service {

    public Map idMaps;
    public KuwoDao kuwoDao;
    public Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        log("onStart");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy");
    }

    @Override
    public boolean stopService(Intent name) {
        log("onUnbind");
        return super.stopService(name);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        log("onUnbind");
        return super.onUnbind(intent);
    }

    public void log(String message) {
        L.i(message);
    }
}
