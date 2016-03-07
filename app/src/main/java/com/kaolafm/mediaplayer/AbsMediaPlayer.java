package com.kaolafm.mediaplayer;

public abstract class AbsMediaPlayer {
    private Object mLocks = new Object();

    /******************************************
     * 回调定义
     ********************************************/
    public interface OnPrepareCompleteListener {
        void onPrepareComplete(AbsMediaPlayer player);
    }

    public interface OnErrorListener {
        boolean onError(AbsMediaPlayer player, int what, int arg1);
    }

    public interface OnProgressUpdateListener {
        void onProgress(AbsMediaPlayer player, long position, long duration);
    }

    public interface OnCompletedListener {
        void onCompleted(AbsMediaPlayer player);
    }

    public interface OnPausedCompletedListener {
        void onPaused(AbsMediaPlayer player);
    }

    public interface OnStoppedCompleteListener {
        void onStopped(AbsMediaPlayer player);
    }

    public interface OnBufferingListener {
        void onBuffering(AbsMediaPlayer player);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(AbsMediaPlayer player);
    }

    public interface OnPlaybackStartListener {
        void onPlaybackStart(AbsMediaPlayer player, long startPosition);
    }

    public interface OnDownloadProgressListener {
        void onDownloadProgress(long downloadSize, long totalSize);
        // public void onDownloadComplete();
    }

    public interface OnLoadFileFromLocalListener {
        void onLoadFileFromLocal(boolean bLocal);
    }

    public interface OnInfoListener {
        boolean onInfo(AbsMediaPlayer player, int what, int extra);
    }

    public interface OnBufferingStatusListener {
        void onBufferingtStart(AbsMediaPlayer player);

        void onBufferingEnd(AbsMediaPlayer player);
    }

    /******************************************
     * 回调声明
     ********************************************/
    protected OnPrepareCompleteListener mOnPrepareCompleteListener = null;
    protected OnErrorListener mOnErrorListener = null;
    protected OnProgressUpdateListener mOnProgressUpdateListener = null;
    protected OnCompletedListener mOnCompletedListener = null;
    protected OnPausedCompletedListener mOnPausedCompletedListener = null;
    protected OnStoppedCompleteListener mOnStoppedCompleteListener = null;
    protected OnBufferingListener mOnBufferingListener = null;
    protected OnSeekCompleteListener mOnSeekCompleteListener = null;
    protected OnPlaybackStartListener mOnPlaybackStartListener = null;
    protected OnDownloadProgressListener mOnDownloadProgressListener = null;
    protected OnLoadFileFromLocalListener mOnLoadFileFromLocalListener = null;
    protected OnInfoListener mOnInfoListener = null;
    protected OnBufferingStatusListener mOnBufferingStatusListener = null;


    protected String mUrl;

    /******************************************
     * 回调设置
     ********************************************/
    public void setPrepareCompleteListener(OnPrepareCompleteListener listener) {
        mOnPrepareCompleteListener = listener;
    }

    public OnPrepareCompleteListener getPrepareCompleteListener() {
        return mOnPrepareCompleteListener;
    }

    public void setErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    public void setProgressUpdateListener(OnProgressUpdateListener listener) {
        mOnProgressUpdateListener = listener;
    }

    public void setPlayCompleteListener(OnCompletedListener listener) {
        mOnCompletedListener = listener;
    }

    public void setPausedCompleteListener(OnPausedCompletedListener listener) {
        mOnPausedCompletedListener = listener;
    }

    public void setStoppedCompleteListener(OnStoppedCompleteListener listener) {
        mOnStoppedCompleteListener = listener;
    }


    public void setBufferingListener(OnBufferingListener listener) {
        mOnBufferingListener = listener;
    }

    public void setSeekCompleteListener(OnSeekCompleteListener listener) {
        mOnSeekCompleteListener = listener;
    }

    public void setPlaybackStartListener(OnPlaybackStartListener listener) {
        mOnPlaybackStartListener = listener;
    }

    public void setDownloadProgressListener(OnDownloadProgressListener listener) {
        mOnDownloadProgressListener = listener;
    }

    public void setLoadFileFromLocalListener(OnLoadFileFromLocalListener listener) {
        mOnLoadFileFromLocalListener = listener;
    }

