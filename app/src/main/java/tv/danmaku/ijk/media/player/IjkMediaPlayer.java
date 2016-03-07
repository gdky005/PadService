/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2013 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.danmaku.ijk.media.player;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.gdky005.padservice.utils.KL;
import com.kaolafm.mediaplayer.AbsM3U8MediaPlayer;
import com.kaolafm.mediaplayer.AbsMediaPlayer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import tv.danmaku.ijk.media.player.annotations.AccessedByNative;
import tv.danmaku.ijk.media.player.annotations.CalledByNative;
import tv.danmaku.ijk.media.player.option.AvFormatOption;
import tv.danmaku.ijk.media.player.pragma.DebugLog;

/**
 * @author bbcallen
 *         <p/>
 *         Java wrapper of ffplay.
 */
public final class IjkMediaPlayer extends AbsM3U8MediaPlayer {
    private final static String TAG = IjkMediaPlayer.class.getName();

    private static final int MEDIA_NOP = 0; // interface test message
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    private static final int MEDIA_BUFFERING_UPDATE = 3;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_SET_VIDEO_SIZE = 5;
    private static final int MEDIA_PLAYER_PTS_UPDATE = 6;
    /**
     * 点播预加载时间戳
     */
    private static final int MEDIA_PLAYER_VOD_PTS_PRELOAD_UPDATE = 8;
    private static final int MEDIA_TIMED_TEXT = 99;
    private static final int MEDIA_ERROR = 100;
    private static final int MEDIA_INFO = 200;

    protected static final int MEDIA_SET_VIDEO_SAR = 10001;

    /**
     * 来自底层PLAYER错误码
     */
    public static final int MEDIA_ERROR_IJK_PLAYER = -10000;
    /**
     * 没有 ts 文件造成的错误
     */
    public static final int NO_TS_FILE_ERROR_IJK_PLAYER = 400;
    /**
     * 播放ID不存在，表示服务器上还没有这个id，表示这个节目从来没有在服务器上出现过
     */
    public static final int NO_ID_SUB_ERROR_IJK_PLAYER = 404;
    /**
     * 网关错误。这个错误目前没有找到具体出现的场景
     */
    public static final int BAD_GATEWAY_SUB_ERROR_IJK_PLAYER = 502;
    /**
     * 播放的文件不存在。是在拿到索引文件后，却拿不到具体的TS文件，这时通常都是么有推流
     */
    public static final int NO_FILE_SUB_ERROR_IJK_PLAYER = 503;
    /**
     * 域名解析错误
     */
    public static final int DOMAIN_SUB_ERROR_IJK_PLAYER = 700;
    /**
     * 多次请求索引文件相同 多半是服务器没有更新m3u8文件。这个错误码会在连续10次请求到相同索引文件的情况下发出
     */
    public static final int NO_UPDATE_SUB_ERROR_IJK_PLAYER = 701;
    /**
     * 底层播放器断点重试
     */
    public static final int GET_STREAM_FAILED_SUB_ERROR_IJK_PLAYER = 0;
    /**
     * 底层播放器因网络断开开始尝试重连
     */
    public static final int GET_STREAM_CONNECTION_SUB_ERROR_IJK_PLAYER = 900;

    /**
     * 底层播放器已经从断网中恢复
     */
    public static final int GET_STREAM_CONNECTED_SUB_ERROR_IJK_PLAYER = 901;

    @AccessedByNative
    private long mNativeMediaPlayer;

    @AccessedByNative
    private int mNativeSurfaceTexture;

    @AccessedByNative
    private int mListenerContext;

    private SurfaceHolder mSurfaceHolder;
    private EventHandler mEventHandler;
    private PowerManager.WakeLock mWakeLock = null;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoSarNum;
    private int mVideoSarDen;

    private String mDataSource;
    private String mFFConcatContent;

//    private OnPreparedListener mOnPreparedListener;
//    private OnCompletionListener mOnCompletionListener;
//    private OnBufferingUpdateListener mOnBufferingUpdateListener;
//    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
//    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
//    private OnInfoListener mOnInfoListener;
//    private IMediaPlayer.OnErrorListener mOnErrorListener;


