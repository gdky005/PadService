package com.gdky005.padservice.utils;

import android.support.annotation.NonNull;

import com.orhanobut.logger.Logger;

/**
 * Log日志输出， 子类是 KL
 * <p/>
 * <p/>
 * 1.多彩格式的日志，提供关键信息；
 * <p/>
 * 2.和slf4j  相同格式，统一管理；
 * <p/>
 * 3.抽离简化日志功能。
 * <p/>
 * Created by WangQing on 15/5/22.
 */
public class L {

    private static final String TAG = "TAG";

    public static void i(String messageForI, Object... args) {
        Logger.i(handleDebugLogFormat(messageForI), args);
    }


    public static void d(String messageForD, Object... args) {
        Logger.d(handleDebugLogFormat(messageForD), args);
    }

    public static void w(String messageForW, Object... args) {
        Logger.w(handleDebugLogFormat(messageForW), args);
    }

    public static void e(String messageForE, Object... args) {
        Logger.e(handleDebugLogFormat(messageForE), args);
    }

    @NonNull
    private static String verifyClass(Class classObj) {
        String className = TAG;

        if (classObj != null) {
            className = classObj.getSimpleName();
        }
        return className;
    }

    /**
     * 处理Debug模式下的打印log格式
     *
     * @param message
     * @return
     */
    private static String handleDebugLogFormat(String message) {
        return message.replace("{}", "%s");
    }

    /**
     * debug 模式下，会打印json 数据
     *
     * @param messageForE
     */
    public static void json(String messageForE) {
        Logger.json(messageForE);
    }
}
