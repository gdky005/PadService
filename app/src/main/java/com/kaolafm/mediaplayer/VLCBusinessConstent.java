/**
 * Copyright:   Copyright (c) 2012
 * Company:     北京车语文化传媒有限公司
 * Department:  数字媒体部
 */
package com.kaolafm.mediaplayer;

/**
 * 播放Service和数据Service交互所用到的常量
 *
 * @author <a href="mailto:xiajl@autoradio.cn">夏俊岭</a>
 * @version 2.0.0 2013
 * @since 2.1
 */
public interface VLCBusinessConstent {

    /**
     * key  定义消息传送所需的ＫＥＹ
     */
    public static final String KEY_THEURL = "KEY_THEURL";

    public static final String KEY_TITLE = "KEY_TITLE";
    public static final String KEY_THEPRELOADURL = "KEY_THEPROLOADURL";
    //    public static final String KEY_BUFFER_PERCENT_I = "KEY_BUFFER_PERCENT_I";
    public static final String KEY_PROGRESSUPDATE_TIME_I = "KEY_PROGRESSUPDATE_TIME_I";
    public static final String KEY_COMPLETION_LENGTH_F = "KEY_COMPLETION_LENGTH_F";
    public static final String KEY_COMPLETION_PERCENT_I = "KEY_COMPLETION_PERCENT_I";
    String KEY_IS_PRE_DOWNLOAD_COMPLETION = "KEY_IS_PRE_DOWNLOAD_COMPLETION";
    public static final String KEY_PROGRESSUPDATE_DURATION_I = "KEY_PROGRESSUPDATE_DURATION_I";
    public static final String KEY_POSITION_I = "KEY_POSITION_I";
    public static final String KEY_PATH = "KEY_PATH";
    //    public static final String KEY_COMPLETION_RESULT = "KEY_COMPLETION";
    public static final String KEY_DURATION = "KEY_DURATION";

    public static final String KEY_ISLIVING = "KEY_ISLIVING";
    /**
     * 当前已经下载大小
     */
    public static final String KEY_DOWNLOAD_SIZE = "KEY_DOWNLOAD_SIZE";
    /**
     * 当前文件总大小
     */
    public static final String KEY_DOWNLOAD_TOTAL_SIZE = "KEY_DOWNLOAD_TOTAL_SIZE";
    /**
     * 当前播放音频文件是否为预缓存好的文件标识
     */
    public static final String KEY_LOAD_LOCAL_FILE = "KEY_LOAD_LOCAL_FILE";
    /**
     * client
     */
    public static final int CLIENT_ACTION_BIND = 86998;    //客户端绑定
    public static final int CLIENT_ACTION_UNBIND = 86999;    //客户端解绑
    public static final int CLIENT_ACTION_THEURL = 87000;    //设置新URL并播放
    public static final int CLIENT_ACTION_PRELOAD_THEURL = 870001;    //预加载URL
    public static final int CLIENT_ACTION_PLAY = 87002;    //通知播放器播放
    public static final int CLIENT_ACTION_PAUSE = 87003;    //通知播放器暂停
    public static final int CLIENT_ACTION_STOP = 87004;    //通知播放器停止
    public static final int CLIENT_ACTION_DESTROY = 87005;    //通知播放器销毁
    public static final int CLIENT_ACTION_KILLSELF = 87006;    //通知播放Service自杀
    public static final int CLIENT_ACTION_REBUILDSERVICE = 87007;    //重建Service时重新设置Service
    public static final int CLIENT_ACTION_BACKGROUND = 87008;    //应用进入后台状态
    public static final int CLIENT_ACTION_FOREGROUND = 87009;    //应用进入前台状态
    public static final int CLIENT_ACTION_SEEK = 87010;    //设置seek并播放
    public static final int CLIENT_ACTION_RESET = 87011;    //通知播放器重置
    public static final int CLIENT_ACTION_PREPARE_PLAY = 87100;    //预备执行动作-播放
    public static final int CLIENT_ACTION_PREPARE_PAUSE = 87101;    //预备执行动作-暂停
    public static final int CLIENT_SENDACTION_PLAY = 87102;    //执行发送播放指令
    public static final int CLIENT_SENDACTION_PAUSE = 87103;    //执行发送暂停指令
    public static final int CLIENT_ACTION_PREPARE_SETTHEURL = 87104;    //预备执行动作-设置URL
    public static final int CLIENT_SENDACTION_SETTHEURL = 87105;    //执行设置URL指令
    public static final int CLIENT_SENDACTION_SEEK = 87106;    //执行seek指令
    public static final int CLIENT_ACTION_PREPARE_RESET = 87107;    //预备执行动作-重置
    public static final int CLIENT_SENDACTION_RESET = 87108;    //执行reset指令
    /**
     * service
     */