    /**
     * Default library loader Load them by yourself, if your libraries are not
     * installed at default place.
     */
    private static IjkLibLoader sLocalLibLoader = new IjkLibLoader() {
        @Override
        public void loadLibrary(String libName) throws UnsatisfiedLinkError,
                SecurityException {
            System.loadLibrary(libName);
        }
    };

    private static volatile boolean mIsLibLoaded = false;

    public static void loadLibrariesOnce(IjkLibLoader libLoader) {
        synchronized (IjkMediaPlayer.class) {
            if (!mIsLibLoaded) {
                try {
                    libLoader.loadLibrary("kaolafmutil");
                    libLoader.loadLibrary("kaolafmsdl");
                    libLoader.loadLibrary("kaolafmffmpeg");
                    libLoader.loadLibrary("kaolafmplayer");
                    mIsLibLoaded = true;
                } catch (UnsatisfiedLinkError ule) {
                    ule.printStackTrace();
                    KL.e("load ijk lib failed {}", ule.getLocalizedMessage());
                } catch (Throwable t) {
                    t.printStackTrace();
                    KL.e("load ijk lib failed {}", t.getLocalizedMessage());
                }
            }
        }
    }

    private static volatile boolean mIsNativeInitialized = false;

    private static void initNativeOnce() {
        synchronized (IjkMediaPlayer.class) {
            if (!mIsNativeInitialized) {
                native_init();
                mIsNativeInitialized = true;
            }
        }
    }

    /**
     * Default constructor. Consider using one of the create() methods for
     * synchronously instantiating a IjkMediaPlayer from a Uri or resource.
     * <p>
     * When done with the IjkMediaPlayer, you should call {@link #release()}, to
     * free the resources. If not released, too many IjkMediaPlayer instances
     * may result in an exception.
     * </p>
     */
    public IjkMediaPlayer() {
        this(sLocalLibLoader);
    }

//    public static IjkMediaPlayer getInstance() {
//        return IJKPLAYERINSTANCE.IJKMEDIAPLAYER;
//    }

//    private static class IJKPLAYERINSTANCE {
//        private static final IjkMediaPlayer IJKMEDIAPLAYER = new IjkMediaPlayer();
//    }

    /**
     * do not loadLibaray
     *
     * @param libLoader
     */
    public IjkMediaPlayer(IjkLibLoader libLoader) {
        initPlayer(libLoader);
//        GetHostsMgr.getInstance().setPlayerInited();
    }

    private void initPlayer(IjkLibLoader libLoader) {
        loadLibrariesOnce(libLoader);
        initNativeOnce();

        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

		/*
         * Native setup requires a weak reference to our object. It's easier to
		 * create it here than in C++.
		 */
        native_setup(new WeakReference<IjkMediaPlayer>(this));
    }

    /*
     * Update the IjkMediaPlayer SurfaceTexture. Call after setting a new
     * display surface.
     */
    private native void _setVideoSurface(Surface surface);