    public void setInfoListener(OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    public void setOnBufferingStatusListener(OnBufferingStatusListener listener) {
        mOnBufferingStatusListener = listener;
    }

    /******************************************
     * 回调调用
     ********************************************/
    public void notifyProgressUpdate(AbsMediaPlayer player, long position, long duration) {
        if (mOnProgressUpdateListener != null) {
            mOnProgressUpdateListener.onProgress(player, position, duration);
        }
    }

    public boolean notifyError(AbsMediaPlayer player, int what, int arg1) {
        if (mOnErrorListener != null) {
            return mOnErrorListener.onError(player, what, arg1);
        }
        return false;
    }

    public void notifyPrepareComplete() {
        synchronized (mLocks) {
            if (mOnPrepareCompleteListener != null) {
                mOnPrepareCompleteListener.onPrepareComplete(this);
            }
        }
    }

    public void notifyPlayComplete(AbsMediaPlayer player) {
        synchronized (mLocks) {
            if (mOnCompletedListener != null) {
                mOnCompletedListener.onCompleted(player);
            }
        }
    }

    public void notifyPausedComplete(AbsMediaPlayer player) {
        synchronized (mLocks) {
            if (mOnPausedCompletedListener != null) {
                mOnPausedCompletedListener.onPaused(player);
            }
        }
    }

    public void notifyStoppedComplete(AbsMediaPlayer player) {
        synchronized (mLocks) {
            if (mOnStoppedCompleteListener != null) {
                mOnStoppedCompleteListener.onStopped(player);
            }
        }
    }

    public void notifyBuffering(AbsMediaPlayer player) {
        synchronized (mLocks) {
            if (mOnBufferingListener != null) {
                mOnBufferingListener.onBuffering(player);
            }
        }
    }

    public void notifySeekComplete(AbsMediaPlayer player) {
        synchronized (mLocks) {
            if (mOnSeekCompleteListener != null) {
                mOnSeekCompleteListener.onSeekComplete(player);
            }
        }
    }

    public void notifyPlaybackStart(AbsMediaPlayer player, long startPosition) {
        synchronized (mLocks) {
            if (mOnPlaybackStartListener != null) {
                mOnPlaybackStartListener.onPlaybackStart(player, startPosition);
            }
        }
    }

    public void notifyDownloadProgress(long downloadSize, long totalSize) {
        synchronized (mLocks) {
            if (mOnDownloadProgressListener != null) {
                mOnDownloadProgressListener.onDownloadProgress(downloadSize, totalSize);
            }
        }
    }

    public void notifyLoadFileFromLocal(boolean bLocal) {
        synchronized (mLocks) {
            if (mOnLoadFileFromLocalListener != null) {
                mOnLoadFileFromLocalListener.onLoadFileFromLocal(bLocal);
            }
        }
    }

    public boolean notifyInfo(AbsMediaPlayer player, int what, int extra) {
        synchronized (mLocks) {
            if (mOnInfoListener != null) {
                return mOnInfoListener.onInfo(player, what, extra);
            }
        }
        return false;
    }

    public void notifyOnBufferingStart(AbsMediaPlayer player) {
        synchronized (mLocks) {
            if (mOnBufferingStatusListener != null) {
                mOnBufferingStatusListener.onBufferingtStart(player);
            }
        }
    }

    public void notifyOnBufferingEnd(AbsMediaPlayer player) {
        synchronized (mLocks) {
            if (mOnBufferingStatusListener != null) {
                mOnBufferingStatusListener.onBufferingEnd(player);
            }
        }
    }

    public String getUrl() {
        return mUrl;
    }

    abstract public long getDuration();

    abstract public long getCurrentPosition();

    abstract public boolean isPaused();

    abstract public boolean isPlaying();

    abstract public void pause();

    abstract public void play();

    abstract public void preload(String url);

    abstract public void reset();

    abstract public void release();

    abstract public void prepare();

    abstract public void seekTo(long msec);

    abstract public void setDataSource(String source);

    abstract public void stop();

    /**
     * 是否已经预缓冲完成
     *
     * @return true为是，false为否
     */
    abstract public boolean isPreDownloadComplete();

    /**
     * 释放所有回调对象
     */
    public void releaseListeners() {
        synchronized (mLocks) {
            mOnPrepareCompleteListener = null;
            mOnErrorListener = null;
            mOnProgressUpdateListener = null;
            mOnCompletedListener = null;
            mOnPausedCompletedListener = null;
            mOnStoppedCompleteListener = null;
            mOnBufferingListener = null;
            mOnSeekCompleteListener = null;
            mOnPlaybackStartListener = null;
            mOnDownloadProgressListener = null;
            mOnLoadFileFromLocalListener = null;
            mOnInfoListener = null;
        }
    }
}