    public static final int SERVICE_ACTION_PAUSED = 88000;    //通知客户端暂停
    public static final int SERVICE_ACTION_STOPED = 88001;    //通知客户端停止
    public static final int SERVICE_ACTION_PLAYING = 88002;    //通知客户端播放
    public static final int SERVICE_ACTION_DISTORIED = 88003;    //通知客户端已销毁
    public static final int SERVICE_ACTION_ERRORINFO = 88004;    //通知客户端有错误消息
    public static final int SERVICE_ACTION_COMPLETED = 88005;    //通知客户端播放结束（不一定是整个音频播放到结尾，请根据其它参数结合着处理）
    //    public static final int SERVICE_ACTION_RELEASE = 88015;    //马上释放
//    public static final int SERVICE_ACTION_LAZYPLAY = 88007;    //延迟播放
//    public static final int SERVICE_ACTION_PREPARE_BUFFER = 88009;    //准备ＢＵＦＦＥＲ发送
//    public static final int SERVICE_ACTION_SEND_BUFFER = 88010;    //服务端ＢＵＦＦＥＲ发送
//    public static final int SERVICE_ACTION_PREPARE_UPDATE = 88011;    //准备UPDATE发送
//    public static final int SERVICE_ACTION_SEND_UPDATE = 88012;    //服务端UPDATE发送
//    public static final int SERVICE_ACTION_SEND_PAUSED = 88013;    //服务端pause发送
    public static final int SERVICE_ACTION_BUFFERING = 88014;
    public static final int SERVICE_ACTION_BUFFER_READY = 88015;    //Buffer ready
    public static final int SERVICE_ACTION_SEEK_READY = 88016;     //Seek ready
    public static final int SERVICE_ACTION_DOWNLOADING = 88017;    // 下载进度
    public static final int SERVICE_ACTION_LOAD_LOCALFILE = 88018; // 是否读取本地预缓存文件
    public static final int SERVICE_ACTION_PROGRESS = 88019; // 音频播放进度

    int SERVICE_ACTION_BUFFERING_START = 88020; // M3U8音频缓冲开始
    int SERVICE_ACTION_BUFFERING_END = 88021; // M3U8音频缓冲结束
    /**
     * error code
     */
    public static final int SERVICE_MEDIAPLAY_ERROR_URLISNULL = 89000;    //错误信息，ＵＲＬ是空。
//    public static final int SERVICE_MEDIAPLAY_ERROR_PROLOADURLISNULL = 89001;    //错误信息 ，PROLOADURL是空。
    /**
     * broadcast
     */
//    public static final String ACTION_PLAYBACK_BUFFER = "com.itings.myradio.intent.action.PBBUFFER";  //广播通知客户端正在缓冲
    public static final String ACTION_PLAYBACK_UPDATED = "com.itings.myradio.intent.action.PBUPDATE"; //广播通知客户端更新播放倒计时

    /**
     * Enum
     */
    enum MediaPlayType {        //可使用播放器的种类
        VLCMediaPlay,
        SYSTEMMediaPlay
    }

}
