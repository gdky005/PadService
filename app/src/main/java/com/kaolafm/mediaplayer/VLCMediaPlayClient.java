/**
 * Copyright:   Copyright (c) 2012
 * Company:     北京车语文化传媒有限公司
 * Department:  数字媒体部
 */
package com.kaolafm.mediaplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;

import com.gdky005.padservice.utils.KL;

/**
 * 播放器客户端类 注意： 如使用系统播放器，只支持单播功能。
 *
 * @author <a href="mailto:xiajl@autoradio.cn">夏俊岭</a>
 * @version 2.0.0 2013
 * @since 2.1
 */
public class VLCMediaPlayClient implements VLCBusinessConstent {

    private static VLCMediaPlayClient uniqueInstance = null;
    // private String TAG = "VLCMediaPlayClient-xjl";
    // Service绑定是否用户绑定，如果属于系统自动重建绑定则会触发重新加载崩溃URL1次，再崩溃则认为URL不可用，则请求下一首数据
    private boolean isFirst = false;
    private int isFirstCount = 0;
    private long duration;
    /**
     * 是否是直播url true为是，false为否
     */
    private boolean isLiving;
    /**
     * When sendPlayURLToService(), record current file size.
     */
    private long mLastActualFilseSize;
    // ---------------------接收Service消息方法群-----------------------------------
    // 是否绑定Service
    private boolean mIsbound = false;
    // 向服务端发送
    private Messenger sendMsgToService = null;
    // 接受服务端消息
    private Messenger inComingMessenger = null;
    /**
     *
     */
    private String theURL = null;
    private String title = null;
    private int position = 0;
    private OnProgressUpdateListener mOnProgressUpdateListener = null;
    private OnBufferListener mOnBufferListener = null;

    public boolean isBound() {
        return mIsbound;
    }

//    private BroadcastReceiver mControlReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
    // TODO Auto-generated method stub
//            String action = intent.getAction();
//			if (ACTION_PLAYBACK_BUFFER.equals(action)) {
//
//				if (null != mOnBufferListener) {
//					Bundle serviceBundle = intent.getExtras();
//					String urlString = serviceBundle.getString(KEY_THEURL);
//					int buffer = serviceBundle.getInt(KEY_BUFFER_PERCENT_I);
//					if (buffer > 0)
//						mOnBufferListener
//							.onBufferListener(urlString, buffer, 0);
//
//				}
//			} else
//            if (ACTION_PLAYBACK_UPDATED.equals(action)) {
//
//                if (null != mOnProgressUpdateListener) {
//                    Bundle serviceBundle = intent.getExtras();
//                    String urlString = serviceBundle.getString(KEY_THEURL);
//                    int position1 = serviceBundle
//                            .getInt(KEY_PROGRESSUPDATE_TIME_I);
//                    int duration = serviceBundle
//                            .getInt(KEY_PROGRESSUPDATE_DURATION_I);
//                    if (theURL != null && urlString != null
//                            && urlString.equals(theURL)) {
//                        if (position1 > position) {
//                            position = position1;
//                        }
//                        mOnProgressUpdateListener.onProgressUpdateListener(
//                                urlString, position1, duration);
//                    }
//                }
//            }

    //        }
//    };
    private OnCompletionListener mOnCompletionListener = null;
    private OnErrorListener mOnErrorListener = null;
    private OnPausedCompleteListener mOnPausedCompleteListener = null;
    private OnPlayingListener mOnPlayingListener = null;
    private OnBufferReadyListener mOnBufferReadyListener = null;
    private OnSeekReadyListener mOnSeekReadyListener = null;
    private OnStoppedListener mOnStoppedListener = null;
    private OnReconnectListener mOnReconnectListener = null;
    private OnDownloadProgressListener mOnDownloadProgressListener = null;
    private OnLoadFileFromLocalListener mOnLoadFileFromLocalListener = null;
    private OnBufferingStatusListener mOnBufferingStatusListener = null;