    /**
     * Sets the {@link SurfaceHolder} to use for displaying the
     * video portion of the media.
     * <p/>
     * Either a surface holder or surface must be set if a display or video sink
     * is needed. Not calling this method or
     * {@link #setSurface(Surface)} when playing back a video will
     * result in only the audio track being played. A null surface holder or
     * surface will result in only the audio track being played.
     *
     * @param sh the SurfaceHolder to use for video display
     */
    public void setDisplay(SurfaceHolder sh) {
        mSurfaceHolder = sh;
        Surface surface;
        if (sh != null) {
            surface = sh.getSurface();
        } else {
            surface = null;
        }
        _setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    /**
     * Sets the {@link Surface} to be used as the sink for the
     * video portion of the media. This is similar to
     * {@link #setDisplay(SurfaceHolder)}, but does not support
     * {@link #setScreenOnWhilePlaying(boolean)}. Setting a Surface will un-set
     * any Surface or SurfaceHolder that was previously set. A null surface will
     * result in only the audio track being played.
     * <p/>
     * If the Surface sends frames to a {@link android.graphics.SurfaceTexture},
     * the timestamps returned from
     * {@link android.graphics.SurfaceTexture#getTimestamp()} will have an
     * unspecified zero point. These timestamps cannot be directly compared
     * between different media sources, different instances of the same media
     * source, or multiple runs of the same program. The timestamp is normally
     * monotonically increasing and is unaffected by time-of-day adjustments,
     * but it is reset when the position is set.
     *
     * @param surface The {@link Surface} to be used for the video
     *                portion of the media.
     */
    public void setSurface(Surface surface) {
        if (mScreenOnWhilePlaying && surface != null) {
            DebugLog.w(TAG,
                    "setScreenOnWhilePlaying(true) is ineffective for Surface");
        }
        mSurfaceHolder = null;
        _setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    /**
     * Sets the data source (file-path or http/rtsp URL) to use.
     *
     * @param path the path of the file, or the http/rtsp URL of the stream you
     *             want to play
     * @throws IllegalStateException if it is called in an invalid state
     *                               <p/>
     *                               <p/>
     *                               When <code>path</code> refers to a local file, the file may
     *                               actually be opened by a process other than the calling
     *                               application. This implies that the pathname should be an
     *                               absolute path (as any other process runs with unspecified
     *                               current working directory), and that the pathname should
     *                               reference a world-readable file. As an alternative, the
     *                               application could first open the file for reading, and then
     *                               use the file descriptor form
     *                               {@link #setDataSource(java.io.FileDescriptor)}.
     */
    @Override
    public void setDataSource(String path) {
        mDataSource = path;
        mUrl = path;
        try {
            _setDataSource(path, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private native void _setDataSource(String path, String[] keys,
                                       String[] values) throws IOException, IllegalArgumentException,
            SecurityException, IllegalStateException;

    public String getDataSource() {
        return mDataSource;
    }

    public void setDataSourceAsFFConcatContent(String ffConcatContent) {
        mFFConcatContent = ffConcatContent;
    }

    @Override
    public void prepare() throws IllegalStateException {
        if (TextUtils.isEmpty(mFFConcatContent)) {
            _prepareAsync();
        } else {
            _prepareAsync();
        }
    }

    public native void _prepareAsync() throws IllegalStateException;

    @Override
    public void play() {
//        KL.d("play-------------->mUrl = {}", mUrl);
//        stayAwake(true);
        _start();
    }

    @Override
    public void preload(String url) {

    }

    private native void _start() throws IllegalStateException;

    @Override
    public void stop() throws IllegalStateException {
//        stayAwake(false);
        _stop();
    }

    @Override
    public boolean isPreDownloadComplete() {
        return false;
    }

    private native void _stop() throws IllegalStateException;

    @Override
    public void pause() throws IllegalStateException {
        KL.d("play m3u8 pause start");
//        stayAwake(false);
        _pause();
        notifyPausedComplete(this);
    }

    private native void _pause() throws IllegalStateException;

    @SuppressLint("Wakelock")
    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                washeld = true;
                mWakeLock.release();
            }
            mWakeLock = null;
        }

        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(mode | PowerManager.ON_AFTER_RELEASE,
                IjkMediaPlayer.class.getName());
        mWakeLock.setReferenceCounted(false);
        if (washeld) {
            mWakeLock.acquire();
        }
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mScreenOnWhilePlaying != screenOn) {
            if (screenOn && mSurfaceHolder == null) {
                DebugLog
                        .w(TAG,
                                "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
            }
            mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
    }

    @SuppressLint("Wakelock")
//    private void stayAwake(boolean awake) {
//        if (mWakeLock != null) {
//            if (awake && !mWakeLock.isHeld()) {
//                mWakeLock.acquire();
//            } else if (!awake && mWakeLock.isHeld()) {
//                mWakeLock.release();
//            }
//        }
//        mStayAwake = awake;
//        updateSurfaceScreenOn();
//    }

    private void updateSurfaceScreenOn() {
        if (mSurfaceHolder != null) {
            mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
        }
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public int getVideoSarNum() {
        return mVideoSarNum;
    }

    public int getVideoSarDen() {
        return mVideoSarDen;
    }


    @Override
    public native boolean isPlaying();

    @Override
    public native void seekTo(long msec) throws IllegalStateException;

    @Override
    public native long getCurrentPosition();

    @Override
    public boolean isPaused() {
        return !isPlaying();
    }

    @Override
    public native long getDuration();

    /**
     * Releases resources associated with this IjkMediaPlayer object. It is
     * considered good practice to call this method when you're done using the
     * IjkMediaPlayer. In particular, whenever an Activity of an application is
     * paused (its onPause() method is called), or stopped (its onStop() method
     * is called), this method should be invoked to release the IjkMediaPlayer
     * object, unless the application has a special need to keep the object
     * around. In addition to unnecessary resources (such as memory and
     * instances of codecs) being held, failure to call this method immediately
     * if a IjkMediaPlayer object is no longer needed may also lead to
     * continuous battery consumption for mobile devices, and playback failure
     * for other applications if no multiple instances of the same codec are
     * supported on a device. Even if multiple instances of the same codec are
     * supported, some performance degradation may be expected when unnecessary
     * multiple instances are used at the same time.
     */
    @Override
    public void release() {
//        stayAwake(false);
//        updateSurfaceScreenOn();
        releaseListeners();
        _release();
    }

    private native void _release();

    @Override
    public void reset() {
        KL.d("play m3u8 reset start");
//        stayAwake(false);
        _stop();
        // make sure none of the listeners get called anymore
        mEventHandler.removeCallbacksAndMessages(null);

        mVideoWidth = 0;
        mVideoHeight = 0;
        notifyPausedComplete(this);
    }

    private native void _reset();

    public native void setVolume(float leftVolume, float rightVolume);

    public MediaInfo getMediaInfo() {
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.mMediaPlayerName = "ijkplayer";

        String videoCodecInfo = _getVideoCodecInfo();
        if (!TextUtils.isEmpty(videoCodecInfo)) {
            String nodes[] = videoCodecInfo.split(",");
            if (nodes.length >= 2) {
                mediaInfo.mVideoDecoder = nodes[0];
                mediaInfo.mVideoDecoderImpl = nodes[1];
            } else if (nodes.length >= 1) {
                mediaInfo.mVideoDecoder = nodes[0];
                mediaInfo.mVideoDecoderImpl = "";
            }
        }

        String audioCodecInfo = _getAudioCodecInfo();
        if (!TextUtils.isEmpty(audioCodecInfo)) {
            String nodes[] = audioCodecInfo.split(",");
            if (nodes.length >= 2) {
                mediaInfo.mAudioDecoder = nodes[0];
                mediaInfo.mAudioDecoderImpl = nodes[1];
            } else if (nodes.length >= 1) {
                mediaInfo.mAudioDecoder = nodes[0];
                mediaInfo.mAudioDecoderImpl = "";
            }
        }

        try {
            mediaInfo.mMeta = IjkMediaMeta.parse(_getMediaMeta());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return mediaInfo;
    }

    private native String _getVideoCodecInfo();

    private native String _getAudioCodecInfo();

    public void setAvOption(AvFormatOption option) {
        setAvFormatOption(option.getName(), option.getValue());
    }

    public void setAvFormatOption(String name, String value) {
        _setAvFormatOption(name, value);
    }

    public void setAvCodecOption(String name, String value) {
        _setAvCodecOption(name, value);
    }

    public void setSwScaleOption(String name, String value) {
        _setSwScaleOption(name, value);
    }

    /**
     * @param chromaFourCC AvFourCC.SDL_FCC_RV16 AvFourCC.SDL_FCC_RV32
     *                     AvFourCC.SDL_FCC_YV12
     */
    public void setOverlayFormat(int chromaFourCC) {
        _setOverlayFormat(chromaFourCC);
    }

    /**
     * @param frameDrop =0 do not drop any frame <0 drop as many frames as possible >0
     *                  display 1 frame per `frameDrop` continuous dropped frames,
     */
    public void setFrameDrop(int frameDrop) {
        _setFrameDrop(frameDrop);
    }

    public void setMediaCodecEnabled(boolean enabled) {
        _setMediaCodecEnabled(enabled);
    }

    public void setOpenSLESEnabled(boolean enabled) {
        _setOpenSLESEnabled(enabled);
    }

    public void setAutoPlayOnPrepared(boolean enabled) {
        _setAutoPlayOnPrepared(enabled);
    }

    public void setDnsAddress(String[] values) throws IOException, IllegalArgumentException,
            SecurityException, IllegalStateException {
        _setDnsAddress(values);
    }

    private native void _setAvFormatOption(String name, String value);

    private native void _setAvCodecOption(String name, String value);

    private native void _setSwScaleOption(String name, String value);

    private native void _setOverlayFormat(int chromaFourCC);

    private native void _setFrameDrop(int frameDrop);

    private native void _setMediaCodecEnabled(boolean enabled);

    private native void _setOpenSLESEnabled(boolean enabled);

    private native void _setAutoPlayOnPrepared(boolean enabled);

    public Bundle getMediaMeta() {
        return _getMediaMeta();
    }

    private native Bundle _getMediaMeta();

    public static String getColorFormatName(int mediaCodecColorFormat) {
        return _getColorFormatName(mediaCodecColorFormat);
    }

    private static native final String _getColorFormatName(
            int mediaCodecColorFormat);

    private static native final void native_init();

    private native final void native_setup(Object IjkMediaPlayer_this);

    private native final void native_finalize();

    private native final void native_message_loop(Object IjkMediaPlayer_this);

    private native void _setDnsAddress(String[] values) throws IOException, IllegalArgumentException,
            SecurityException, IllegalStateException;

    protected void finalize() {
        native_finalize();
    }

    private static class EventHandler extends Handler {
        private WeakReference<IjkMediaPlayer> mWeakPlayer;

        public EventHandler(IjkMediaPlayer mp, Looper looper) {
            super(looper);
            mWeakPlayer = new WeakReference<IjkMediaPlayer>(mp);
        }

        @Override
        public void handleMessage(final Message msg) {
           final IjkMediaPlayer player = mWeakPlayer.get();
            if (player == null || player.mNativeMediaPlayer == 0) {
                DebugLog.w(TAG,
                        "IjkMediaPlayer went away with unhandled events");
                return;
            }
            switch (msg.what) {
                case MEDIA_PREPARED:
                    player.notifyPrepareComplete();
                    player.notifyPlaybackStart(player, player.getCurrentPosition());
                    return;
                case MEDIA_PLAYBACK_COMPLETE:
                    player.notifyPlayComplete(player);
//                    tv.danmaku.ijk.media.player.stayAwake(false);
                    return;
                case MEDIA_PLAYER_VOD_PTS_PRELOAD_UPDATE:
//                    KL.d("MEDIA_PLAYER_VOD_PTS_PRELOAD_UPDATE---->msg.arg1 = {}, msg.arg2 = {}",
//                            msg.arg1, msg.arg2);
                    int dPosition = msg.arg1;
                    int dDuration = msg.arg2;
                    if (dPosition < 0 || dDuration < 0) {
                        return;
                    }
                    int currDownloadPosition = dDuration - dPosition < 1000 ? dDuration : dPosition; // TS文件下载容错处理
                    player.notifyDownloadProgress(currDownloadPosition, dDuration);
                    break;
                case MEDIA_PLAYER_PTS_UPDATE:
//                    KL.d("MEDIA_PLAYER_PTS_UPDATE-------> msg.arg1 = {}, msg.arg2 = {}",
//                            msg.arg1, msg.arg2);
                    int position = msg.arg1;
                    int duration = msg.arg2;
                    if (position < 0 || duration < 0 || (duration < position)) { // 处理M3U8点播节目出现时间戳混乱问题
                        player.notifyPlayComplete(player);
                        return;
                    }
                    player.notifyProgressUpdate(player, position, duration);
                    break;
                case MEDIA_BUFFERING_UPDATE:
//                    long bufferPosition = msg.arg1;
                    //System.out.println("handleMessage------->" + msg.arg1
                    //	+ "---->duration = " + msg.arg2);
//                    if (bufferPosition < 0) {
//                        bufferPosition = 0;
//                    }

//                    long percent = 0;
//                    long duration = tv.danmaku.ijk.media.player.getDuration();
//                    if (duration > 0) {
//                        percent = bufferPosition * 100 / duration;
//                    }
//                    if (percent >= 100) {
//                        percent = 100;
//                    }
                    // DebugLog.efmt(TAG, "Buffer (%d%%) %d/%d", percent,
                    // bufferPosition, duration);
//                    tv.danmaku.ijk.media.player.notifyOnBufferingUpdate((int) percent);
//                    tv.danmaku.ijk.media.player.notifyOnDownloadProgress((int) bufferPosition, (int) duration);
                    return;
                case MEDIA_SEEK_COMPLETE:
//                    tv.danmaku.ijk.media.player.notifyOnSeekComplete();
                    player.notifySeekComplete(player);
                    return;

                case MEDIA_SET_VIDEO_SIZE:
                    player.mVideoWidth = msg.arg1;
                    player.mVideoHeight = msg.arg2;
//                    tv.danmaku.ijk.media.player.notifyOnVideoSizeChanged(tv.danmaku.ijk.media.player.mVideoWidth,
//                            tv.danmaku.ijk.media.player.mVideoHeight, tv.danmaku.ijk.media.player.mVideoSarNum,
//                            tv.danmaku.ijk.media.player.mVideoSarDen);
                    return;

                case MEDIA_ERROR:
                    KL.d("media_error------------>msg.arg1 = {}, msg.arg2 = {}", msg.arg1, msg.arg2);

//                    postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (!tv.danmaku.ijk.media.player.notifyError(tv.danmaku.ijk.media.player, msg.arg1, msg.arg2)) {
//                                tv.danmaku.ijk.media.player.notifyPlayComplete(tv.danmaku.ijk.media.player);
//                            }
//                        }
//                    },1000*2);

                    if (!player.notifyError(player, msg.arg1, msg.arg2)) {
                                player.notifyPlayComplete(player);
                            }
//                    tv.danmaku.ijk.media.player.stayAwake(false);
                    return;
                case MEDIA_INFO:
                    if (msg.arg1 != MEDIA_INFO_VIDEO_TRACK_LAGGING) {
                        DebugLog.i(TAG, "Info (" + msg.arg1 + "," + msg.arg2 + ")");
                    }
                    player.notifyInfo(player, msg.arg1, msg.arg2);
                    if (msg.arg1 == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                        player.notifyOnBufferingStart(player);
                    } else if (msg.arg1 == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                        player.notifyOnBufferingEnd(player);
                    }
                    // No real default action so far.
                    return;
                case MEDIA_TIMED_TEXT:
                    // do nothing
                    break;

                case MEDIA_NOP: // interface test message - ignore
                    break;

                case MEDIA_SET_VIDEO_SAR:
                    player.mVideoSarNum = msg.arg1;
                    player.mVideoSarDen = msg.arg2;
//                    tv.danmaku.ijk.media.player.notifyOnVideoSizeChanged(tv.danmaku.ijk.media.player.mVideoWidth,
//                            tv.danmaku.ijk.media.player.mVideoHeight, tv.danmaku.ijk.media.player.mVideoSarNum,
//                            tv.danmaku.ijk.media.player.mVideoSarDen);
                    break;

                default:
//                    DebugLog.e(TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
    }

    /*
     * Called from native code when an interesting event happens. This method
     * just uses the EventHandler system to post the event back to the main app
     * thread. We use a weak reference to the original IjkMediaPlayer object so
     * that the native code is safe from the object disappearing from underneath
     * it. (This is the cookie passed to native_setup().)
     */
    @CalledByNative
    private static void postEventFromNative(Object weakThiz, int what,
                                            int arg1, int arg2, Object obj) {
        if (weakThiz == null)
            return;

        @SuppressWarnings("rawtypes")
        IjkMediaPlayer mp = (IjkMediaPlayer) ((WeakReference) weakThiz).get();
        if (mp == null) {
            return;
        }

        if (what == MEDIA_INFO && arg1 == MEDIA_INFO_STARTED_AS_NEXT) {
            // this acquires the wakelock if needed, and sets the client side
            // state
            mp.play();
        }
        if (mp.mEventHandler != null) {
            Message m = mp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            mp.mEventHandler.sendMessage(m);
        }
    }

    private OnControlMessageListener mOnControlMessageListener;

//    public void setOnControlMessageListener(OnControlMessageListener listener) {
//        mOnControlMessageListener = listener;
//    }

    public static interface OnControlMessageListener {
        public int onControlResolveSegmentCount();

        public String onControlResolveSegmentUrl(int segment);

        public String onControlResolveSegmentOfflineMrl(int segment);

        public int onControlResolveSegmentDuration(int segment);
    }

    @CalledByNative
    private static int onControlResolveSegmentCount(Object weakThiz) {
        DebugLog.ifmt(TAG, "onControlResolveSegmentCount");
        if (weakThiz == null || !(weakThiz instanceof WeakReference<?>))
            return -1;

        @SuppressWarnings("unchecked")
        WeakReference<IjkMediaPlayer> weakPlayer = (WeakReference<IjkMediaPlayer>) weakThiz;
        IjkMediaPlayer player = weakPlayer.get();
        if (player == null)
            return -1;

        OnControlMessageListener listener = player.mOnControlMessageListener;
        if (listener == null)
            return -1;

        return listener.onControlResolveSegmentCount();
    }

    @CalledByNative
    private static String onControlResolveSegmentUrl(Object weakThiz,
                                                     int segment) {
        DebugLog.ifmt(TAG, "onControlResolveSegmentUrl %d", segment);
        if (weakThiz == null || !(weakThiz instanceof WeakReference<?>))
            return null;

        @SuppressWarnings("unchecked")
        WeakReference<IjkMediaPlayer> weakPlayer = (WeakReference<IjkMediaPlayer>) weakThiz;
        IjkMediaPlayer player = weakPlayer.get();
        if (player == null)
            return null;

        OnControlMessageListener listener = player.mOnControlMessageListener;
        if (listener == null)
            return null;

        return listener.onControlResolveSegmentUrl(segment);
    }

    @CalledByNative
    private static String onControlResolveSegmentOfflineMrl(Object weakThiz,
                                                            int segment) {
        DebugLog.ifmt(TAG, "onControlResolveSegmentOfflineMrl %d", segment);
        if (weakThiz == null || !(weakThiz instanceof WeakReference<?>))
            return null;

        @SuppressWarnings("unchecked")
        WeakReference<IjkMediaPlayer> weakPlayer = (WeakReference<IjkMediaPlayer>) weakThiz;
        IjkMediaPlayer player = weakPlayer.get();
        if (player == null)
            return null;

        OnControlMessageListener listener = player.mOnControlMessageListener;
        if (listener == null)
            return null;

        return listener.onControlResolveSegmentOfflineMrl(segment);
    }

    @CalledByNative
    private static int onControlResolveSegmentDuration(Object weakThiz,
                                                       int segment) {
        DebugLog.ifmt(TAG, "onControlResolveSegmentDuration %d", segment);
        if (weakThiz == null || !(weakThiz instanceof WeakReference<?>))
            return -1;

        @SuppressWarnings("unchecked")
        WeakReference<IjkMediaPlayer> weakPlayer = (WeakReference<IjkMediaPlayer>) weakThiz;
        IjkMediaPlayer player = weakPlayer.get();
        if (player == null)
            return -1;

        OnControlMessageListener listener = player.mOnControlMessageListener;
        if (listener == null)
            return -1;

        return listener.onControlResolveSegmentDuration(segment);
    }

    public static interface OnMediaCodecSelectListener {
        String onMediaCodecSelect(AbsMediaPlayer mp, String mimeType,
                                  int profile, int level);
    }

    private OnMediaCodecSelectListener mOnMediaCodecSelectListener;

//    public void setOnMediaCodecSelectListener(
//            OnMediaCodecSelectListener listener) {
//        mOnMediaCodecSelectListener = listener;
//    }

//    public void resetListeners() {
////        super.resetListeners();
//        mOnMediaCodecSelectListener = null;
//    }

    @CalledByNative
    private static String onSelectCodec(Object weakThiz, String mimeType,
                                        int profile, int level) {
        if (weakThiz == null || !(weakThiz instanceof WeakReference<?>))
            return null;

        @SuppressWarnings("unchecked")
        WeakReference<IjkMediaPlayer> weakPlayer = (WeakReference<IjkMediaPlayer>) weakThiz;
        IjkMediaPlayer player = weakPlayer.get();
        if (player == null)
            return null;

        OnMediaCodecSelectListener listener = player.mOnMediaCodecSelectListener;
        if (listener == null)
            listener = DefaultMediaCodecSelector.sInstance;

        return listener.onMediaCodecSelect(player, mimeType, profile, level);
    }

    public static class DefaultMediaCodecSelector implements
            OnMediaCodecSelectListener {
        public static DefaultMediaCodecSelector sInstance = new DefaultMediaCodecSelector();

        @SuppressWarnings("deprecation")
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public String onMediaCodecSelect(AbsMediaPlayer mp, String mimeType,
                                         int profile, int level) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                return null;

            if (TextUtils.isEmpty(mimeType))
                return null;

            KL.i("onSelectCodec: mime={}, profile={}, level={}", mimeType, profile, level);
            ArrayList<IjkMediaCodecInfo> candidateCodecList = new ArrayList<IjkMediaCodecInfo>();
            int numCodecs = MediaCodecList.getCodecCount();
            for (int i = 0; i < numCodecs; i++) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
                KL.d("  found codec: {}", codecInfo.getName());
                if (codecInfo.isEncoder())
                    continue;

                String[] types = codecInfo.getSupportedTypes();
                if (types == null)
                    continue;

                for (String type : types) {
                    if (TextUtils.isEmpty(type))
                        continue;

                    KL.d("    mime: {}", type);
                    if (!type.equalsIgnoreCase(mimeType))
                        continue;

                    IjkMediaCodecInfo candidate = IjkMediaCodecInfo
                            .setupCandidate(codecInfo, mimeType);
                    if (candidate == null)
                        continue;

                    candidateCodecList.add(candidate);
                    KL.i("candidate codec: {} rank={}", codecInfo.getName(), candidate.mRank);
                    candidate.dumpProfileLevels(mimeType);
                }
            }

            if (candidateCodecList.isEmpty()) {
                return null;
            }

            IjkMediaCodecInfo bestCodec = candidateCodecList.get(0);

            for (IjkMediaCodecInfo codec : candidateCodecList) {
                if (codec.mRank > bestCodec.mRank) {
                    bestCodec = codec;
                }
            }

            if (bestCodec.mRank < IjkMediaCodecInfo.RANK_LAST_CHANCE) {
                KL.w("unaccetable codec:{}", bestCodec.mCodecInfo.getName());
                return null;
            }

            KL.i("selected codec: {} rank={}", bestCodec.mCodecInfo.getName(), bestCodec.mRank);
            return bestCodec.mCodecInfo.getName();
        }
    }

//    public final void setOnVideoSizeChangedListener(
//            OnVideoSizeChangedListener listener) {
//        mOnVideoSizeChangedListener = listener;
//    }

//    private class IjkMediaPlayerListenerHolder {
//        public WeakReference<IjkMediaPlayer> mWeakMediaPlayer;
//
//        public IjkMediaPlayerListenerHolder(IjkMediaPlayer mp) {
//            mWeakMediaPlayer = new WeakReference<IjkMediaPlayer>(mp);
//        }
//    }
}
