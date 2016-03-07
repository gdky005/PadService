package com.kaolafm.mediaplayer;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.gdky005.padservice.utils.KL;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class KaolaMediaPlayer extends AbsMediaPlayer {

    enum MediaState {
        STATE_IDLE, STATE_INITIALIZED, STATE_PREPARING, STATE_PREPARED, STATE_ERROR, STATE_PLAYING, STATE_PAUSED, STATE_SEEKING, STATE_SEEKDONE, STATE_STOPPED, STATE_RELEASED
    }

    private AudioTrack mAudioTrack = null;
    private MediaChunkData mBufferedData = null;
    private int mPlayOffset = 0;
    private PlayerThread mPlayerThread = null;
    private MediaState mState = MediaState.STATE_IDLE;
    private KaolaDecoder mDecoder = null;
    private Context mContext;
    private MediaFile mMediaFile;
    private boolean mIsStreaming;
    private long mRequestBytes = -1;
    private static final int FIXED_SAMPLE_RATE = 48000;
    private static final int READ_TIMEOUT = 1000 * 5;
    private static final int CONNECT_TIME_OUT = 1000 * 30;
    private static final int MSG_FETCH_HEADER_DONE = 0;
    private static final int MSG_FETCH_HEADER_ERROR = 1;
    private static final int MSG_DOWNLOAD_CHUNK_DONE = 2;
    private static final int MSG_DOWNLOAD_CHUNK_ERROR = 3;
    private static final int MSG_BUFFER_RESUME = 4;
    /**
     * 获取网络数据默认缓冲区大小
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * 最大重试次数
     */
    private static final int MAX_REDIRECT_COUNT = 5;

    /**
     * 是否已经预缓存完成true为是，false为否
     */
    private boolean isPreDownloadComplete;

    public KaolaMediaPlayer(Context ctx) {
        mContext = ctx;
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                FIXED_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, FIXED_SAMPLE_RATE * 4,
                AudioTrack.MODE_STREAM);
        mPlayerThread = new PlayerThread();
        mPlayerThread.start();
    }

    private class PlayerThread extends Thread {
        private static final int MSG_PLAY_NEXT_BUFFER = 0;
        private static final int MSG_PLAY_ERROR_BUFFER = 1;
        public static final int MSG_PLAY_PAUSE = 2;
        public static final int MSG_PLAY_PLAY = 3;
        public static final int MSG_PLAY_STOP = 4;
        public static final int MSG_PLAY_RESET = 5;
        public static final int MSG_PLAY_RELEASE = 6;
        public static final int MSG_PLAY_SETSOURCE = 8;
        private static final int MSG_PLAY_COMPLETE = 7;
        public static final int MSG_PLAY_PREPARE = 9;
        public static final int MSG_PLAY_SEEK_COMPLETE = 10;
        public static final int MSG_PLAY_START_SEEK = 11;
        private static final int FIXED_BUFFER_SIZE = 4800;
        public Handler mAudioBufferProcessor = null;
        private boolean mPaused = false;

        public void pauseImmediately() {
            mPaused = true;
        }

        public void run() {
            Looper.prepare();
            mAudioBufferProcessor = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_PLAY_NEXT_BUFFER:
                            // LogUtil.Log(TAG, "MSG_PLAY_NEXTBUFFER " + mPaused);
                            if (mPaused) {
//                                if (mOnPausedCompletedListener != null) {
//                                    mOnPausedCompletedListener
//                                            .onPaused(KaolaMediaPlayer.this);
//                                }
                                notifyPausedComplete(KaolaMediaPlayer.this);
                                break;
                            }
                            MediaChunkData data = mBufferedData;
                            try {
                                if (data == null) {
                                    data = mDecoder.getAudioChunkData();
                                    mBufferedData = data;
                                }
                            } catch (OutOfMemoryError oom) {
//                                if (mOnBufferingListener != null) {
//                                    mOnBufferingListener
//                                            .onBuffering(KaolaMediaPlayer.this);
//                                }
                                notifyBuffering(KaolaMediaPlayer.this);
                                break;
                            } catch (DecoderDataException e) {
                                KL.i("BUFFER is not enough, wait for bytes = "
                                        + e.getExpectBytes());
                                mRequestBytes = e.getExpectBytes();
//                                if (mOnBufferingListener != null) {
//                                    mOnBufferingListener
//                                            .onBuffering(KaolaMediaPlayer.this);
//                                }
                                notifyBuffering(KaolaMediaPlayer.this);
                                break;
                            }
                            if (data == null) {
                                KL.i("Got error! Decoder");
                                sendEmptyMessage(MSG_PLAY_ERROR_BUFFER);
                                break;
                            }
                            if (mPaused)
                                break;
//                            if (mOnProgressUpdateListener != null) {
//                                mOnProgressUpdateListener.onProgress(
//                                        KaolaMediaPlayer.this, data.getTimestamp(), 0);
//                            }
                            notifyProgressUpdate(KaolaMediaPlayer.this, data.getTimestamp(), 0);
                            int samples = data.getSampleCount();
                            while (!mPaused && mPlayOffset < samples) {
                                int t = mPlayOffset + FIXED_BUFFER_SIZE;
                                if (t > samples) {
                                    t = samples;
                                }
                                int input = t - mPlayOffset;
                                mAudioTrack.write(data.getData(), mPlayOffset,
                                        input);
                                mPlayOffset += FIXED_BUFFER_SIZE;
                            }
                            if (mPlayOffset >= samples) {
                                mBufferedData = null;
                                mPlayOffset = 0;
                            }
                            // LogUtil.Log(TAG, "MSG_PLAY_NEXT_BUFFER Done");
                            if (data.isEndofStream()) {
                                KL.i("Player reached end");
                                sendEmptyMessage(MSG_PLAY_COMPLETE);
                                break;
                            }
                            if (!mPaused) {
                                sendEmptyMessage(MSG_PLAY_NEXT_BUFFER);
                            }
                            break;
                        case MSG_PLAY_ERROR_BUFFER:
                            KL.i("MSG_PLAY_ERROR");
                            mState = MediaState.STATE_ERROR;
//                            if (mOnErrorListener != null) {
//                                mOnErrorListener.onError(KaolaMediaPlayer.this, 1, 0);
//                            }
                            notifyError(KaolaMediaPlayer.this, 1, 0);
                            break;
                        case MSG_PLAY_PAUSE:
                            KL.i("MSG_PLAY_PAUSE {}", mPaused);
                            if (hasMessages(MSG_PLAY_NEXT_BUFFER)) {
                                removeMessages(MSG_PLAY_NEXT_BUFFER);
                            }
                            mPaused = true;
//                            if (mOnPausedCompletedListener != null) {
//                                mOnPausedCompletedListener
//                                        .onPaused(KaolaMediaPlayer.this);
//                            }
                            notifyPausedComplete(KaolaMediaPlayer.this);
                            break;
                        case MSG_PLAY_PLAY:
                            KL.i("MSG_PLAY_PLAY {}", mPaused);
                            mPaused = false;
                            if (!mPaused) { // && mOnPlaybackStartListener != null
//                                mOnPlaybackStartListener.onPlaybackStart(
//                                        KaolaMediaPlayer.this, getCurrentPosition());
                                notifyPlaybackStart(
                                        KaolaMediaPlayer.this, getCurrentPosition());
                            }
                            sendEmptyMessage(MSG_PLAY_NEXT_BUFFER);
                            break;
                        case MSG_PLAY_STOP:
                            KL.i("MSG_PLAY_STOP {}", mPaused);
                            mPaused = true;
                            if (hasMessages(MSG_PLAY_NEXT_BUFFER)) {
                                removeMessages(MSG_PLAY_NEXT_BUFFER);
                            }
                            synchronized (mAudioTrack) {
                                mAudioTrack.stop();
                            }
                            if (mSeekThread != null) {
                                mSeekThread.interrupt();
                                mSeekThread = null;
                            }
                            if (mDecoder != null) {
                                mDecoder.reset();
                            }
                            if (hasMessages(MSG_PLAY_NEXT_BUFFER)) {
                                removeMessages(MSG_PLAY_NEXT_BUFFER);
                            }
//                            if (mOnStoppedCompleteListener != null) {
//                                mOnStoppedCompleteListener
//                                        .onStopped(KaolaMediaPlayer.this);
//                            }
                            notifyStoppedComplete(KaolaMediaPlayer.this);
                            break;
                        case MSG_PLAY_COMPLETE:
                            KL.i("MSG_PLAY_COMPLETE");
//                            if (mOnCompletedListener != null) {
//                                mOnCompletedListener
//                                        .onCompleted(KaolaMediaPlayer.this);
//                            }
                            notifyPlayComplete(KaolaMediaPlayer.this);
                            break;
                        case MSG_PLAY_RESET:
                            KL.i("MSG_PLAY_RESET");
                            if (mSeekThread != null) {
                                mSeekThread.interrupt();
                                mSeekThread = null;
                            }
                            if (mThread != null) {
                                mThread.interrupt();
                                mThread = null;
                            }
                            break;
                        case MSG_PLAY_RELEASE:
                            KL.i("MSG_PLAY_RELEASE");
                            removeMessages(MSG_PLAY_PLAY);
                            removeMessages(MSG_PLAY_STOP);
                            removeMessages(MSG_PLAY_NEXT_BUFFER);
                            removeMessages(MSG_PLAY_RESET);
                            if (mDecoder != null) {
                                mDecoder.release();
                                mDecoder = null;
                            }
                            synchronized (mAudioTrack) {
                                mAudioTrack.release();
                            }
                            mAudioTrack = null;
                            mAudioBufferProcessor.getLooper().quit();
                            KL.d("release opus player----------->start");
                            break;
                        case MSG_PLAY_SETSOURCE:
                            isPreDownloadComplete = false;
                            flushData();
                            if (mSeekThread != null) {
                                mSeekThread.interrupt();
                                mSeekThread = null;
                            }
                            if (mDecoder != null) {
                                mDecoder.release();
                                mDecoder = null;
                            }
                            mMediaFile = null;
                            mRequestBytes = -1;
                            mDecoder = new OpusDecoder();
                            String source = (String) msg.obj;
                            KL.i("MSG_PLAY_SETSOURCE {}", source);
                            try {
                                if (source.startsWith("http://")) {
                                    mMediaFile = MediaCacheFiles.getMediaFile(
                                            mContext, source);
                                    if (mMediaFile != null
                                            && mMediaFile.getCompletedBytes() == mMediaFile
                                            .length()
                                            && !TextUtils.isEmpty(mMediaFile
                                            .getLocalFilePath())) {
                                        File file = new File(
                                                mMediaFile.getLocalFilePath());
                                        if (file.exists()) { // && mOnLoadFileFromLocalListener != null
//                                            mOnLoadFileFromLocalListener
//                                                    .onLoadFileFromLocal(true);
                                            isPreDownloadComplete = true;
                                            notifyLoadFileFromLocal(true);
                                        }
                                    }
                                    mIsStreaming = true;
                                } else {
                                    mMediaFile = new MediaFile(mContext, source);
                                    mIsStreaming = false;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                mState = MediaState.STATE_ERROR;
                            }
                            mDecoder.setup(mMediaFile);
                            break;
                        case MSG_PLAY_PREPARE:
                            if (mState == MediaState.STATE_ERROR) {
                                sendEmptyMessage(MSG_PLAY_ERROR_BUFFER);
                                break;
                            }
                            mPaused = false;
                            try {
                                if (mIsStreaming) {
                                    mState = MediaState.STATE_PREPARING;
                                    if (mMediaFile.parsed()) {
                                        Message msg1 = Message.obtain(mHandler,
                                                MSG_FETCH_HEADER_DONE, mMediaFile);
                                        mHandler.sendMessage(msg1);
                                        break;
                                    }
                                    new HeaderFetcherThread(mMediaFile).start();
                                    KL.i("Start to fetch header");
                                } else {
                                    if (mDecoder.prepare()) {
                                        mState = MediaState.STATE_PREPARED;
//                                        if (mOnPrepareCompleteListener != null)
//                                            mOnPrepareCompleteListener
//                                                    .onPrepareComplete(KaolaMediaPlayer.this);
                                        notifyPrepareComplete();
                                    } else {
                                        mState = MediaState.STATE_ERROR;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                mState = MediaState.STATE_ERROR;
                            }
                            if (mState == MediaState.STATE_ERROR
                                    ) { //&& mOnErrorListener != null
//                                mOnErrorListener.onError(KaolaMediaPlayer.this, 0, 0);
                                notifyError(KaolaMediaPlayer.this, 0, 0);
                            }
                            break;
                        case MSG_PLAY_SEEK_COMPLETE:
                            KL.i("MSG_PLAY_SEEK_COMPLETE");
                            mSeekThread = null;
                            if (mState == MediaState.STATE_SEEKING) {
                                mState = MediaState.STATE_SEEKDONE;
//                                if (mOnSeekCompleteListener != null) {
//                                    mOnSeekCompleteListener
//                                            .onSeekComplete(KaolaMediaPlayer.this);
//                                }
                                notifySeekComplete(KaolaMediaPlayer.this);
                            }
                            break;
                        case MSG_PLAY_START_SEEK:
                            flushData();
                            if (mSeekThread != null) {
                                mSeekThread.interrupt();
                                mSeekThread = null;
                            }
                            mSeekThread = new SeekThread(mDecoder, mMediaFile,
                                    msg.arg1, null);
                            mSeekThread.start();
                            break;
                        default:
                            break;
                    }
                }
            };
            Looper.loop();
        }

    }

    public void setDataSource(String source) {
        if (mState == MediaState.STATE_IDLE && source != null) {
            Message msg = Message.obtain();
            msg.what = PlayerThread.MSG_PLAY_SETSOURCE;
            msg.obj = source;
            mUrl = source;
            if (canPlayerThreadSendMessage()) {
                mPlayerThread.mAudioBufferProcessor.sendMessage(msg);
            }
            mState = MediaState.STATE_INITIALIZED;
        } else {
            KL.e("cannot setDataSource in state = {}", mState);
        }
    }

    public void play() {
        if (mState == MediaState.STATE_PREPARED
                || mState == MediaState.STATE_PAUSED
                || mState == MediaState.STATE_STOPPED
                || mState == MediaState.STATE_SEEKDONE) {
            synchronized (mAudioTrack) {
                mAudioTrack.play();
            }
            if (canPlayerThreadSendMessage()) {
                mPlayerThread.mAudioBufferProcessor
                        .sendEmptyMessage(PlayerThread.MSG_PLAY_PLAY);
            }
            mState = MediaState.STATE_PLAYING;
        } else {
            KL.e("cannot play in state = {}", mState);
        }
    }

    /**
     * 检测播放线程是否可以发送消息
     *
     * @return true为可以，false为不可以
     */
    private boolean canPlayerThreadSendMessage() {
        Looper looper = mPlayerThread.mAudioBufferProcessor.getLooper();
        if (looper == null) {
            return false;
        }
        Thread thread = looper.getThread();
        if (thread == null) {
            return false;
        }
        boolean isAlive = thread.isAlive();
//        KL.d("canPlayerThreadSendMessage-------->isAlive = {}", isAlive);
        return isAlive;
    }

    public void pause() {
        if (mState == MediaState.STATE_PLAYING
                || mState == MediaState.STATE_SEEKDONE) {
            mState = MediaState.STATE_PAUSED;
            if (canPlayerThreadSendMessage()) {
                mPlayerThread.mAudioBufferProcessor
                        .sendEmptyMessage(PlayerThread.MSG_PLAY_PAUSE);
            }
            synchronized (mAudioTrack) {
                mAudioTrack.pause();
            }
        } else {
            KL.e("cannot pause in state = {}", mState);
        }
    }

    public synchronized void stop() {
        mPlayerThread.pauseImmediately();
        if (mState == MediaState.STATE_PREPARING) {
            mPlayerThread.pauseImmediately();
//            if (mOnPausedCompletedListener != null) {
//                mOnPausedCompletedListener
//                        .onPaused(KaolaMediaPlayer.this);
//            }
            notifyPausedComplete(KaolaMediaPlayer.this);
            return;
        }
        if (mState == MediaState.STATE_PLAYING
                || mState == MediaState.STATE_PAUSED) {
            mState = MediaState.STATE_STOPPED;
            mPlayerThread.pauseImmediately();
            flushData();
            if (canPlayerThreadSendMessage()) {
                mPlayerThread.mAudioBufferProcessor
                        .sendEmptyMessage(PlayerThread.MSG_PLAY_STOP);
            }
        } else {
            KL.e("cannot stop in state = {}", mState);
        }
    }

    @Override
    public boolean isPreDownloadComplete() {
        return isPreDownloadComplete;
    }

    public void preload(String url) {
        MediaFile file = MediaCacheFiles.getMediaFile(mContext, url);
        if (file != null && !file.parsed()) {
            new HeaderFetcherThread(file).start();
        }
    }

    public void reset() {
        try {
            KL.i("reset player");
            stop();
        } catch (Throwable e) {
            KL.e(e.toString(), e);
        }
        if (canPlayerThreadSendMessage()) {
            mPlayerThread.mAudioBufferProcessor
                    .sendEmptyMessage(PlayerThread.MSG_PLAY_RESET);
        }
        mState = MediaState.STATE_IDLE;
    }

    public void release() {
        KL.d("release player-------opus----->start");
        reset();
        if (canPlayerThreadSendMessage()) {
            mPlayerThread.mAudioBufferProcessor
                    .sendEmptyMessage(PlayerThread.MSG_PLAY_RELEASE);
        }
        mState = MediaState.STATE_RELEASED;
        releaseListeners();
    }

    private void flushData() {
        mAudioTrack.flush();
        mBufferedData = null;
        mPlayOffset = 0;
    }

    public void prepare() {
        if (mState == MediaState.STATE_INITIALIZED) {
            mState = MediaState.STATE_PREPARING;
            if (canPlayerThreadSendMessage()) {
                mPlayerThread.mAudioBufferProcessor
                        .sendEmptyMessage(PlayerThread.MSG_PLAY_PREPARE);
            }
        } else {
            KL.e("cannot prepare in state = {}", mState);
        }
    }

    @Override
    public long getDuration() {
        return 0;
    }

    public long getCurrentPosition() {
        if (mDecoder != null) {
            return (int) mDecoder.getCurrentPosition();
        }
        return 0;
    }

    public boolean isPaused() {
        return mState == MediaState.STATE_PAUSED;
    }

    @Override
    public boolean isPlaying() {
        return mState == MediaState.STATE_PLAYING;
    }

    private HttpURLConnection createConnection(String address)
            throws IOException {
        URL url = new URL(address);
        HttpURLConnection connection = null;
        KL.d("---------->getUnicomProxyConnection un = "
                + url.toString());
        connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIME_OUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestProperty("User-Agent", "NetFox");
        connection.setRequestProperty("Accept-Encoding", "identity");
        return connection;
    }

    /**
     * 单次seek是否已经执行完毕 true为是，false为否
     */
    private static boolean bSeekComplete = true;

    public synchronized void seekTo(long msec) {
        if (!bSeekComplete) {
            return;
        }
        if (mState == MediaState.STATE_PLAYING
                || mState == MediaState.STATE_PAUSED
                || mState == MediaState.STATE_PREPARED
                || mState == MediaState.STATE_SEEKING) {
            bSeekComplete = false;
            mState = MediaState.STATE_SEEKING;
            mPlayerThread.pauseImmediately();
            KL.i("seekTo = {}", msec);
            mSeekTries++;
            synchronized (mSeekTag) {
                Message msg = Message.obtain(null,
                        PlayerThread.MSG_PLAY_START_SEEK);
                msg.arg1 = (int) msec;
                if (canPlayerThreadSendMessage()) {
                    mPlayerThread.mAudioBufferProcessor.sendMessage(msg);
                }
            }
        } else {
            KL.i("cannot seekTo in state = {}", mState);
        }
    }

    private SeekThread mSeekThread = null;
    private Object mSeekTag = new Object();
    private int mSeekTries = 0;

    private class SeekThread extends Thread {
        private KaolaDecoder mSeekDecoder;
        private MediaFile mSeekFile;
        private long mSeekTarget;
        private MediaState mPreviousState;

        public SeekThread(KaolaDecoder decoder, MediaFile file, long msec,
                          MediaState state) {
            mSeekDecoder = decoder;
            mSeekFile = file;
            mSeekTarget = msec;
            mPreviousState = state;
        }

        public void run() {
            synchronized (mSeekTag) {
//                long tm = System.currentTimeMillis();
                mSeekDecoder.startSeek(mSeekTarget);
                boolean request = false;
                while (!mSeekDecoder.isSeekComplete()) {
                    if (isInterrupted()) {
                        break;
                    }
                    boolean ret = mSeekDecoder.processSeek();
                    if (!ret && !isInterrupted()) {
                        request = true;
                        int t = mSeekDecoder.getSeekOffset();
                        int r = mSeekDecoder.getSeekRange();
                        int offset = mSeekFile.findNextMissedPosition(t);
                        int range = t - offset + r;
                        if (range > 0) {
                            range = mSeekFile.alignRangeBytes(offset, range);
                        }
                        if (mThread != null) {
                            mThread.interrupt();
                            mThread = null;
                        }
//                        KL.i(
//                                "Seek requires range from pos = {} range = {}",
//                                offset, range);
                        HttpURLConnection connection = null;
                        InputStream inputStream = null;
                        try {
                            connection = createConnection(mSeekFile.getUrl());
                            String sProperty = null;

                            int startRange = offset;
                            int endRange = offset + range;
                            if (startRange > endRange) {
                                startRange = endRange;
                            }
                            sProperty = "bytes=" + startRange + "-" + endRange;
//                            KL.d("seek-------------> startRange = {}, endRange = {}",
//                                    startRange, endRange);
                            connection.setRequestProperty("RANGE", sProperty);
                            int responseCode = connection.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                                inputStream = new BufferedInputStream(
                                        connection.getInputStream());
                                byte[] data = new byte[BUFFER_SIZE];
                                int readBytes = 0;
                                int pos = offset;
                                while (!isInterrupted()
                                        && (readBytes = inputStream.read(data, 0,
                                        BUFFER_SIZE)) != -1) {
                                    mSeekFile.write(data, 0, readBytes, pos);
                                    pos += readBytes;
//                                    if (mOnDownloadProgressListener != null) {
//                                        mOnDownloadProgressListener
//                                                .onDownloadProgress(
//                                                        mSeekFile.getCompletedBytes(),
//                                                        mSeekFile.length());
//                                    }
                                    notifyDownloadProgress(mSeekFile.getCompletedBytes(),
                                            mSeekFile.length());
                                }
                                mSeekFile.markChunks(offset, pos - offset);
                            }
                        } catch (MalformedURLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } finally {
                            if (connection != null)
                                connection.disconnect();
                        }
                        if (isInterrupted()) {
                            break;
                        }
                    }
                }
                int pos = mSeekDecoder.endSeek();
                mSeekTries--;
                if (mMediaFile == mSeekFile && mSeekTries <= 0) {
                    Message msg = Message.obtain(null,
                            PlayerThread.MSG_PLAY_SEEK_COMPLETE, mPreviousState);
                    if (canPlayerThreadSendMessage()) {
                        mPlayerThread.mAudioBufferProcessor.sendMessage(msg);
                    }
                    if (mThread == null && request) {
                        mThread = new Thread(new RequestRunnable(mSeekFile,
                                mSeekFile.findNextMissedPosition(pos),
                                mSeekFile.findNextMissedRange(pos)));
                        mThread.start();
                    }
                }
                bSeekComplete = true;
//                if (logger.isInfoEnabled())
//                    KL.i("Seek costs = {}",
//                            (System.currentTimeMillis() - tm));
            }
        }
    }

    private Thread mThread = null;
    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            MediaFile file = null;
            if (mMediaFile == null) {
                return;
            }
            switch (msg.what) {
                case MSG_FETCH_HEADER_DONE:
                    file = (MediaFile) msg.obj;
                    KL.i("Fetch header is done, input file = {}",
                            file.getUrl());
                    if (mMediaFile == null) {
                        KL.i("mMediaFile == null");
                        break;
                    }
                    KL.i("Fetch header is done, current file = {}",
                            mMediaFile.getUrl());
                    if (mState == MediaState.STATE_PREPARING && mMediaFile == file) {
                        if (mDecoder != null && mDecoder.prepare()) {
                            mState = MediaState.STATE_PREPARED;
                            mThread = new Thread(new RequestRunnable(mMediaFile));
                            mThread.start();
//                            if (mOnPrepareCompleteListener != null) {
//                                mOnPrepareCompleteListener
//                                        .onPrepareComplete(KaolaMediaPlayer.this);
//                            }
                            notifyPrepareComplete();
                        } else {
                            mState = MediaState.STATE_ERROR;
//                            if (mOnErrorListener != null)
//                                mOnErrorListener.onError(KaolaMediaPlayer.this, 0, 0);
                            notifyError(KaolaMediaPlayer.this, 0, 0);
                        }
                    }
                    break;
                case MSG_FETCH_HEADER_ERROR:
                    file = (MediaFile) msg.obj;
                    KL.i("Fetch header error occurs, input file = {}",
                            file.getUrl());
                    if (mState == MediaState.STATE_PREPARING && mMediaFile == file) {
                        new HeaderFetcherThread(mMediaFile).start();
                    }
                    break;
                case MSG_DOWNLOAD_CHUNK_ERROR:
                    file = (MediaFile) msg.obj;
                    if (mMediaFile == file) {
                        removeMessages(MSG_DOWNLOAD_CHUNK_ERROR);
                        KL.i(
                                "Resume a new thread to start download url = {}",
                                mMediaFile.getUrl());
                        if (mThread != null) {
                            mThread.interrupt();
                            mThread = null;
                        }
                        int offset = msg.arg1;
                        mThread = new Thread(new RequestRunnable(mMediaFile,
                                mMediaFile.findNextMissedPosition(offset),
                                mMediaFile.findNextMissedRange(offset)));
                        mThread.start();
                    }
                    break;
                case MSG_DOWNLOAD_CHUNK_DONE:
                    file = (MediaFile) msg.obj;
                    if (mMediaFile == file) {
                        removeMessages(MSG_DOWNLOAD_CHUNK_DONE);
                        if (mThread != null) {
                            mThread.interrupt();
                        }
                        mThread = new Thread(new RequestRunnable(file));
                        mThread.start();
                    }
                    break;
                case MSG_BUFFER_RESUME:
                    KL.i("Buffer resumed");
                    file = (MediaFile) msg.obj;
                    if (mMediaFile == file) {
                        mRequestBytes = -1;
                        if (mState == MediaState.STATE_PLAYING && canPlayerThreadSendMessage()) {
                            mPlayerThread.mAudioBufferProcessor
                                    .sendEmptyMessage(PlayerThread.MSG_PLAY_PLAY);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    // private static final String DOWNLOAD_ACTION = "action";

    // private void sendBroadcast(boolean active) {
    // ???
    // Intent intent = new
    // Intent(HomeBroadcastReceiver.ACTION_PLAYER_BUFFER_SUCCESS);
    // intent.putExtra(DOWNLOAD_ACTION, active);
    // mContext.sendBroadcast(intent);
    // }

    private class RequestRunnable implements Runnable {

        private int mStartOffset;
        private int mRange;
        private MediaFile mFile = null;

        public RequestRunnable(MediaFile file) {
            mFile = file;
            if (file != null) {
                mStartOffset = file.findNextMissedPosition(0);
                mRange = file.findNextMissedRange(mStartOffset);
            }
        }

        public RequestRunnable(MediaFile file, int startOffset, int range) {
            mFile = file;
            mStartOffset = file.findNextMissedPosition(startOffset);
            mRange = startOffset + range - mStartOffset;
            if (mRange > 0) {
                mRange = mFile.alignRangeBytes(mStartOffset, mRange);
            }
        }

        @Override
        public void run() {
            boolean error = true;
            KL.i("Request body with file = {}", mFile);
            int sum = 0;
            if (mFile != null) {
                if (mRange <= 0) {
                    KL.i("url = {} range is 0, stop downloading",
                            mFile.getUrl());
                    return;
                }

                // sendBroadcast(true);
                HttpURLConnection connection = null;
                InputStream iStream = null;
                try {
                    connection = createConnection(mFile.getUrl());
                    int startRange = mStartOffset;
                    int endRange = mStartOffset + mRange - 1;
                    if (startRange > endRange) {
                        startRange = endRange;
                    }
                    String sProperty = "bytes=" + startRange + "-" + endRange;
//                    KL.d("seek---------------> sProperty = {}",
//                            sProperty);
                    connection.setRequestProperty("RANGE", sProperty);

                    /** 处理URL重定向逻辑*/
                    int redirectCount = 0;
                    int responseCode = 0;
                    /** 处理URL重定向逻辑*/
                    responseCode = connection.getResponseCode();
                    while ((responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                            responseCode == HttpURLConnection.HTTP_MOVED_PERM) && redirectCount < MAX_REDIRECT_COUNT) {
                        connection = createConnection(connection.getHeaderField("Location"));
                        redirectCount++;
                        responseCode = connection.getResponseCode();
                    }

//                    responseCode = connection.getResponseCode();
//                    logger
//                            .info(
//                                    "Start to request url = {} offset = {} range = {} response = {}",
//                                    mFile.getUrl(), mStartOffset, mRange, responseCode);
                    if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                        iStream = new BufferedInputStream(
                                connection.getInputStream());
                        byte[] data = new byte[BUFFER_SIZE];
                        int readBytes = 0;
                        int tempOffset = mStartOffset;
                        int tempBytes = 0;
                        while ((readBytes = iStream.read(data, 0, BUFFER_SIZE)) != -1
                                && !Thread.interrupted()) {
                            mFile.write(data, 0, readBytes, sum + mStartOffset);
                            tempBytes += readBytes;
                            int bytesLeft = mFile.markChunks(tempOffset,
                                    tempBytes);
                            tempOffset += (tempBytes - bytesLeft);
                            tempBytes = bytesLeft;
                            sum += readBytes;
                            if (mRequestBytes != -1
                                    && sum + mStartOffset > mRequestBytes) {
                                Message msg = Message.obtain(mHandler,
                                        MSG_BUFFER_RESUME, mFile);
                                mHandler.sendMessage(msg);
                            }
//                            if (mOnDownloadProgressListener != null) {
//                                mOnDownloadProgressListener.onDownloadProgress(
//                                        mFile.getCompletedBytes(), mFile.length());
//                            }
                            notifyDownloadProgress(mFile.getCompletedBytes(), mFile.length());
                        }
                        isPreDownloadComplete = mFile.getCompletedBytes() == mFile.length();
                        if (sum >= mRange) {
                            error = false;
                        }
                    }
                    if (responseCode == 416) {
                        error = false;
                    }
//                    if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
//                        if (mOnErrorListener != null) {
//                            mOnErrorListener.onError(KaolaMediaPlayer.this, 1);
//                        }
//                        return;
//                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (InterruptedIOException interruptedIOException) {
                    interruptedIOException.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if (iStream != null) {
                        try {
                            iStream.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if (connection != null)
                        connection.disconnect();
                }
            }
            if (error && !Thread.interrupted()) {
                Message msg = Message.obtain(mHandler,
                        MSG_DOWNLOAD_CHUNK_ERROR, mFile);
                msg.arg1 = mStartOffset;
                mHandler.sendMessageDelayed(msg, 3000);
            } else {
                KL.i("Download file = {} completed bytes = {}",
                        mFile.getUrl(), sum);
                Message msg = Message.obtain(mHandler, MSG_DOWNLOAD_CHUNK_DONE,
                        mFile);
                mHandler.sendMessageDelayed(msg, 500);
            }
            // sendBroadcast(false);
        }
    }

    private class HeaderFetcherThread extends Thread {
        private MediaFile mFile = null;

        public HeaderFetcherThread(MediaFile file) {
            super();
            mFile = file;
        }

        public void run() {
            boolean error = true;
            KL.i("New a header fetcher thread with file = {}", mFile);
            if (mFile != null) {
                HttpURLConnection connection = null;
                InputStream iStream = null;
                try {
                    connection = createConnection(mFile.getUrl());

                    int redirectCount = 0;
                    int responseCode = 0;
                    /** 处理URL重定向逻辑*/
                    responseCode = connection.getResponseCode();
                    while ((responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                            responseCode == HttpURLConnection.HTTP_MOVED_PERM) && redirectCount < MAX_REDIRECT_COUNT) {
                        connection = createConnection(connection.getHeaderField("Location"));
                        redirectCount++;
                        responseCode = connection.getResponseCode();
                    }

//                    responseCode = connection.getResponseCode();
                    long contentLength = connection.getContentLength();
                    KL.i("File = {} header response = {} length = {}",
                            mFile, responseCode, contentLength);
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        mFile.setLength(contentLength);
                        iStream = new BufferedInputStream(
                                connection.getInputStream());
                        int headerCount = 1024 * 64;
                        byte[] data = new byte[BUFFER_SIZE];
                        int readBytes = 0;
                        int sum = 0;
                        while (!isInterrupted()
                                && (readBytes = iStream.read(data, 0, BUFFER_SIZE)) != -1) {
                            if (sum >= headerCount) {
                                break;
                            }
                            mFile.write(data, 0, readBytes, sum);
                            sum += readBytes;
                        }
                        error = false;
                    }

//                    if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
//                        if (mOnErrorListener != null) {
//                            mOnErrorListener.onError(KaolaMediaPlayer.this, 1);
//                        }
//                        return;
//                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (mFile == mMediaFile && !error) {
                        Message msg = Message.obtain(mHandler,
                                MSG_FETCH_HEADER_DONE, mFile);
                        mHandler.sendMessage(msg);
                    }
                    if (iStream != null) {
                        try {
                            iStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null)
                        connection.disconnect();
                }
            }
            if (mFile == mMediaFile) {
                if (error) {
                    Message msg = Message.obtain(mHandler,
                            MSG_FETCH_HEADER_ERROR, mFile);
                    mHandler.sendMessageDelayed(msg, 3000);
                }
            }
        }
    }

}