    private Context context;
    private boolean isPlaying = false;
    private Handler client = new Handler() {
        /*
         * (non-Javadoc)
         *
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                case SERVICE_ACTION_PAUSED: {
                    isPlaying = false;
                    String urlString = null;
                    Object obj = msg.obj;
                    if (null != obj) {
                        Bundle serviceBundle = (Bundle) msg.obj;
                        urlString = serviceBundle.getString(KEY_THEURL);
                    }
                    KL.i("client _SERVICE_ACTION_PAUSED=theURL:");
                    if (null != mOnPausedCompleteListener) {
                        mOnPausedCompleteListener
                                .onPausedCompleteListener(urlString);
                    }
                }
                break;
                case SERVICE_ACTION_STOPED: {
                    isPlaying = false;
                    String urlString = null;
                    Object obj = msg.obj;
                    if (null != obj) {
                        Bundle serviceBundle = (Bundle) msg.obj;
                        urlString = serviceBundle.getString(KEY_THEURL);
                    }
                    if (null != mOnStoppedListener) {
                        mOnStoppedListener.onStoppedListener(urlString);
                    }
                }
                break;
                case SERVICE_ACTION_PLAYING: {
                    isPlaying = true;
                    String urlString = null;
                    Object obj = msg.obj;
                    if (null != obj) {
                        Bundle serviceBundle = (Bundle) msg.obj;
                        urlString = serviceBundle.getString(KEY_THEURL);
                    }
                    if (null != mOnPlayingListener) {
                        mOnPlayingListener.onPlayingListener(urlString, msg.arg1);
                    }
                }
                break;
                case SERVICE_ACTION_DISTORIED: {

                }
                break;
                case SERVICE_ACTION_ERRORINFO: {
                    String urlString = null;
                    Object obj = msg.obj;
                    if (null != obj) {
                        Bundle serviceBundle = (Bundle) msg.obj;
                        urlString = serviceBundle.getString(KEY_THEURL);
                    }
                    if (null != mOnPausedCompleteListener) {
                        mOnPausedCompleteListener
                                .onPausedCompleteListener(urlString);
                    }
                    if (null != mOnErrorListener) {
                        mOnErrorListener.onErrorListener(urlString, msg.arg1, msg.arg2);
                    }

                }
                break;
                case SERVICE_MEDIAPLAY_ERROR_URLISNULL:
                    if (null != mOnErrorListener) {
                        mOnErrorListener.onUrlNullErrorListener();
                    }
                    break;
                case SERVICE_ACTION_COMPLETED: {
                    String urlString = null;
                    Object obj = msg.obj;
                    if (null != obj) {
                        Bundle serviceBundle = (Bundle) msg.obj;
                        urlString = serviceBundle.getString(KEY_THEURL);
                        float duration = serviceBundle
                                .getFloat(KEY_COMPLETION_LENGTH_F);
                        int position = serviceBundle
                                .getInt(KEY_COMPLETION_PERCENT_I);
                        if (null != mOnPausedCompleteListener) {
                            mOnPausedCompleteListener
                                    .onPausedCompleteListener(urlString);
                        }
                        if (null != mOnCompletionListener) {
                            KL.i("client_SERVICE_ACTION_COMPLETED=");
                            mOnCompletionListener.onCompletionListener(urlString,
                                    duration, position);

                        }
                    }
                }
                break;
                case SERVICE_ACTION_BUFFERING: {
                    Bundle bundle = (Bundle) msg.obj;
                    if (bundle != null) {
                        String urlString = bundle.getString(KEY_THEURL);
                        if (mOnReconnectListener != null) {
                            mOnReconnectListener.onReconnectListener(urlString,
                                    bundle.getInt(KEY_DURATION),
                                    bundle.getInt(KEY_POSITION_I));
                        }
                    }
                }
                break;
                case SERVICE_ACTION_DOWNLOADING: {
                    Bundle bundle = (Bundle) msg.obj;
                    if (mOnDownloadProgressListener != null) {
                        mOnDownloadProgressListener.onDownloadProgress(
                                bundle.getLong(KEY_DOWNLOAD_SIZE),
                                bundle.getLong(KEY_DOWNLOAD_TOTAL_SIZE));
                    }
                    break;
                }
                case SERVICE_ACTION_LOAD_LOCALFILE: {
                    Bundle bundle = (Bundle) msg.obj;
                    boolean bLocalFile = bundle.getBoolean(KEY_LOAD_LOCAL_FILE);
                    mOnLoadFileFromLocalListener.onLoadFileFromLocal(bLocalFile);
                }
                break;
                case SERVICE_ACTION_BUFFERING_START:
                    if (mOnBufferingStatusListener != null) {
                        mOnBufferingStatusListener.onBufferingtStart();
                    }
                    break;
                case SERVICE_ACTION_BUFFERING_END:
                    if (mOnBufferingStatusListener != null) {
                        mOnBufferingStatusListener.onBufferingEnd();
                    }
                    break;
                case SERVICE_ACTION_BUFFER_READY:
                    Bundle bufferBundle = (Bundle) msg.obj;
                    if (bufferBundle != null) {
                        String urlString = bufferBundle.getString(KEY_THEURL);
                        if (mOnBufferReadyListener != null) {
                            mOnBufferReadyListener.onBufferReadyListener(urlString);
                        }
                    }
                    break;
                case SERVICE_ACTION_SEEK_READY:
                    Bundle seekBundle = (Bundle) msg.obj;
                    if (seekBundle != null) {
                        String urlString = seekBundle.getString(KEY_THEURL);
                        if (mOnSeekReadyListener != null) {
                            mOnSeekReadyListener.onSeekReadyListener(urlString);
                        }
                    }

                    break;
                case SERVICE_ACTION_PROGRESS:
                    if (null != mOnProgressUpdateListener) {
                        Bundle serviceBundle = (Bundle) msg.obj;
                        String urlString = serviceBundle.getString(KEY_THEURL);
                        int position1 = serviceBundle
                                .getInt(KEY_PROGRESSUPDATE_TIME_I);
                        int duration = serviceBundle
                                .getInt(KEY_PROGRESSUPDATE_DURATION_I);
                        boolean isPreDownloadComplete = serviceBundle.getBoolean(KEY_IS_PRE_DOWNLOAD_COMPLETION, false);
                        if (theURL != null && urlString != null
                                && urlString.equals(theURL)) {
                            if (position1 > position) {
                                position = position1;
                            }
                            mOnProgressUpdateListener.onProgressUpdateListener(
                                    urlString, position1, duration, isPreDownloadComplete);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private boolean uiIsBack = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            sendMsgToService = null;
            mIsbound = false;
            isFirst = false;
            bindVLCPlayService(context);
            KL.i("onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            KL.d("Player client onServiceConnected");
            // TODO Auto-generated method stub
            sendMsgToService = new Messenger(service);
            inComingMessenger = new Messenger(client);
            int temp = 0;
            Message message = Message.obtain(null, CLIENT_ACTION_BIND, temp, 0);
            try {
                message.replyTo = inComingMessenger;
                sendMsgToService.send(message);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            KL.d("onServiceConnected");
            mIsbound = true;
            // 容错机制： Service自己崩溃重连时，自动重新发送正在播放的URL
            if (!isFirst) {
                KL.i("auto onServiceConnected");
                if (isFirstCount > 0) {
                    KL.i("auto onServiceConnected -notify UI request new the url");
                    isFirstCount = 0;
                    if (null != mOnCompletionListener) {
                        mOnCompletionListener
                                .onCompletionListener(theURL, 0, 0);
                    }
                } else {
                    KL.i("auto onServiceConnected -retry");
                    isFirstCount = isFirstCount + 1;
                    clientActionHandler
                            .sendEmptyMessage(CLIENT_ACTION_PREPARE_SETTHEURL);
                }

            }
            setBackGround(uiIsBack);
        }
    };
    private String mFilePath;

    public synchronized static VLCMediaPlayClient getInstance() {

        if (uniqueInstance == null) {

            uniqueInstance = new VLCMediaPlayClient();
        }
        return uniqueInstance;
    }

    public void setOnProgressUpdateListener(OnProgressUpdateListener listener) {
        mOnProgressUpdateListener = listener;
    }

    public void setOnBufferListener(OnBufferListener listener) {
        mOnBufferListener = listener;
    }

    // -------------向VLC MediaPlayService 发送消息方法群--------------------------

    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    public void setOnPausedCompleteListener(OnPausedCompleteListener listener) {
        mOnPausedCompleteListener = listener;
    }

    public void setOnPlayingListener(OnPlayingListener listener) {
        mOnPlayingListener = listener;
    }

    public void setOnBufferReadyListener(OnBufferReadyListener listener) {
        mOnBufferReadyListener = listener;
    }

    public void setOnSeekReadyListener(OnSeekReadyListener listener) {
        mOnSeekReadyListener = listener;
    }

    public void setOnProloadErrorListener(OnProloadErrorListener listener) {
    }

    public void setOnReconnectListener(OnReconnectListener listener) {
        mOnReconnectListener = listener;
    }

    public void setOnDownloadProgressListener(
            OnDownloadProgressListener listener) {
        mOnDownloadProgressListener = listener;
    }

    public boolean canSendDownloadProgressMsg() {
        return mOnDownloadProgressListener != null;
    }

    public void setOnLoadFileFromLocalListener(
            OnLoadFileFromLocalListener listener) {
        mOnLoadFileFromLocalListener = listener;
    }

    public void setOnBufferingStatusListener(OnBufferingStatusListener mOnBufferingStatusListener) {
        this.mOnBufferingStatusListener = mOnBufferingStatusListener;
    }

    public boolean canSendLoadFileFromLocalMsg() {
        return mOnLoadFileFromLocalListener != null;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
//        KL.i("context={}", context);
//        if (context != null) {
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(ACTION_PLAYBACK_BUFFER);
//            filter.addAction(ACTION_PLAYBACK_UPDATED);
//            KL.i("context=mControlReceiver={}", mControlReceiver);
//            context.registerReceiver(mControlReceiver, filter);
//        } else {
//            this.context.unregisterReceiver(mControlReceiver);
//        }
        this.context = context;
    }

    /**
     * 绑定Service时必须指定该应用环境下要使用的播放器类型 即TYPE 1. VLCMediaPlay 可以使用opus,mp3 2.
     * SYSTEMMediaPlay 只支持mp3 ，而且是单播
     *
     * @param context
     */
    public void bindVLCPlayService(Context context) {
        if (context == null)
            return;
        isFirst = true;
        boolean flag = context.bindService(new Intent(context,
                VLCMediaPlayService.class), mConnection, Context.BIND_AUTO_CREATE);
        setContext(context);
        KL.i("bindVLCPlayService={}", flag);
        mIsbound = flag;

    }

    public void unBindVLCPlayService() {
        if (mIsbound && this.context != null) {
            this.context.unbindService(mConnection);
        }
        setContext(null);
        mIsbound = false;
    }

    /**
     * 通知远程播放器是否为后台播放状态。
     *
     * @param isBack
     */
    public synchronized void setBackGround(boolean isBack) {
        uiIsBack = isBack;
        if (sendMsgToService == null)
            return;
        try {
            if (isBack) {
                sendMsgToService.send(Message.obtain(null,
                        CLIENT_ACTION_BACKGROUND));
            } else {
                sendMsgToService.send(Message.obtain(null,
                        CLIENT_ACTION_FOREGROUND));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // --------------------------处理快速切换按钮事件

    /**
     * 向Service 发送立刻播放的URL
     *
     * @param urlString 播放URL
     * @param title     碎片名称
     * @param position  当前播放位置
     * @param duration  播放url总时长
     * @param delayInMs 延迟多久开播
     * @param isLiving  是否为直播 true为是，false为否
     */
    public synchronized void sendPlayURLToService(String urlString,
                                                  String title,
                                                  int position,
                                                  long duration,
                                                  int delayInMs,
                                                  boolean isLiving) {
        KL.d(
                        "VlcMediaPlayClient sendPlayUrlToService() title: {}, position: {}",
                        title, position);
        if (sendMsgToService == null || TextUtils.isEmpty(urlString)) {
            return;
        }
        this.theURL = urlString;
        this.title = title;
        this.position = position;
        this.duration = duration;
        this.isLiving = isLiving;
        clientActionHandler.sendEmptyMessageDelayed(
                CLIENT_ACTION_PREPARE_SETTHEURL, delayInMs);
    }

    /**
     * 向Service 发送预加载的URL
     *
     * @param duration
     */
    public synchronized void sendPlayProloadURLToService(String urlString,
                                                         String title, long duration) {
        if (sendMsgToService == null || TextUtils.isEmpty(urlString))
            return;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_THEPRELOADURL, urlString);
        Message message = Message.obtain(null, CLIENT_ACTION_PRELOAD_THEURL,
                bundle);
        try {
            sendMsgToService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向Service 停止并释放当前播放器。
     */
    public synchronized void sendReleaseActionToService() {
        if (sendMsgToService == null) {
            return;
        }
        position = 0;
        Message message = Message.obtain(null, CLIENT_ACTION_KILLSELF);
        try {
            sendMsgToService.send(message);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } finally {

        }
    }

    /**
     * 向Service 停止并释放当前播放器。
     */
    public synchronized void sendStopAndReleaseActionToService() {
        if (sendMsgToService == null)
            return;
        position = 0;
        Message message = Message.obtain(null, CLIENT_ACTION_DESTROY);
        try {
            sendMsgToService.send(message);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } finally {

        }
    }

    /**
     * 向Service 发送 暂停指令
     *
     * @param duration
     */
    public synchronized void sendPauseActionToService(String urlString,
                                                      String title, long duration) {
        if (sendMsgToService == null) {
            return;
        }
        this.theURL = urlString;
        this.title = title;
        clientActionHandler.sendEmptyMessage(CLIENT_ACTION_PREPARE_PAUSE);

    }

    /**
     * 向Service 发送 重置指令
     *
     * @param urlString
     * @param title
     */
    public synchronized void sendResetActionToService(String urlString, String title) {
        if (sendMsgToService == null) {
            return;
        }
        this.theURL = urlString;
        this.title = title;
        clientActionHandler.sendEmptyMessage(CLIENT_ACTION_PREPARE_RESET);
    }

    /**
     * 向Service 发送播放指令
     *
     * @param duration
     */
    public synchronized void sendPlayActionToService(String urlString,
                                                     String title, long duration) {
        if (sendMsgToService == null)
            return;
        this.theURL = urlString;
        this.title = title;
        clientActionHandler.sendEmptyMessage(CLIENT_ACTION_PREPARE_PLAY);

    }

    Handler clientActionHandler = new Handler() {

        /*
         * (non-Javadoc)
         *
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if (sendMsgToService == null)
                return;
            super.handleMessage(msg);
            if (clientActionHandler == null)
                return;
            switch (msg.what) {
                case CLIENT_ACTION_PREPARE_SETTHEURL: {
                    if (clientActionHandler
                            .hasMessages(CLIENT_SENDACTION_SETTHEURL)) {
                        clientActionHandler
                                .removeMessages(CLIENT_SENDACTION_SETTHEURL);
                    }
                    clientActionHandler
                            .sendEmptyMessage(CLIENT_SENDACTION_SETTHEURL);
                }
                break;
                // Seek
                case CLIENT_SENDACTION_SEEK:
                    Message msgSeek = Message.obtain();
                    msgSeek.what = CLIENT_ACTION_SEEK;
                    msgSeek.arg1 = msg.arg1;
                    try {
                        sendMsgToService.send(msgSeek);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case CLIENT_SENDACTION_SETTHEURL: {
                    KL.i("CLIENT_SENDACTION_SETTHEURL={}, position: {}",
                            title, position);
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_TITLE, title == null ? "" : title);
                    bundle.putString(KEY_THEURL, theURL);
                    bundle.putInt(KEY_POSITION_I, position);
                    bundle.putString(KEY_PATH, mFilePath);
                    bundle.putLong(KEY_DURATION, duration);
                    bundle.putBoolean(KEY_ISLIVING, isLiving);
                    Message message = Message.obtain(null, CLIENT_ACTION_THEURL,
                            bundle);
                    try {
                        sendMsgToService.send(message);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally {
                    }
                }
                break;
                case CLIENT_ACTION_PREPARE_PLAY: {
                    if (clientActionHandler.hasMessages(CLIENT_SENDACTION_PAUSE)) {
                        clientActionHandler.removeMessages(CLIENT_SENDACTION_PAUSE);
                    }
                    if (clientActionHandler.hasMessages(CLIENT_SENDACTION_PLAY)) {
                        clientActionHandler.removeMessages(CLIENT_SENDACTION_PLAY);
                    }

//                    clientActionHandler.sendEmptyMessageDelayed(
//                            CLIENT_SENDACTION_PLAY, 50);
                    clientActionHandler.sendEmptyMessage(CLIENT_SENDACTION_PLAY);
                }
                break;
                case CLIENT_ACTION_PREPARE_PAUSE: {
                    if (clientActionHandler.hasMessages(CLIENT_SENDACTION_PLAY)) {
                        clientActionHandler.removeMessages(CLIENT_SENDACTION_PLAY);
                    }
                    if (clientActionHandler.hasMessages(CLIENT_SENDACTION_PAUSE)) {
                        clientActionHandler.removeMessages(CLIENT_SENDACTION_PAUSE);
                    }
                    clientActionHandler.sendEmptyMessageDelayed(
                            CLIENT_SENDACTION_PAUSE, 50);
                }
                break;
                case CLIENT_ACTION_PREPARE_RESET:
                    if (clientActionHandler.hasMessages(CLIENT_SENDACTION_PLAY)) {
                        clientActionHandler.removeMessages(CLIENT_SENDACTION_PLAY);
                    }
                    if (clientActionHandler.hasMessages(CLIENT_SENDACTION_PAUSE)) {
                        clientActionHandler.removeMessages(CLIENT_SENDACTION_PAUSE);
                    }

                    if (clientActionHandler.hasMessages(CLIENT_SENDACTION_RESET)) {
                        clientActionHandler.removeMessages(CLIENT_SENDACTION_RESET);
                    }

                    clientActionHandler.sendEmptyMessage(
                            CLIENT_SENDACTION_RESET);
                    break;
                case CLIENT_SENDACTION_PLAY: {
                    isPlaying = true;
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_TITLE, title);
                    bundle.putString(KEY_THEURL, theURL);
                    bundle.putString(KEY_PATH, mFilePath);
                    Message message = Message.obtain(null, CLIENT_ACTION_PLAY,
                            bundle);
                    try {
                        sendMsgToService.send(message);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally {
                    }
                }
                break;
                case CLIENT_SENDACTION_PAUSE: {
                    isPlaying = false;
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_TITLE, title);
                    bundle.putString(KEY_THEURL, theURL);
                    bundle.putString(KEY_PATH, mFilePath);
                    Message message = Message.obtain(null, CLIENT_ACTION_PAUSE,
                            bundle);
                    try {
                        sendMsgToService.send(message);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally {
                    }
                }
                break;
                case CLIENT_SENDACTION_RESET:
                    isPlaying = false;
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_TITLE, title);
                    bundle.putString(KEY_THEURL, theURL);
                    bundle.putString(KEY_PATH, mFilePath);
                    Message message = Message.obtain(null, CLIENT_ACTION_RESET,
                            bundle);
                    try {
                        sendMsgToService.send(message);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally {
                    }
                    break;
                case CLIENT_ACTION_REBUILDSERVICE: {
                }
                break;
                default:
                    break;
            }

        }

    };

    /**
     * 向Service 发送停止指令
     */
    public synchronized void sendStopActionToService(String urlString,
                                                     String title, long duration) {
        if (sendMsgToService == null)
            return;
        Bundle bundler = new Bundle();
        bundler.putString(KEY_TITLE, title);
        bundler.putString(KEY_THEURL, urlString);
        bundler.putString(KEY_PATH, mFilePath);
        Message message = Message.obtain(null, CLIENT_ACTION_STOP, bundler);
        try {
            sendMsgToService.send(message);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } finally {

        }
    }

    /**
     * Set player to play when it's ready.
     *
     * @param url
     */
    public synchronized void sendPlay(String url) {
        this.theURL = url;
        clientActionHandler.sendEmptyMessageDelayed(
                CLIENT_SENDACTION_SETTHEURL, 50);
    }

    /**
     * Seek
     *
     * @param progress
     */
    public synchronized void seekTo(int progress) {
        Message msg = clientActionHandler.obtainMessage();
        msg.arg1 = progress;
        msg.what = CLIENT_SENDACTION_SEEK;
        clientActionHandler.sendMessageDelayed(msg, 50);
    }

    public interface OnProgressUpdateListener {
        void onProgressUpdateListener(String theURL, int position,
                                      int duration, boolean isPreDownloadComplete);
    }

    public interface OnBufferListener {
        void onBufferListener(String theURL, int time, int length);
    }

    public interface OnCompletionListener {
        // 播放完成时 播放的时间长度 ，播放的百分比
        void onCompletionListener(String theURL, float duration,
                                  int position);
    }

    public interface OnErrorListener {
        // 目前对底层上抛错误类别不详，以后细化再处理，目前统一为播放器错误。what为错误类型，extra为子错误事件类型，目前主要针对m3u8播放有效
        void onErrorListener(String theURL, int what, int extra);

        void onUrlNullErrorListener();
    }

    public interface OnPlayingListener {
        void onPlayingListener(String theURL, int position);
    }

    public interface OnBufferReadyListener {
        void onBufferReadyListener(String theURL);
    }

    public interface OnSeekReadyListener {
        void onSeekReadyListener(String theURL);
    }

    public interface OnPausedCompleteListener {
        void onPausedCompleteListener(String theURL);
    }

    public interface OnStoppedListener {
        void onStoppedListener(String theURL);
    }

    public interface OnProloadErrorListener {
        void onProloadErrorListener(String theURL);
    }

    public interface OnReconnectListener {
        void onReconnectListener(String theURL, long duration,
                                 long position);
    }

    public interface OnDownloadProgressListener {
        void onDownloadProgress(long downloadSize, long totalSize);
    }

    public interface OnLoadFileFromLocalListener {
        void onLoadFileFromLocal(boolean bLocal);
    }

    public interface OnBufferingStatusListener {
        void onBufferingtStart();

        void onBufferingEnd();
    }

}
