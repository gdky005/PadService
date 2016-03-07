/**
 * Copyright:   Copyright (c) 2012
 * Company:     北京车语文化传媒有限公司
 * Department:  数字媒体部
 */
package com.kaolafm.mediaplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;

import com.gdky005.padservice.utils.KL;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @author <a href="mailto:xiajl@autoradio.cn">夏俊岭</a>
 * @version 2.0.0 2013
 * @since 2.1
 */
public class VLCMediaPlayService extends Service implements
        VLCBusinessConstent, AbsMediaPlayer.OnErrorListener,
        AbsMediaPlayer.OnPausedCompletedListener,
        AbsMediaPlayer.OnCompletedListener,
        AbsMediaPlayer.OnPrepareCompleteListener,
        AbsMediaPlayer.OnProgressUpdateListener,
        AbsMediaPlayer.OnStoppedCompleteListener,
        AbsMediaPlayer.OnBufferingListener,
        AbsMediaPlayer.OnSeekCompleteListener,
        AbsMediaPlayer.OnPlaybackStartListener,
        AbsMediaPlayer.OnDownloadProgressListener,
        AbsMediaPlayer.OnLoadFileFromLocalListener,
        AbsMediaPlayer.OnBufferingStatusListener {
    
    private final IncomingHandler mIncomingHandler = new IncomingHandler();
    final Messenger mMessenger = new Messenger(mIncomingHandler);
    // 处理延迟释放handler
    // 此应用里使用播放器的类型
    VLCBusinessConstent.MediaPlayType mediaType = MediaPlayType.VLCMediaPlay;
    // 媒体音量控制
    AudioManager audio = null;
    MediaStatus mediaStatus = MediaStatus.Stopped;
    MediaStatus oldMediaStatus = MediaStatus.Stopped;
    WifiLock mWifiLock;
    private AbsMediaPlayer mFirstPlayer;
    private AbsMediaPlayer mSecondPlayer;
    private AbsMediaPlayer mMp3Player;
    private AbsMediaPlayer mIjkMediaPlayer;
    private boolean mIsPlaying = false;
    private Messenger clientMessenger = null;
    private static final String ALARMSTATU = "1";
    /**
     * 播放器创建业务
     */

    // 当前播放的URL
    private String currentURL = null;
    // 当前碎片的总时长
    private int duration = 0;
    // 当前碎片的播放进度
    private long position = 0;
    /**
     * 当前播放的碎片是不是直播节目 true为是，false为否
     */
    private boolean isLiving;
    private AbsMediaPlayer mPlayer = null;

    /**
     * 获取音频焦点动作
     */
    public static final String REQUEST_AUDIOFOCUS_ACTION = "com.kaolafm.mediaplayer.requestAudioFocus";
    /**
     * 移除音频焦点动作
     */
//    public static final String ABANDON_AUDIOFOCUS_ACTION = "com.kaolafm.mediaplayer.abandonAudioFocus";

    private BroadcastReceiver mHeadsetReceiver = new BroadcastReceiver() {
        private static final String STATE = "state";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                if (intent.hasExtra(STATE)) {
                    if (intent.getIntExtra(STATE, 0) == 0) {
                        if (mPlayer != null && mediaStatus == MediaStatus.Playing) {
                            if (isLiving) {
                                reset();
                            } else {
                                pause();
                            }
                        } else {//LivePlayerMgr has independent registed boradcst reciver to
                            // handle headset plug
//                            LivePlayerManager livePlayerManager = LivePlayerManager.getInstance(context);
//                            livePlayerManager.manageHeadsetPlug();
                        }
                    }
                }
            } else if (REQUEST_AUDIOFOCUS_ACTION.equals(action)) {
//                abandonAudioFocus();
                requestAudioFocus();
            }
//            else if (ABANDON_AUDIOFOCUS_ACTION.equals(action)) {
//                abandonAudioFocus();
//            }
        }
    };
    /**
     * 电源管理
     */
    // private PowerManager.WakeLock wakeLock = null;
    private int oldVolume;
    private boolean mCanPlay = true;
    private boolean comefromlossfocus = false;
    private OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            KL.i("new audio focus {}, oldVolume = {}, comefromlossfocus = {}, oldMediaStatus = {}",
                    focusChange, oldVolume, comefromlossfocus, oldMediaStatus);
            Context context = getApplicationContext();
