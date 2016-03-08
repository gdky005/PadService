package com.kaolafm.mediaplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.gdky005.padservice.utils.KL;
import com.kaolafm.mediaplayer.VLCMediaPlayClient.OnDownloadProgressListener;
import com.kaolafm.mediaplayer.VLCMediaPlayClient.OnLoadFileFromLocalListener;

import java.util.ArrayList;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayerService extends Service {
    /**
     * Player state
     */
    public static final int STATE_ON_IDLE = 0;
    public static final int STATE_ON_PREPARING = 1;
    public static final int STATE_ON_PLAYING = 2;
    public static final int STATE_ON_FAILED = 3;
    public static final int STATE_ON_PAUSED = 4;
    public static final int STATE_ON_END = 5;
    private int mState;
    /**
     * Player state listeners
     */
    public ArrayList<IPlayerStateListener> mPlayStateListeners = new ArrayList<>();
    private ArrayList<OnDownloadProgressListener> mOnDownloadProgressListeners = new ArrayList<>();
    private ArrayList<OnLoadFileFromLocalListener> mOnLoadFileFromLocalListeners = new ArrayList<>();
    private IBinder mPlayerBinder = new PlayerBinder();
    /**
     * Broadcast actions define.
     */
    public static final String ACTION_BASE = "com.kaolafm.action";
    public static final String ACTION_START_NEW_PLAY = ACTION_BASE
            + ".START_NEW_PLAY";
    public static final String ACTION_PLAY = ACTION_BASE + ".PLAY";
    public static final String ACTION_PAUSE = ACTION_BASE + ".PAUSE";
    public static final String ACTION_STOP_PLAYER_SERVICE = ACTION_BASE
            + ".STOP_PLAYER_SERVICE";
    private boolean mStartForeground = false;
    private PlayItem mPlayItem;
    public final static String KEY_PLAY_ITEM = "KEY_PLAY_ITEM";

    /**
     * 是否弹出是否需要使用移动网络收听 true为是，false为否
     */
    private boolean bShowMobileNetWarning; // 解决http://redmine.itings.cn/issues/17331

    @Override
    public void onCreate() {
        super.onCreate();
        VLCMediaPlayClient.getInstance().bindVLCPlayService(
                getApplicationContext());
        addMediaPlayerListener();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_START_NEW_PLAY);
        intentFilter.addAction(ACTION_PLAY);
        intentFilter.addAction(ACTION_PAUSE);
        intentFilter.addAction(ACTION_STOP_PLAYER_SERVICE);
        registerReceiver(mPlayReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VLCMediaPlayClient.getInstance().unBindVLCPlayService();
        unregisterReceiver(mPlayReceiver);
        stopForeground(true);
        mStartForeground = false;
    }

    private BroadcastReceiver mPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_START_NEW_PLAY.equals(intent.getAction())) {
                mPlayItem = intent.getParcelableExtra(KEY_PLAY_ITEM);
                startNewPlay(mPlayItem);
            } else if (ACTION_PLAY.equals(intent.getAction())) {
                if (mPlayerBinder != null) {
                    try {
                        ((PlayerBinder) mPlayerBinder).play();
                    } catch (RemoteException e) {
                        KL.e(
                                "PlayerService received broadcast, play error.", e);
                    }
                }
            } else if (ACTION_PAUSE.equals(intent.getAction())) {
                if (mPlayerBinder != null) {
                    try {
                        ((PlayerBinder) mPlayerBinder).pause();
                    } catch (RemoteException e) {
                        KL.e(
                                        "PlayerService received broadcast, pause error.",
                                        e);
                    }
                }
            } else if (ACTION_STOP_PLAYER_SERVICE.equals(intent.getAction())) {
                // Close app.
                stopPlayerService();
            }
        }
    };

    /**
     * 开始一个新的播放
     */
    private void startNewPlay(final PlayItem playItem) {
        if (playItem == null) {
            return;
        }
        mPlayItem = playItem;
        onIdle(playItem);
        if (playItem.getIsOffline()) {
            onPlayerPreparing(playItem);
        } else {
            Context context = getApplicationContext();
            bShowMobileNetWarning = false;
            onPlayerPreparing(playItem);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (!mStartForeground) {
            //TODO
//            try {
//                startForeground(
//                        KaolaNotificationManager.NOTIFICATION_ID,
//                        KaolaNotificationManager.getInstance(
//                                getApplicationContext()).getNotification());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            mStartForeground = true;
        }
        return mPlayerBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopPlayerService();
    }

    private void stopPlayerService() {
        stopSelf();
    }

    VLCMediaPlayClient.OnBufferListener mOnBufferListener = new VLCMediaPlayClient.OnBufferListener() {

        @Override
        public void onBufferListener(String theURL, int time, int length) {
            KL.i("mp>>>+ onBufferListener: {}/{} {}", time, length,
                    theURL);
        }
    };

    OnDownloadProgressListener mOnDownloadProgressListener = new OnDownloadProgressListener() {

        @Override
        public void onDownloadProgress(long downloadSize, long totalSize) {
            for (int i = 0, size = mOnDownloadProgressListeners.size(); i < size; i++) {
                VLCMediaPlayClient.OnDownloadProgressListener onDownloadProgressListener = mOnDownloadProgressListeners
                        .get(i);
                if (onDownloadProgressListener == null) {
                    continue;
                }
                onDownloadProgressListener.onDownloadProgress(downloadSize,
                        totalSize);
            }
        }
    };

    VLCMediaPlayClient.OnSeekReadyListener mOnSeekReadyListener = new VLCMediaPlayClient.OnSeekReadyListener() {
        @Override
        public void onSeekReadyListener(String theURL) {
            onPlayerSeekComplete(theURL);
        }
    };

    OnLoadFileFromLocalListener mOnLoadFileFromLocalListener = new OnLoadFileFromLocalListener() {

        @Override
        public void onLoadFileFromLocal(boolean bLocal) {
            for (int i = 0, size = mOnLoadFileFromLocalListeners.size(); i < size; i++) {
                OnLoadFileFromLocalListener onLoadFileFromLocalListener = mOnLoadFileFromLocalListeners.get(i);
                if (onLoadFileFromLocalListener == null) {
                    continue;
                }
                onLoadFileFromLocalListener.onLoadFileFromLocal(bLocal);
            }
        }
    };

    private VLCMediaPlayClient.OnBufferingStatusListener mOnBufferingStatusListener = new VLCMediaPlayClient.OnBufferingStatusListener() {
        @Override
        public void onBufferingtStart() {
            for (int i = 0, size = mPlayStateListeners.size(); i < size; i++) {
                IPlayerStateListener listener = mPlayStateListeners.get(i);
                if (listener == null) {
                    continue;
                }
                listener.onBufferingStart(mPlayItem);
            }
        }

        @Override
        public void onBufferingEnd() {
            for (int i = 0, size = mPlayStateListeners.size(); i < size; i++) {
                IPlayerStateListener listener = mPlayStateListeners.get(i);
                if (listener == null) {
                    continue;
                }
                listener.onBufferingEnd(mPlayItem);
            }
        }
    };

    VLCMediaPlayClient.OnProgressUpdateListener mOnProgressUpdateListener = new VLCMediaPlayClient.OnProgressUpdateListener() {

        @Override
        public void onProgressUpdateListener(String theURL, int position,
                                             int duration, boolean isPreDownloadComplete) {
            onProgress(theURL, position, duration, isPreDownloadComplete);
        }
    };

    VLCMediaPlayClient.OnErrorListener mOnErrorListener = new VLCMediaPlayClient.OnErrorListener() {

        @Override
        public void onErrorListener(String theURL, int what, int extra) {
            KL.i("mp>>>+ mOnErrorListener: theURL = {}, what = {}, extra = {}", theURL, what, extra);
            onPlayerFailed(what, extra);
        }

        @Override
        public void onUrlNullErrorListener() {
            onPlayerFailed(0, 0); // 对于这种情况暂时未定义，后期如果需要使用此错误可进行相关定义
        }
    };

    VLCMediaPlayClient.OnCompletionListener mOnCompletionListener = new VLCMediaPlayClient.OnCompletionListener() {

        @Override
        public void onCompletionListener(String theURL, float duration,
                                         int position) {
            KL.i("mp>>>+ mOnCompletionListener: {}", theURL);
            onPlayerEnd();
        }

    };

    VLCMediaPlayClient.OnPausedCompleteListener mOnPausedCompleteListener = new VLCMediaPlayClient.OnPausedCompleteListener() {

        @Override
        public void onPausedCompleteListener(String theURL) {
            KL.i("mp>>>+ mOnPausedCompleteListener: {}", theURL);
            onPlayerPaused();

        }
    };

    VLCMediaPlayClient.OnPlayingListener mOnPlayingListener = new VLCMediaPlayClient.OnPlayingListener() {

        @Override
        public void onPlayingListener(String theURL, int position) {
            KL.i("mp>>>+ onPlayingListener: {} position: {}", theURL,
                    position);
            if (theURL == null) {
                return;
            }

            onPlayerPlaying();
        }
    };

    VLCMediaPlayClient.OnBufferReadyListener mOnBufferReadyListener = new VLCMediaPlayClient.OnBufferReadyListener() {

        @Override
        public void onBufferReadyListener(String theURL) {
        }
    };

    VLCMediaPlayClient.OnProloadErrorListener mOnPreloadErrorListener = new VLCMediaPlayClient.OnProloadErrorListener() {

        @Override
        public void onProloadErrorListener(String theURL) {

        }

    };

    VLCMediaPlayClient.OnReconnectListener mReconnectListener = new VLCMediaPlayClient.OnReconnectListener() {

        @Override
        public void onReconnectListener(String theURL, long duration,
                                        long position) {

            for (int i = 0, size = mPlayStateListeners.size(); i < size; i++) {
                IPlayerStateListener listener = mPlayStateListeners.get(i);
                if (listener == null) {
                    continue;
                }
                listener.onPlayerPreparing(mPlayItem);
            }
//            for (IPlayerStateListener listener : mPlayStateListeners) {
//                listener.onPlayerPreparing(mPlayItem);
//            }
        }
    };

    private void addMediaPlayerListener() {
        VLCMediaPlayClient vlcMediaPlayClient = VLCMediaPlayClient
                .getInstance();
        vlcMediaPlayClient.setOnBufferListener(mOnBufferListener);
        vlcMediaPlayClient.setOnBufferReadyListener(mOnBufferReadyListener);
        vlcMediaPlayClient
                .setOnProgressUpdateListener(mOnProgressUpdateListener);
        vlcMediaPlayClient.setOnErrorListener(mOnErrorListener);
        vlcMediaPlayClient.setOnCompletionListener(mOnCompletionListener);
        vlcMediaPlayClient
                .setOnPausedCompleteListener(mOnPausedCompleteListener);
        vlcMediaPlayClient.setOnPlayingListener(mOnPlayingListener);
        vlcMediaPlayClient.setOnProloadErrorListener(mOnPreloadErrorListener);
        vlcMediaPlayClient.setOnReconnectListener(mReconnectListener);
        vlcMediaPlayClient
                .setOnDownloadProgressListener(mOnDownloadProgressListener);
        vlcMediaPlayClient
                .setOnLoadFileFromLocalListener(mOnLoadFileFromLocalListener);
        vlcMediaPlayClient.setOnSeekReadyListener(mOnSeekReadyListener);
        vlcMediaPlayClient.setOnBufferingStatusListener(mOnBufferingStatusListener);
    }

    public void onIdle(PlayItem playItem) {
        translateState(STATE_ON_IDLE);
//        for (IPlayerStateListener listener : mPlayStateListeners) {
//            listener.onIdle(playItem);
//        }

        for (int i = 0, size = mPlayStateListeners.size(); i < size; i++) {
            IPlayerStateListener listener = mPlayStateListeners.get(i);
            if (listener == null) {
                continue;
            }
            listener.onIdle(mPlayItem);
        }
    }

    /**
     * 根据播放器类型获取相对应的播放地址
     *
     * @param playItem
     * @return
     */
//    private String getRealPlayUrlByPlayerType(PlayItem playItem) {
//        String playUrl;
//        if (playItem.getIsOffline()) {
//            playUrl = playItem.getOfflinePlayUrl();
//        } else if (mTypePlayer == TYPE_OPUS_PLAYER) {
//            playUrl = playItem.getPlayUrl();
//        } else if (mTypePlayer == TYPE_M3U8_PLAYER) {
//            playUrl = playItem.getM3u8PlayUrl();
//        } else if (mTypePlayer == TYPE_MP3_PLAYER) {
//            playUrl = playItem.getMp3PlayUrl();
//        } else {
//            playUrl = playItem.getPlayUrl();
//        }
//        return playUrl;
//    }
    private void onPlayerPreparing(PlayItem playItem) {
        translateState(STATE_ON_PREPARING);
        ArrayList<IPlayerStateListener> playerStateListeners = (ArrayList<IPlayerStateListener>) mPlayStateListeners.clone();
//        for (IPlayerStateListener listener : playerStateListeners) {
//            listener.onPlayerPreparing(playItem);
//        }

        for (int i = 0, size = playerStateListeners.size(); i < size; i++) {
            IPlayerStateListener listener = playerStateListeners.get(i);
            if (listener == null) {
                continue;
            }
            listener.onPlayerPreparing(mPlayItem);
        }

//        String playUrl = getRealPlayUrlByPlayerType(playItem);
        String playUrl = getPlayUrl();
        VLCMediaPlayClient.getInstance().sendPlayURLToService(playUrl,
                playItem.getTitle(), playItem.getPosition(),
                playItem.getDuration(), 0, mPlayItem != null ? mPlayItem.isLivingUrl() : false);
    }

    private void onPlayerSeekComplete(String url) {
//        for (IPlayerStateListener listener : mPlayStateListeners) {
//            listener.onSeekComplete(url);
//        }

        for (int i = 0, size = mPlayStateListeners.size(); i < size; i++) {
            IPlayerStateListener listener = mPlayStateListeners.get(i);
            if (listener == null) {
                continue;
            }
            listener.onSeekComplete(url);
        }
    }

    private void onPlayerPlaying() {
        translateState(STATE_ON_PLAYING);
//        for (IPlayerStateListener listener : mPlayStateListeners) {
//            listener.onPlayerPlaying(mPlayItem);
//        }
        for (int i = 0, size = mPlayStateListeners.size(); i < size; i++) {
            IPlayerStateListener listener = mPlayStateListeners.get(i);
            if (listener == null) {
                continue;
            }
            listener.onPlayerPlaying(mPlayItem);
        }
        // TODO Preload next playItem
        // PlayItemEntry preloadPlayItem =
        // PlaylistManager.getInstance(this).preloadNextPlayItem();
        // if (preloadPlayItem != null) {
        // VLCMediaPlayClient.getInstance().sendPlayProloadURLToService(preloadPlayItem.getPlayUrl(),
        // preloadPlayItem.getTitle(), preloadPlayItem.getDuration());
        // }
    }

    private void onProgress(String theURL, int position, int duration, boolean isPreDownloadComplete) {
//        for (IPlayerStateListener listener : mPlayStateListeners) {
//            listener.onProgress(theURL, position, duration);
//        }
        if (bShowMobileNetWarning) {
            return;
        }
//        KL.d("onProgress----------->position = {}", position);
        for (int i = 0, size = mPlayStateListeners.size(); i < size; i++) {
            IPlayerStateListener listener = mPlayStateListeners.get(i);
            if (listener == null) {
                continue;
            }
            listener.onProgress(theURL, position, duration, isPreDownloadComplete);
        }
        if (mPlayItem != null) {
            mPlayItem.setPosition(position);
        }
    }

    private void onPlayerEnd() {
        translateState(STATE_ON_END);
//        for (IPlayerStateListener listener : mPlayStateListeners) {
//            listener.onPlayerEnd(mPlayItem);
//        }
        for (int i = 0, size = mPlayStateListeners.size(); i < size; i++) {
            IPlayerStateListener listener = mPlayStateListeners.get(i);
            if (listener == null) {
                continue;
            }
            listener.onPlayerEnd(mPlayItem);
        }
        onIdle(mPlayItem);
        mPlayItem = null;
    }

    private void onPlayerPaused() {
        translateState(STATE_ON_PAUSED);
//        for (IPlayerStateListener listener : mPlayStateListeners) {
//            listener.onPlayerPaused(mPlayItem);
//        }
        for (int i = 0, size = mPlayStateListeners.size(); i < size; i++) {
            IPlayerStateListener listener = mPlayStateListeners.get(i);
            if (listener == null) {
                continue;
            }
            listener.onPlayerPaused(mPlayItem);
        }
    }

    private void onPlayerFailed(int what, int extra) {
        translateState(extra == IjkMediaPlayer.GET_STREAM_CONNECTED_SUB_ERROR_IJK_PLAYER ?
                STATE_ON_PLAYING : STATE_ON_FAILED);
//        for (IPlayerStateListener listener : mPlayStateListeners) {
//            listener.onPlayerFailed(mPlayItem, what, extra);
//        }
        for (int i = 0, size = mPlayStateListeners.size(); i < size; i++) {
            IPlayerStateListener listener = mPlayStateListeners.get(i);
            if (listener == null) {
                continue;
            }
            listener.onPlayerFailed(mPlayItem, what, extra);
        }
    }

    private void translateState(int state) {
        KL.i("[{} --> {}]", mapStateToString(mState),
                mapStateToString(state));
        mState = state;
    }

    private String mapStateToString(int state) {
        switch (state) {
            case STATE_ON_PLAYING:
                return "STATE_ON_PLAYING";
            case STATE_ON_FAILED:
                return "STATE_ON_FAILED";
            case STATE_ON_PAUSED:
                return "STATE_ON_PAUSED";
            case STATE_ON_END:
                return "STATE_ON_END";
            case STATE_ON_PREPARING:
                return "STATE_ON_PREPARING";
            case STATE_ON_IDLE:
                return "STATE_ON_IDLE";
            default:
                return state + "";
        }
    }

    public class PlayerBinder extends Binder {
        /**
         * Opus播放器类型
         */
        private static final int TYPE_OPUS_PLAYER = 0x0001;
        /**
         * Mp3播放器类型
         */
        private static final int TYPE_MP3_PLAYER = 0x0002;
        /**
         * M3U8播放器类型
         */
        private static final int TYPE_M3U8_PLAYER = 0x0003;

        /**
         * 是否需要播放Mp3格式音频文件 true为是，false为否 默认播放opus文件格式
         */
        private int mTypePlayer = TYPE_OPUS_PLAYER;

        /**
         * 是否为MP3播放器
         *
         * @return true为是，false为否
         */
        public boolean isMp3Player() {
            return mTypePlayer == TYPE_MP3_PLAYER;
        }

        /**
         * 是否为Opus播放器
         *
         * @return true为是，false为否
         */
        public boolean isOpusPlayer() {
            return mTypePlayer == TYPE_OPUS_PLAYER;
        }

        /**
         * 是否为M3U8播放器
         *
         * @return true为是，false为否
         */
        public boolean isM3U8Player() {
            return mTypePlayer == TYPE_M3U8_PLAYER;
        }

        public long getCurrentPosition(){
            if(mPlayItem != null){
                return mPlayItem.getPosition();
            }else
                return 0L;

        }

        public void start(PlayItem playItem) throws RemoteException {
            mTypePlayer = getPlayerUrlType(playItem.getPlayUrl());
            PlayerService.this.startNewPlay(playItem);
        }

        public boolean isState(int state) throws RemoteException {
            return mState == state;
        }

        public void play() throws RemoteException {
            if (mState == STATE_ON_PAUSED) {
                if (mPlayItem != null) {
//                    String playUrl = mPlayItem.getIsOffline() ? mPlayItem
//                            .getOfflinePlayUrl() : mPlayItem.getMp3PlayUrl();
//                    String playUrl = getRealPlayUrlByPlayerType(mPlayItem);

                    Context context = getApplicationContext();
                    if (!mPlayItem.getIsOffline()) {
                        if (mPlayItem.isLivingUrl()) {
                            try {
                                PlayerBinder.this.start(mPlayItem);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        } else {
                            String playUrl = getPlayUrl();
                            VLCMediaPlayClient.getInstance().sendPlayActionToService(
                                    playUrl, mPlayItem.getTitle(), mPlayItem.getDuration());
                        }
                    } else {
                        if (mPlayItem.isLivingUrl()) {
                            start(mPlayItem);
                        } else {
                            String playUrl = getPlayUrl();
                            VLCMediaPlayClient.getInstance().sendPlayActionToService(
                                    playUrl, mPlayItem.getTitle(), mPlayItem.getDuration());
                        }
                    }
                }
            }
        }

        public void pause() throws RemoteException {
            if (mState == STATE_ON_PLAYING) {
                if (mPlayItem != null) {
//                    String playUrl = mPlayItem.getIsOffline() ? mPlayItem
//                            .getOfflinePlayUrl() : mPlayItem.getPlayUrl();
//                    String playUrl = getRealPlayUrlByPlayerType(mPlayItem);
                    if (mPlayItem.isLivingUrl()) {
                        reset();
                    } else {
                        String playUrl = getPlayUrl();
                        VLCMediaPlayClient.getInstance().sendPauseActionToService(
                                playUrl, mPlayItem.getTitle(), mPlayItem.getDuration());
                    }
                }
            }
        }

        /**
         * 停止播放器操作
         */
        public void stop() {
            if (mState == STATE_ON_PREPARING) {
                if (mPlayItem != null) {
//                    String playUrl = mPlayItem.getIsOffline() ? mPlayItem
//                            .getOfflinePlayUrl() : mPlayItem.getPlayUrl();
//                    String playUrl = getRealPlayUrlByPlayerType(mPlayItem);
                    String playUrl = getPlayUrl();
                    VLCMediaPlayClient.getInstance().sendStopActionToService(playUrl, mPlayItem.getTitle(), mPlayItem.getDuration());
                }
            }
        }

        public void stopPlayerService() throws RemoteException {
            PlayerService.this.stopPlayerService();
        }

//        public void stopAndRelease() throws RemoteException {
//            VLCMediaPlayClient.getInstance()
//                    .sendStopAndReleaseActionToService();
//        }

        public void reset() throws RemoteException {
            KL.d("play m3u8 playerService---------->start");
            if (mPlayItem != null) {
                String playUrl = getPlayUrl();
                VLCMediaPlayClient.getInstance().sendResetActionToService(playUrl, mPlayItem.getTitle());
            }
        }

        /**
         * 应用退出时可调用
         *
         * @throws RemoteException
         */
        public void release() throws RemoteException {
            mState = STATE_ON_IDLE;
            KL.i("release");
            VLCMediaPlayClient.getInstance().sendReleaseActionToService();
        }

        public void seek(int progress) throws RemoteException {
            VLCMediaPlayClient.getInstance().seekTo(progress);
        }

        public void addPlayerStateListener(IPlayerStateListener listener)
                throws RemoteException {
            if (mPlayStateListeners.contains(listener)) {
                return;
            }
            dispatchCurrentState(listener);
            mPlayStateListeners.add(listener);
        }

        public void removePlayerStateListener(IPlayerStateListener listener)
                throws RemoteException {
            if (mPlayStateListeners.contains(listener)) {
                mPlayStateListeners.remove(listener);
            }
        }

        public void addDownloadProgressListener(
                OnDownloadProgressListener listener) {
            if (mOnDownloadProgressListeners.contains(listener)) {
                return;
            }
            mOnDownloadProgressListeners.add(listener);
        }

        public void removeDownloadProgressListener(
                OnDownloadProgressListener listener) {
            if (mOnDownloadProgressListeners.contains(listener)) {
                mOnDownloadProgressListeners.remove(listener);
            }
        }

        public void addLoadFileFromLocalListener(
                OnLoadFileFromLocalListener listener) {
            if (mOnLoadFileFromLocalListeners.contains(listener)) {
                return;
            }
            mOnLoadFileFromLocalListeners.add(listener);
        }

        public void removeLoadFileFromLocalListener(
                OnLoadFileFromLocalListener listener) {
            if (mOnLoadFileFromLocalListeners.contains(listener)) {
                mOnLoadFileFromLocalListeners.remove(listener);
            }
        }
    }

    private void dispatchCurrentState(IPlayerStateListener listener) {
        switch (mState) {
            case STATE_ON_PLAYING:
                listener.onPlayerPlaying(mPlayItem);
                break;
            case STATE_ON_FAILED:
                listener.onPlayerFailed(mPlayItem, 0, 0);
                break;
            case STATE_ON_PAUSED:
                listener.onPlayerPaused(mPlayItem);
                break;
            case STATE_ON_PREPARING:
                listener.onPlayerPreparing(mPlayItem);
                break;
        }
    }

    /**
     * Callback of Player state.
     */
    public interface IPlayerStateListener {

        void onIdle(PlayItem playItem);

        void onPlayerPreparing(PlayItem playItem);

        void onPlayerPlaying(PlayItem playItem);

        void onPlayerPaused(PlayItem playItem);

        void onProgress(String url, int position, int duration, boolean isPreDownloadComplete);

        void onPlayerFailed(PlayItem playItem, int what, int extra);

        void onPlayerEnd(PlayItem playItem);

        void onSeekComplete(String url);

        void onBufferingStart(PlayItem playItem);

        void onBufferingEnd(PlayItem playItem);
    }

    /**
     * 获取一个播放的url
     *
     * @return
     */
    private String getPlayUrl() {
        if (mPlayItem == null) {
            return "";
        }
        String playUrl = mPlayItem.getIsOffline() ? mPlayItem
                .getOfflinePlayUrl() : mPlayItem.getPlayUrl();
        return playUrl;
    }

    /**
     * 根据URL判断当前需要采用的播放器类型
     *
     * @param url
     * @return 参考 TYPE_OPUS_PLAYER，TYPE_MP3_PLAYER，TYPE_M3U8_PLAYER
     */
    public int getPlayerUrlType(String url) {
        if (TextUtils.isEmpty(url)) {
            return PlayerBinder.TYPE_OPUS_PLAYER;
        }
        int type = PlayerBinder.TYPE_OPUS_PLAYER;
        int index = url.lastIndexOf('.');
        String urlSuffixStr = null;
        if (index > 0) {
            urlSuffixStr = url.substring(index, url.length());
        }
        if (urlSuffixStr != null) {
            urlSuffixStr = urlSuffixStr.toLowerCase();
            if (urlSuffixStr.endsWith(".mp3")) {
                type = PlayerBinder.TYPE_MP3_PLAYER;
            } else if (urlSuffixStr.endsWith(".m3u8") || urlSuffixStr.endsWith(".aac")) {
                type = PlayerBinder.TYPE_M3U8_PLAYER;
            } else {
                type = PlayerBinder.TYPE_OPUS_PLAYER;
            }
        }
        return type;
    }
}
