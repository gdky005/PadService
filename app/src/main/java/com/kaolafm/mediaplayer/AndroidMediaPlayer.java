package com.kaolafm.mediaplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.os.Message;

import com.gdky005.padservice.utils.KL;


public class AndroidMediaPlayer extends AbsMediaPlayer implements
        OnCompletionListener,
        OnErrorListener,
        OnPreparedListener,
        OnSeekCompleteListener,
        OnBufferingUpdateListener {

    private static final int MSG_UPDATE_TIMESTAMP = 0;
    private static final int MSG_STOP_UPDATE = 1;
    public static final long TIME_INTERVAL = 1000;
    private MediaPlayer mPlayer;

    /**
     * 当前播放器下载进度百分比（1-100）
     */
    private int mCurrentDownloadPercent;

    public AndroidMediaPlayer(Context context) {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnSeekCompleteListener(this);
        mPlayer.setOnBufferingUpdateListener(this);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_TIMESTAMP:
                    int currentPosition = (int) getCurrentPosition();
                    if (currentPosition >= TIME_INTERVAL) {
                        notifyProgressUpdate(AndroidMediaPlayer.this, currentPosition, mPlayer.getDuration());
                    }

                    sendEmptyMessageDelayed(MSG_UPDATE_TIMESTAMP, TIME_INTERVAL);
                    break;
                case MSG_STOP_UPDATE:
                    removeMessages(MSG_UPDATE_TIMESTAMP);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public long getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public long getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public boolean isPaused() {
        boolean isPlaying = false;
        try {
            isPlaying = !mPlayer.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
            KL.e("isPaused----------->error = {}", e.toString());
        }
        return isPlaying;
    }

    @Override
    public boolean isPlaying() {
        try {
            return mPlayer.isPlaying();
        } catch (Exception e) {
            KL.e("isPlaying----------->error = {}", e.toString());
        }
        return false;
    }

    @Override
    public void pause() {
        mHandler.sendEmptyMessage(MSG_STOP_UPDATE);
        try {
            mPlayer.pause();
        } catch (Exception e) {
            e.printStackTrace();
            KL.e("pause----------->error = {}", e.toString());
            return;
        }
        notifyPausedComplete(this);
    }

    @Override
    public void play() {
        try {
            mPlayer.start();
            notifyPlaybackStart(this, mPlayer.getCurrentPosition());
            mHandler.sendEmptyMessage(MSG_UPDATE_TIMESTAMP);
        } catch (Exception e) {
            e.printStackTrace();
            KL.e("play----------->error = {}", e.toString());
        }
    }

    @Override
    public void preload(String url) {
    }

    @Override
    public void reset() {
        mHandler.sendEmptyMessage(MSG_STOP_UPDATE);
        mPlayer.reset();
        notifyPausedComplete(this);
    }

    @Override
    public void release() {
        KL.d("release player-------mp3----->start");
        mPlayer.release();
        releaseListeners();
        notifyPausedComplete(this);
    }

    @Override
    public void prepare() {
        try {
//            mPlayer.prepareAsync();
            mPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            KL.e("prepareAsync----------->error = {}", e.toString());
        }
    }

    @Override
    public void seekTo(long msec) {
        try {
            mPlayer.seekTo((int) msec);
        } catch (Exception e) {
            e.printStackTrace();
            KL.e("seekTo----------->error = {}", e.toString());
        }
    }

    @Override
    public void setDataSource(String source) {
        mUrl = source;
        mCurrentDownloadPercent = 0;
        try {
            mPlayer.setDataSource(source);
        } catch (Exception e) {
            e.printStackTrace();
            KL.e("setDataSource----------->error = {}", e.toString());
        }
    }

    @Override
    public void stop() {
        mHandler.sendEmptyMessage(MSG_STOP_UPDATE);
        try {
            mPlayer.stop();
            notifyPausedComplete(this);
        } catch (Exception e) {
            e.printStackTrace();
            KL.e("stop----------->error = {}", e.toString());
        }
    }

    @Override
    public boolean isPreDownloadComplete() {
        return mCurrentDownloadPercent == 100;
    }

    @Override
    public void onSeekComplete(MediaPlayer player) {
        KL.d("onSeekComplete---------->");
        notifySeekComplete(this);
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        notifyPrepareComplete();
    }

    @Override
    public boolean onError(MediaPlayer player, int what, int extra) {
        return notifyError(this, what, extra);
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        mHandler.sendEmptyMessage(MSG_STOP_UPDATE);
        notifyPlayComplete(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (mCurrentDownloadPercent == percent) {
            return;
        }
        int totalSize = mPlayer.getDuration();
        int downloadSize = totalSize * percent / 100;
        notifyDownloadProgress(downloadSize, totalSize);
        mCurrentDownloadPercent = percent;
    }
}
