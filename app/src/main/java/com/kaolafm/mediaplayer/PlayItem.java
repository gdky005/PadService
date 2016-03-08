package com.kaolafm.mediaplayer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 碎片信息类
 */
public class PlayItem implements Parcelable {

    public static final int NOT_LIKED = 0;
    public static final int HAS_LIKED = 1;
    /**
     * 台宣类型
     */
    public static final int CTG_TYPE_TX = 131;
    /**
     * 报时类型
     */
    public static final int CTG_TYPE_BS = 132;
    /**
     * 碎片ID
     */
    private long audioId;
    /**
     * 碎片名称
     */
    private String title;
    /**
     * 播放地址
     */
    private String playUrl;
    /**
     * 离线url
     */
    private String offlineUrl;
    /**
     * 是否为离线
     */
    private boolean isOffline;
    /**
     * 离线地址
     */
    private String offlinePlayUrl;
    /**
     * 当前播放位置
     */
    private int position;
    /**
     * 时长
     */
    private int duration;
    private String audioDes;
    private long albumId;
    private String albumPic;
    private String albumOfflinePic;
    private String albumName;
    private long orderNum;
    private String mp3PlayUrl;
    private String m3u8PlayUrl;
    private String shareUrl;
    /**
     * 131台宣，
     */
    private long categoryId;
    private String hosts;
    private long fileSize;
    private int isLiked;
    private String updateTime;
    private long createTime;
    private String clockId;
    private boolean isInterrupted = false;

    /**
     * 默认来源
     */
    public static final int DATA_SRC_DEFAULT = 0;
    /**
     * 来源于历史记录
     */
    public static final int DATA_SRC_HISTORY = 1;
    /**
     * 当前播单对象来源
     */
    private int dataSrc;
    /**
     * 是否是直播中url
     * added by GuoPei
     */
    private boolean isLivingUrl;

    /**
     * LiveData所需
     * 字段
     */
//    //回放url
//    private String backLiveUrl;
    //聊天室id
    private long liveId;
    //直播碎片图片
    private String audioPic;
//    public PlayItemType type;


//    public enum PlayItemType {
//        //默认
//        DEFAULT,
//        //直播回放
//        LIVE_PLAYBACK,
//        //直播编辑试听
//        LIVE_AUDITION_EDIT,
//        //直播工作间试听
//        LIVE_AUDITION,
//        //私人直播
//        LIVING,
//        //广播直播
//        BROADCAST_LIVING,
//        //广播回放
//        BROADCAST_PLAYBACK
//    }


    public long getLiveId() {
        return liveId;
    }

    public void setLiveId(long liveId) {
        this.liveId = liveId;
    }

    public void setAudioId(long audioId) {
        this.audioId = audioId;
    }

    public long getAudioId() {
        return audioId;
    }

    public String getAudioPic() {
        return audioPic;
    }

