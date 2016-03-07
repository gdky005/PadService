package com.kaolafm.mediaplayer;

/**
 * Created by kaolafm on 2015/11/9.
 */
public abstract class AbsM3U8MediaPlayer extends AbsMediaPlayer {
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_BUFFERING_END = 702;

    public abstract boolean isPlaying();
}