//            if (LivePlayerManager.getInstance(context).isShowLivePlayerEnabled()) {
//                LivePlayerManager.getInstance(context).manageAudioFocusChange(focusChange);
//                return;
//            }

//            if (LivePlayerManager.getInstance(context).isShowLivePlayerEnabled()) {
//                return;
//            }

            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                if (mediaStatus == MediaStatus.Playing) {
                    oldMediaStatus = MediaStatus.Playing;
                    comefromlossfocus = true;
                    mCanPlay = true;
                    oldVolume = audio
                            .getStreamVolume(AudioManager.STREAM_MUSIC);
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC,
                            oldVolume / 2, 0);
                } else {
                    oldMediaStatus = mediaStatus;
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (comefromlossfocus) {
                    comefromlossfocus = false;
                    mCanPlay = true;
                    if (oldMediaStatus == MediaStatus.Playing) {
                        mIsPlaying = true;
                        if (isLiving) {
                            startNewPlay();
                        } else {
                            play();
                        }
//                        PlayerManager.getInstance(context).play();
                        currentURL = mPlayer.getUrl();
                        Bundle bundle = new Bundle();
                        bundle.putString(KEY_THEURL, currentURL);
                        Message message = Message.obtain(null,
                                SERVICE_ACTION_PLAYING, bundle);
                        sendMsgToClient(message);
                        if (oldVolume != 0) {
                            audio.setStreamVolume(AudioManager.STREAM_MUSIC,
                                    oldVolume, 0);
                        }
                    }
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                if (mediaStatus == MediaStatus.Playing) {
                    oldMediaStatus = MediaStatus.Playing;
                    comefromlossfocus = true;
                    mCanPlay = false;
                    oldVolume = 0;
                    if (isLiving) {
                        reset();
                    } else {
                        pause();
                    }
//                    PlayerManager.getInstance(context).pause();
//                    Bundle bundle = new Bundle();
//                    bundle.putString(KEY_THEURL, currentURL);
//                    Message message = Message.obtain(null,
//                            SERVICE_ACTION_PAUSED, bundle);
//                    sendMsgToClient(message);
                } else {
                    oldMediaStatus = mediaStatus;
                }
            }
        }
    };


    private void reset() {
        mIsPlaying = false;
        try {
            mPlayer.reset();
        } catch (IllegalStateException ill) {
            ill.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        KL.i(">>>> VLCservice onBind ");
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        KL.i(">>>> VLCservice onCreate \r\n>>>> VLCservice first mediaplay Create ");
        setWakeMode();
        // Create the Wifi lock (this does not acquire the lock, this just
        // creates it)
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        if (null != mWifiLock && !mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
//        LivePlayerManager.getInstance(this).removeRadioLive();
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        createMediaPlayer();
        IntentFilter intentFilter = new IntentFilter(
                Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(REQUEST_AUDIOFOCUS_ACTION);
//        intentFilter.addAction(ABANDON_AUDIOFOCUS_ACTION);
        registerReceiver(mHeadsetReceiver, intentFilter);
        super.onCreate();
    }

    private void createMediaPlayer() {
        mFirstPlayer = new KaolaMediaPlayer(this);
        mSecondPlayer = new KaolaMediaPlayer(this);
        mMp3Player = new AndroidMediaPlayer(this);
//        mIjkMediaPlayer = LivePlayerManager.getInstance(this).getLivePlayerInstance();
        mIjkMediaPlayer = new IjkMediaPlayer();
        mPlayer = mFirstPlayer;
    }

    private void switchPlayer(final String url) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        KL.d("switchPlayer----------->{}", url);
        if (url == null || url.length() == 0) {
            return;
        }
        String url1 = mFirstPlayer.getUrl();
        if (mPlayer != null) {
            setMediaPlayerListener(mPlayer, null);
            try {
                mPlayer.reset();
            } catch (IllegalStateException ill) {
                ill.printStackTrace();
            }
        }
        int index = url.lastIndexOf('.');
        String urlSuffixStr = null;
        if (index > 0) {
            urlSuffixStr = url.substring(index, url.length());
        }
        KL.d("switchPlayer = {}", urlSuffixStr);
        if (urlSuffixStr != null) {
            urlSuffixStr = urlSuffixStr.toLowerCase();
            if (urlSuffixStr.endsWith(".mp3")) {
                mPlayer = mMp3Player;
            } else if (urlSuffixStr.endsWith(".m3u8") || urlSuffixStr.endsWith(".aac")) {
                mPlayer = mIjkMediaPlayer;
            } else {
                if (url.equals(url1)) {
                    mPlayer = mSecondPlayer;
                } else {
                    mPlayer = mFirstPlayer;
                }
            }
        }
        setMediaPlayerListener(mPlayer, VLCMediaPlayService.this);
        try {
            mIjkMediaPlayer.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mPlayer.reset();
//            if (!(mPlayer instanceof IjkMediaPlayer)) {
//            }
            mPlayer.setDataSource(url);
            mPlayer.prepare();
            mCanPlay = true;
        } catch (Exception e) {
            mCanPlay = false;
            KL.e("init player error = {}", e.toString());
            onError(mPlayer, -1, -1);
        }
//            }
//        }).start();
    }

    /**
     * 主动获取音频焦点
     */
    private void requestAudioFocus() {
        if (audio != null) {
            audio.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    /**
     * 主动放弃音频焦点
     */
//    private void abandonAudioFocus() {
//        if (audio != null) {
//            audio.abandonAudioFocus(afChangeListener);
//        }
//    }

    /**
     * 播放器监听监听
     */

    private void setMediaPlayerListener(AbsMediaPlayer player,
                                        VLCMediaPlayService service) {
        if (player != null) {
            player.setErrorListener(service);
            player.setPausedCompleteListener(service);
            player.setPlayCompleteListener(service);
            player.setPrepareCompleteListener(service);
            player.setProgressUpdateListener(service);
            player.setStoppedCompleteListener(service);
            player.setBufferingListener(service);
            player.setSeekCompleteListener(service);
            player.setPlaybackStartListener(service);
            player.setDownloadProgressListener(service);
            player.setLoadFileFromLocalListener(service);
            player.setOnBufferingStatusListener(service);
        }
    }

    @Override
    public void onDestroy() {
        unSetWakeMode();
        if (mWifiLock != null && mWifiLock.isHeld()) {
            KL.i("Wifi", "--------release-------------------");
            mWifiLock.release();
        }
        mWifiLock = null;
        stopSelf();
        unregisterReceiver(mHeadsetReceiver);
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
        KL.i(">>>> VLCservice onDestroy ");
    }

    @Override
    public void onRebind(Intent intent) {
        //
        KL.i(">>>> VLCservice onRebind ");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //
        KL.i(">>>> VLCservice onUnbind ");
        clientMessenger = null;
        return super.onUnbind(intent);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //
        KL.i(">>>> VLCservice onStartCommand ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
    }

    public void sendMsgToClient(Message message) {
        // LogUtil.Log(TAG, "sendMsgToClient");
        if (clientMessenger == null)
            return;
        // LogUtil.Log(TAG, "clientMessenger="+clientMessenger);
        try {
            clientMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendBroadcastToClient(Intent intent) {
        sendBroadcast(intent);
    }

    // 初始化
    private void initMediaPlayParam() {
        duration = 0;
        position = 0;
    }

    private void setWakeMode() {
        // if (wakeLock == null) {
        // Context context = getApplicationContext();
        // pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        // wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context
        // .getClass().getCanonicalName());
        // wakeLock.acquire();
        // LogUtil.Log(TAG, "setWakeMode()=====");
        // }
    }

    private void unSetWakeMode() {
        // if (wakeLock != null && wakeLock.isHeld()) {
        // wakeLock.release();
        // wakeLock = null;
        // LogUtil.Log(TAG, "release()=========WakeMode(release)=====");
        // }

    }

    /**
     * ------------------------------网络状态监听------------------------------------
     */

    private void play() {
        KL.i("Play is called, can play :{}, mIsPlaying = {}", mCanPlay, mIsPlaying);
        if (mCanPlay && mIsPlaying) {
            try {
                requestAudioFocus();
                mediaStatus = MediaStatus.Playing;
                mPlayer.play();
            } catch (Exception e) {
                KL.e("Player play error.", e);
            }
        }
    }

    private void pause() {
        mIsPlaying = false;
        mPlayer.pause();
    }

    @Override
    public void onStopped(AbsMediaPlayer player) {
        Message msg = Message.obtain();
        msg.what = SERVICE_ACTION_STOPED;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_THEURL, player.getUrl());
        msg.obj = bundle;
        sendMsgToClient(msg);
    }

    @Override
    public void onProgress(AbsMediaPlayer player, long ms, long dur) {
        position = ms;
//        KL.d(PlayerService.class, "onProgress----------->ms = {}", ms);
        mediaStatus = MediaStatus.Playing;
//        Intent intent = new Intent(ACTION_PLAYBACK_UPDATED);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_THEURL, player.getUrl());
        if (dur > 0 && duration != dur) {
            duration = (int) dur;
        }

        bundle.putInt(KEY_PROGRESSUPDATE_DURATION_I, duration);
        bundle.putInt(KEY_PROGRESSUPDATE_TIME_I, (int) position);
        bundle.putBoolean(KEY_IS_PRE_DOWNLOAD_COMPLETION, mPlayer.isPreDownloadComplete());
//        intent.putExtras(bundle);
//        sendBroadcastToClient(intent);
        Message msg = Message.obtain();
        msg.what = SERVICE_ACTION_PROGRESS;
        msg.obj = bundle;
        sendMsgToClient(msg);
    }


    @Override
    public void onPrepareComplete(AbsMediaPlayer player) {
        if (position != 0) {
            mPlayer.seekTo(position);
        } else {
            play();
        }
        // TODO Delete it if don't care about buffer ready.
        // Message msg = Message.obtain();
        // msg.what = SERVICE_ACTION_BUFFER_READY;
        // Bundle bundle = new Bundle();
        // bundle.putString(KEY_THEURL, player.getUrl());
        // msg.o bj = bundle;
        // sendMsgToClient(msg);
    }

    @Override
    public void onCompleted(AbsMediaPlayer player) {
        Message msg = Message.obtain();
        msg.what = SERVICE_ACTION_COMPLETED;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_THEURL, player.getUrl());
        bundle.putFloat(KEY_COMPLETION_LENGTH_F, duration);
        bundle.putInt(KEY_COMPLETION_PERCENT_I, (int) position);
        msg.obj = bundle;
        sendMsgToClient(msg);
    }

    @Override
    public void onPaused(AbsMediaPlayer player) {
        KL.d("play m3u8 onPaused start");
        Message msg = Message.obtain();
        msg.what = SERVICE_ACTION_PAUSED;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_THEURL, player.getUrl());
        msg.obj = bundle;
        mediaStatus = MediaStatus.Paused;
        sendMsgToClient(msg);
    }

    @Override
    public boolean onError(AbsMediaPlayer player, int what, int extra) {
        Message msg = Message.obtain();
        msg.what = SERVICE_ACTION_ERRORINFO;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_THEURL, player.getUrl());
        msg.obj = bundle;
        msg.arg1 = what;
        msg.arg2 = extra;
        sendMsgToClient(msg);
        return true;
    }

    @Override
    public void onBuffering(AbsMediaPlayer player) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_THEURL, player.getUrl());
        bundle.putInt(KEY_DURATION, duration);
        bundle.putInt(KEY_POSITION_I, (int) position);
        Message msg = Message.obtain(null, SERVICE_ACTION_BUFFERING, bundle);
        sendMsgToClient(msg);
    }

    @Override
    public void onSeekComplete(AbsMediaPlayer player) {
        // TODO Delete it if don't care about seek ready or pause.
        // if (mIsPlaying) {
        play();
        // } else {
        // pause();
        // }
        Message msg = Message.obtain();
        msg.what = SERVICE_ACTION_SEEK_READY;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_THEURL, player.getUrl());
        msg.obj = bundle;
        sendMsgToClient(msg);
    }

    @Override
    public void onPlaybackStart(AbsMediaPlayer player, long startPosition) {
        Message msg = Message.obtain();
        msg.what = SERVICE_ACTION_PLAYING;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_THEURL, mPlayer.getUrl());
        msg.arg1 = (int) startPosition;
        msg.obj = bundle;
        sendMsgToClient(msg);
    }

    @Override
    public void onBufferingtStart(AbsMediaPlayer player) {
        Message msg = Message.obtain();
        msg.what = SERVICE_ACTION_BUFFERING_START;
        sendMsgToClient(msg);
    }

    @Override
    public void onBufferingEnd(AbsMediaPlayer player) {
        Message msg = Message.obtain();
        msg.what = SERVICE_ACTION_BUFFERING_END;
        sendMsgToClient(msg);
    }

    enum MediaStatus {
        Playing, Paused, Stopped
    }

    class IncomingHandler extends Handler {

        /*
         * (non-Javadoc)
         *
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle;
            switch (msg.what) {
                case CLIENT_ACTION_BIND: {
                    clientMessenger = msg.replyTo;
                    requestAudioFocus();

                    KL.i("CLIENT_ACTION_BIND_clientMessenger={}",
                            clientMessenger);
                }
                break;
                case CLIENT_ACTION_UNBIND: {
                    clientMessenger = null;
                    KL.i("CLIENT_ACTION_UNBIND_clientMessenger={}",
                            clientMessenger);
                }
                break;
                case CLIENT_ACTION_THEURL:
                    initMediaPlayParam();
                    bundle = (Bundle) msg.obj;
                    if (bundle != null) {
                        String url = bundle.getString(KEY_THEURL);
                        duration = (int) bundle.getLong(KEY_DURATION);
                        position = bundle.getInt(KEY_POSITION_I);
                        isLiving = bundle.getBoolean(KEY_ISLIVING);
                        mIsPlaying = true;
                        KL.i("CLIENT_ACTION_THEURL url = {} , position: {}",
                                url, position);
                        if (url != null && !TextUtils.isEmpty(url)) {
                            switchPlayer(url);
                        } else {
                            sendMsgToClient(Message.obtain(this,
                                    SERVICE_MEDIAPLAY_ERROR_URLISNULL));
                        }
                    }
                    break;
                case CLIENT_ACTION_PLAY: {
                    KL.i("CLIENT_ACTION_PLAY=");
                    if (audio != null) {
//                        audio.requestAudioFocus(afChangeListener,
//                                AudioManager.STREAM_MUSIC,
//                                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
                        requestAudioFocus();
                        mCanPlay = true;
                    }
                    mIsPlaying = true;
                    play();
                    Message msg1 = Message.obtain();
                    msg1.what = SERVICE_ACTION_PLAYING;
                    bundle = new Bundle();
                    bundle.putString(KEY_THEURL, mPlayer.getUrl());
                    msg1.obj = bundle;
                    sendMsgToClient(msg1);
                    break;
                }
                // Seek
                case CLIENT_ACTION_SEEK:
                    KL.i("CLIENT_ACTION_SEEK");
                    long msec = msg.arg1;
                    mPlayer.seekTo(msec);
                    break;
                case CLIENT_ACTION_PAUSE:
                    KL.i("CLIENT_ACTION_PAUSE=");
                    mIsPlaying = false;
                    mPlayer.pause();
                    break;
                case CLIENT_ACTION_STOP:
                    KL.i("CLIENT_ACTION_STOP=");
                    mIsPlaying = false;
                    mPlayer.stop();
                    break;
                case CLIENT_ACTION_RESET:
                    KL.i("CLIENT_ACTION_RESET=");
                    mIsPlaying = false;
                    try {
                        mPlayer.reset();
                    } catch (IllegalStateException ill) {
                        ill.printStackTrace();
                    }
                    break;
                case CLIENT_ACTION_DESTROY:
                    KL.i("CLIENT_ACTION_DESTROY=");
                    mIsPlaying = false;
                    mPlayer.stop();
                    break;
                case CLIENT_ACTION_KILLSELF: {
                    KL.i("CLIENT_ACTION_KILLSELF=");
//                    abandonAudioFocus();
                    stopSelf();
                    releaseAllPlayer();
                }
                break;
                case CLIENT_ACTION_BACKGROUND: {
                    KL.i("CLIENT_ACTION_BACKGROUND");
                }
                break;
                case CLIENT_ACTION_FOREGROUND:
                    KL.i("CLIENT_ACTION_FOREGROUND");
                    break;
                case CLIENT_ACTION_PRELOAD_THEURL:
                    bundle = (Bundle) msg.obj;
                    String urlString = bundle.getString(KEY_THEPRELOADURL);
                    if (urlString != null && !TextUtils.isEmpty(urlString)) {
                        KL.i("CLIENT_ACTION_PRELOAD_THEURL = {}", urlString);
                        mPlayer.preload(urlString);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void releaseAllPlayer() {

        mPlayer.releaseListeners();
        mPlayer.release();

        if (mIjkMediaPlayer != null && mIjkMediaPlayer != mPlayer) {
            mIjkMediaPlayer.releaseListeners();
            mIjkMediaPlayer.release();
        }
        if (mMp3Player != null && mMp3Player != mPlayer) {
            mMp3Player.releaseListeners();
            mMp3Player.release();
        }
        if (mFirstPlayer != null && mFirstPlayer != mPlayer) {
            mFirstPlayer.releaseListeners();
            mFirstPlayer.release();
        }
        if (mSecondPlayer != null && mSecondPlayer != mPlayer) {
            mSecondPlayer.releaseListeners();
            mSecondPlayer.release();
        }
    }

    private void startNewPlay() {
        if (mPlayer == null) {
            return;
        }
        initMediaPlayParam();
        mIsPlaying = true;
        String url = mPlayer.getUrl();
        if (url != null && !TextUtils.isEmpty(url)) {
            switchPlayer(url);
        } else {
            sendMsgToClient(Message.obtain(mIncomingHandler,
                    SERVICE_MEDIAPLAY_ERROR_URLISNULL));
        }
    }

    @Override
    public void onDownloadProgress(long downloadSize, long totalSize) {
        // TODO Auto-generated method stub
        if (VLCMediaPlayClient.getInstance().canSendDownloadProgressMsg()) {
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_DOWNLOAD_SIZE, downloadSize);
            bundle.putLong(KEY_DOWNLOAD_TOTAL_SIZE, totalSize);
            Message msg = Message.obtain(null, SERVICE_ACTION_DOWNLOADING,
                    bundle);
            sendMsgToClient(msg);
        }
    }

    @Override
    public void onLoadFileFromLocal(boolean bLocal) {
        // TODO Auto-generated method stub
        if (VLCMediaPlayClient.getInstance().canSendLoadFileFromLocalMsg()) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(KEY_LOAD_LOCAL_FILE, bLocal);
            Message msg = Message.obtain(null, SERVICE_ACTION_LOAD_LOCALFILE,
                    bundle);
            sendMsgToClient(msg);
        }
    }


}