    public void setAudioPic(String audioPic) {
        this.audioPic = audioPic;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setIsOffline(boolean isOffline) {
        this.isOffline = isOffline;
    }

    public boolean getIsOffline() {
        return isOffline;
    }

    public void setOfflinePlayUrl(String offlinePlayUrl) {
        this.offlinePlayUrl = offlinePlayUrl;
    }

    public String getOfflinePlayUrl() {
        return offlinePlayUrl;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setAudioDes(String audioDes) {
        this.audioDes = audioDes;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public void setAlbumPic(String albumPic) {
        this.albumPic = albumPic;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setOrderNum(long orderNum) {
        this.orderNum = orderNum;
    }

    public void setMp3PlayUrl(String mp3PlayUrl) {
        this.mp3PlayUrl = mp3PlayUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setIsLiked(int isLiked) {
        this.isLiked = isLiked;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public void setClockId(String clockId) {
        this.clockId = clockId;
    }

    public String getAudioDes() {
        return audioDes;
    }

    public long getAlbumId() {
        return albumId;
    }

    public String getAlbumPic() {
        return albumPic;
    }

    public String getAlbumName() {
        return albumName;
    }

    public long getOrderNum() {
        return orderNum;
    }

    public String getMp3PlayUrl() {
        return mp3PlayUrl;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public String getHosts() {
        return hosts;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getIsLiked() {
        return isLiked;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getClockId() {
        return clockId;
    }


    public void setIsInterrupted(boolean isInterrupted) {
        this.isInterrupted = isInterrupted;
    }

    public boolean getIsInterrupted() {
        return this.isInterrupted;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getDataSrc() {
        return dataSrc;
    }

    public void setDataSrc(int dataSrc) {
        this.dataSrc = dataSrc;
    }

    public String getAlbumOfflinePic() {
        return albumOfflinePic;
    }

    public String getM3u8PlayUrl() {
        return m3u8PlayUrl;
    }

    public void setM3u8PlayUrl(String m3u8PlayUrl) {
        this.m3u8PlayUrl = m3u8PlayUrl;
    }

    public void setAlbumOfflinePic(String albumOfflinePic) {
        this.albumOfflinePic = albumOfflinePic;
    }

    public boolean isLivingUrl() {
        return isLivingUrl;
    }

    public void setIsLivingUrl(boolean isLivingUrl) {
        this.isLivingUrl = isLivingUrl;
    }

    public String getOfflineUrl() {
        return offlineUrl;
    }

    public void setOfflineUrl(String offlineUrl) {
        this.offlineUrl = offlineUrl;
    }



//    /**
//     * PlayItem类型转化为直播数据类型
//     *
//     * @param livePlayItem PlayItem类型的数据
//     * @return
//     */
//    public static PlayItem translateToLiveData(@NonNull PlayItem livePlayItem) {
//        if(null == livePlayItem){
//            return null;
//        }
//        LiveData liveData = new LiveData();
//        liveData.setAlbumId(livePlayItem.albumId);
//        liveData.setLiveName(livePlayItem.albumName);
//        liveData.setLivePic(livePlayItem.albumPic);
//
//        liveData.setProgramName(livePlayItem.title);
//        liveData.setProgramDesc(livePlayItem.audioDes);
//    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.audioId);
        dest.writeString(this.title);
        dest.writeString(this.playUrl);
        dest.writeString(this.offlineUrl);
        dest.writeByte(isOffline ? (byte) 1 : (byte) 0);
        dest.writeString(this.offlinePlayUrl);
        dest.writeInt(this.position);
        dest.writeInt(this.duration);
        dest.writeString(this.audioDes);
        dest.writeLong(this.albumId);
        dest.writeString(this.albumPic);
        dest.writeString(this.albumName);
        dest.writeLong(this.orderNum);
        dest.writeString(this.mp3PlayUrl);
        dest.writeString(this.shareUrl);
        dest.writeLong(this.categoryId);
        dest.writeString(this.hosts);
        dest.writeLong(this.fileSize);
        dest.writeInt(this.isLiked);
        dest.writeString(this.updateTime);
        dest.writeLong(this.createTime);
        dest.writeString(this.clockId);
        dest.writeByte(isInterrupted ? (byte) 1 : (byte) 0);
        dest.writeString(this.offlinePlayUrl);
    }

    public PlayItem() {
    }

    private PlayItem(Parcel in) {
        this.audioId = in.readLong();
        this.title = in.readString();
        this.playUrl = in.readString();
        this.offlineUrl = in.readString();
        this.isOffline = in.readByte() != 0;
        this.offlinePlayUrl = in.readString();
        this.position = in.readInt();
        this.duration = in.readInt();
        this.audioDes = in.readString();
        this.albumId = in.readLong();
        this.albumPic = in.readString();
        this.albumName = in.readString();
        this.orderNum = in.readLong();
        this.mp3PlayUrl = in.readString();
        this.shareUrl = in.readString();
        this.categoryId = in.readLong();
        this.hosts = in.readString();
        this.fileSize = in.readLong();
        this.isLiked = in.readInt();
        this.updateTime = in.readString();
        this.createTime = in.readLong();
        this.clockId = in.readString();
        this.isInterrupted = in.readByte() != 0;
        this.offlinePlayUrl = in.readString();
    }

    public static final Parcelable.Creator<PlayItem> CREATOR = new Parcelable.Creator<PlayItem>() {
        public PlayItem createFromParcel(Parcel source) {
            return new PlayItem(source);
        }

        public PlayItem[] newArray(int size) {
            return new PlayItem[size];
        }
    };
}
