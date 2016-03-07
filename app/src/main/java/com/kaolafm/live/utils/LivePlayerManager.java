package com.kaolafm.live.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 直播流管理类
 */
public class LivePlayerManager {

	private static final String TAG = LivePlayerManager.class.getSimpleName();
	private static LivePlayerManager mLivePlayerManager;

	private Context mContext;
	private IMediaPlayer mMediaPlayer;
	/**
	 * 直播房间ID
	 */
	private long mLiveID = 0L;
	/**
	 * 直播计划ID
	 */
	private long mProgramID = 0L;
	/**
	 * 直播链接
	 */
	private String mLiveUrl;
	/**
	 * 断网之前播放的时间
	 */
	private long mNotNetBeforePotison;
	/**
	 * 是否可以处理来自通知栏的播放和暂停直播 true为是，false为否
	 */
	private boolean canManageNotifycationPlayOrPause = true;
	// private boolean mIsPlaying = false;
	/**
	 * 是否可以重新播放，针对手机来电话true为是，false为否
	 */
	private boolean mReplay;
	/**
	 * 在点播播放器和直播播放器进行切换的时候，判断是否显示直播播放器
	 */
	private boolean mShowLivePlayerEnabled;
	/**
	 * 直播是否已经结束true为是，false为否
	 */
	private boolean mIsLiveComplete;
	/**
	 * 考拉播放器是否已经启动true为是，false为否
	 */
	private boolean isPlayerStart;
	/**
	 * 是否可以处理MiUi音乐控件 true为是，false为否
	 */
	private boolean canShowMiUiSupport;
	/**
	 * 当时直播流是否在播放中true为是，false为否 判断是直播播放器还是回放播放器 true为直播，false为直播点播方式
	 */
	private boolean isLivingPlayerPlayed = true;
	/**
	 * 记录当前播放器是否已经释放成功 true为是，false为否
	 */
	private boolean isLivePlayerReleaseSuccess = true;

	/**
	 * 记录当前播放音频总时长
	 */
	private long mDuration;

	/**
	 * 记录播放器当前播放进度
	 */
	private int mCurrentPosition;

	/**
	 * 播放直播任务对象
	 */
	// private KaolaTask mPlayTask;
	public LivePlayerManager() {
		super();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * 用户拖动进度条当前直播播放器是否处于播放状态true为是，false为否
	 */
	private boolean isLivePlayerPlayingBeforeSeek;
	/**
	 * 当前播放器是否处于seek状态 true为是，false为否
	 */
	// private boolean isPlayerSeekStart;

	private static final String ACTION_BASE = "com.kaolafm.LivePlayerManager.action";
	public static final String ACTION_PLAY_OR_PAUSE = ACTION_BASE
		+ ".PLAY_OR_PAUSE";

	private ArrayList<OnLivePlayerDisAbleListener> mLivePlayerDisAbleListener = new ArrayList<OnLivePlayerDisAbleListener>();
	/**
	 * 回放直播结束回调（播放到最后一秒的时候更换状态）
	 */
	private ArrayList<OnLiveEndListener> mOnLiveEndListeners = new ArrayList<OnLiveEndListener>();
	/**
	 * 音频缓冲回调
	 */
	private ArrayList<OnLiveStreamBufferListener> mOnLiveStreamBufferListener = new ArrayList<OnLiveStreamBufferListener>();

	/**
	 * 直播播放器播放流程回调集合对象
	 */
	private ArrayList<OnLivePlayerStatusChangeListener> mOnLivePlayerStatusChangeListener = new ArrayList<OnLivePlayerStatusChangeListener>();
	private OnNetWorkStatusChangedListener mNetWorkStatusChangedListener;
	/**
	 * 直播播放状态回调集合对象
	 */
	private ArrayList<OnStateChangedListener> mOnStateChangedListeners = new ArrayList<OnStateChangedListener>();
	/**
	 * 直播播放器Seek回调集合
	 */
	private ArrayList<OnLiveSeekCompleteListener> mOnLiveSeekCompleteListeners = new ArrayList<OnLiveSeekCompleteListener>();
	/**
	 * 直播当前播放进度回调集合
	 */
	private ArrayList<OnProcessListener> mOnProcessListeners = new ArrayList<OnProcessListener>();
	/**
	 * 直播播放器缓冲开始回调集合
	 */
	private ArrayList<OnLiveStreamBufferStartListener> mOnLiveStreamBufferStartListeners = new ArrayList<OnLiveStreamBufferStartListener>();

	/**
	 * 直播播放器缓冲结束回调集合
	 */
	private ArrayList<OnLiveStreamBufferEndListener> mOnLiveStreamBufferEndListeners = new ArrayList<OnLiveStreamBufferEndListener>();

	/**
	 * 记录当前直播或直播点播播放器状态是否为播放状态，true为是，false为否
	 * 只所以用这个变量是因为目前在更新播放器状态更新不同步且没有找到合适的解决方案
	 */
	private boolean isLivePlayerPlaying;

	/**
	 * 处理通知栏点击播放和暂停消息
	 */
	private static final int HANDLE_NOTIFYCATION_PLAY_OR_PAUSE_MSG = 1;
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLE_NOTIFYCATION_PLAY_OR_PAUSE_MSG:
				if (isLivingPlayerPlayed) {
					if (isPlaying()) {
						reset();
					} else {
						// start(mLiveData);
					}
				} else {
					if (isPlaying()) {
						pause();
					} else {
						if (isLivePlayerReleaseSuccess()) {
							if (!TextUtils.isEmpty(mLiveUrl)) {
								start(mLiveUrl);
							}
						} else {
							start();
						}
					}
				}
				canManageNotifycationPlayOrPause = true;
				break;
			default:
				break;
			}
		}
	};

	public boolean isPlayerStart() {
		return isPlayerStart;
	}

	public void setPlayerStart(boolean isPlayerStart) {
		this.isPlayerStart = isPlayerStart;
	}

	private AudioManager mAudioManager;

	private LivePlayerManager(Context context) {
		mContext = context;
		initPlayer();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_PLAY_OR_PAUSE);
		mContext.registerReceiver(mPlayReceiver, intentFilter);
		mAudioManager = (AudioManager) mContext
			.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
			AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		// canShowMiUiSupport = MiUiSupport.canSupportMiUi();
		// NetworkMonitor.getInstance(mContext)
		// .registerNetworkStatusChangeListener(
		// mOnNetworkStatusChangedListener);
	}

	private OnAudioFocusChangeListener mOnAudioFocusChangeListener = new OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int focus) {
			// TODO Auto-generated method stub
			System.out.println("onAudioFocusChange------>" + focus);
			manageAudioFocusChange(focus);
		}
	};

	private void initPlayer() {
		if (!isLivePlayerReleaseSuccess) {
			resetMediaPlayer();
		}
		try {
			// if (mMediaPlayer == null) {
			mMediaPlayer = new IjkMediaPlayer();
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnCompletionListener(mComletionListener);
			mMediaPlayer.setOnErrorListener(mErroristener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
			mMediaPlayer.setOnInfoListener(mInfoListener);
			// }
		} catch (Exception ex) {
			ex.printStackTrace();
			// KL.e(LivePlayerManager.class, "initPlayer failed {}",
			// ex.toString());
		}
	}

	private IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {

		@Override
		public boolean onInfo(IMediaPlayer mp, int what, int extra) {
			switch (what) {
			case IMediaPlayer.MEDIA_INFO_BUFFERING_START: // 当前IJK播放器没有任何缓存了，需要重新拉取数据
															// 卡顿开始
															// KL.i(LivePlayerManager.class,
				// "ijk缓冲开始。。。MEDIA_INFO_BUFFERING_START");
				bufferStartOrEnd(true);
				break;
			case IMediaPlayer.MEDIA_INFO_BUFFERING_END: // IJK的主线程已经停止工作，此时可以进行重试。
														// 卡顿结束
														// KL.i(LivePlayerManager.class,
				// "ijk缓冲结束。。。MEDIA_INFO_BUFFERING_END");
				bufferStartOrEnd(false);
				break;
			default:
				break;
			}
			return false;
		}
	};

	/**
	 * 缓冲开始或者结束 true开始 false结束
	 */
	private void bufferStartOrEnd(final boolean flag) {
		if (Looper.getMainLooper() == Looper.myLooper()) {
			if (flag) {
				for (OnLiveStreamBufferStartListener onLiveStreamBufferStartListener : mOnLiveStreamBufferStartListeners) {
					onLiveStreamBufferStartListener.onLiveStreamBufferStart();
				}
			} else {
				for (OnLiveStreamBufferEndListener onLiveStreamBufferEndListener : mOnLiveStreamBufferEndListeners) {
					onLiveStreamBufferEndListener.onLiveStreamBufferEnd();
				}
			}
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (flag) {
						for (OnLiveStreamBufferStartListener onLiveStreamBufferStartListener : mOnLiveStreamBufferStartListeners) {
							onLiveStreamBufferStartListener
								.onLiveStreamBufferStart();
						}
					} else {
						for (OnLiveStreamBufferEndListener onLiveStreamBufferEndListener : mOnLiveStreamBufferEndListeners) {
							onLiveStreamBufferEndListener
								.onLiveStreamBufferEnd();
						}
					}
				}
			});
		}
	}

	/**
	 * 播放器准备完成回调
	 */
	private IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
		public void onPrepared(IMediaPlayer mp) {
			// KL.i(LivePlayerManager.class, "onPrepared------------>");
			try {
				// mMediaPlayer.start();
				mMediaPlayer.start();
				// KL.d(LivePlayerManager.class, "play m3u8 start playing");
				isLivePlayerPlaying = true;
				isLivePlayerReleaseSuccess = false;
				requestAudioFocus();
				notifyStateChanged();
			} catch (IllegalStateException ill) {
				// KL.e(LivePlayerManager.class, "onPrepared error : {}",
				// ill.toString());
			}
			notifyOnPlayerPrepared();
		}
	};

	private void notifyOnPlayerPrepared() {
		if (null == mOnLivePlayerStatusChangeListener) {
			return;
		}

		if (Looper.getMainLooper() == Looper.myLooper()) {
			for (OnLivePlayerStatusChangeListener onLivePlayerStatusChangeListener : mOnLivePlayerStatusChangeListener) {
				onLivePlayerStatusChangeListener.onPrepared();
			}
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					for (OnLivePlayerStatusChangeListener onLivePlayerStatusChangeListener : mOnLivePlayerStatusChangeListener) {
						onLivePlayerStatusChangeListener.onPrepared();
					}
				}
			});
		}
	}

	private void notifyOnAnchorConnectFailed() {
		if (null == mOnLivePlayerStatusChangeListener) {
			return;
		}
		if (Looper.getMainLooper() == Looper.myLooper()) {
			for (OnLivePlayerStatusChangeListener onLivePlayerStatusChangeListener : mOnLivePlayerStatusChangeListener) {
				onLivePlayerStatusChangeListener.onAnchorConnectFailed();
			}
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					for (OnLivePlayerStatusChangeListener onLivePlayerStatusChangeListener : mOnLivePlayerStatusChangeListener) {
						onLivePlayerStatusChangeListener
							.onAnchorConnectFailed();
					}
				}
			});
		}
	}

	private void notifyOnReseted() {
		if (null == mOnLivePlayerStatusChangeListener) {
			return;
		}
		if (Looper.getMainLooper() == Looper.myLooper()) {
			for (OnLivePlayerStatusChangeListener onLivePlayerStatusChangeListener : mOnLivePlayerStatusChangeListener) {
				onLivePlayerStatusChangeListener.onReseted();
			}
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					for (OnLivePlayerStatusChangeListener onLivePlayerStatusChangeListener : mOnLivePlayerStatusChangeListener) {
						onLivePlayerStatusChangeListener.onReseted();
					}
				}
			});
		}
		// PlayBackManager.reportLiveEnd(mContext, mLiveData);
	}

	private void notifyOnLiveStreamBuffer() {
		if (null == mOnLiveStreamBufferListener) {
			return;
		}
		if (Looper.getMainLooper() == Looper.myLooper()) {
			for (OnLiveStreamBufferListener onLiveStreamBufferListener : mOnLiveStreamBufferListener) {
				onLiveStreamBufferListener.onLiveStreamBuffer();
			}
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					for (OnLiveStreamBufferListener onLiveStreamBufferListener : mOnLiveStreamBufferListener) {
						onLiveStreamBufferListener.onLiveStreamBuffer();
					}
				}
			});
		}
	}

	/**
	 * 播放器进度缓冲回调
	 */
	private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate(IMediaPlayer mp, int position,
			int duration) {
			// if (isPlayerSeekStart) {
			// return;
			// }
			mDuration = duration;
			mCurrentPosition = position;
			for (OnProcessListener onProcessListener : mOnProcessListeners) {
				mNotNetBeforePotison = position;
				onProcessListener.onProcess(position, duration);
			}
		}
	};
	/**
	 * seetTo方法 seek结束回调
	 */
	private IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
		@Override
		public void onSeekComplete(IMediaPlayer mp) {
			// KL.i(LivePlayerManager.class, "onSeekComplete------------>");
			// isPlayerSeekStart = false;
			if (isLivePlayerPlayingBeforeSeek) {
				start();
			}
			notifyLiveSeekComplete();
		}
	};

	private void notifyLiveSeekComplete() {
		if (Looper.getMainLooper() == Looper.myLooper()) {
			for (OnLiveSeekCompleteListener onLiveSeekCompleteListener : mOnLiveSeekCompleteListeners) {
				onLiveSeekCompleteListener.onLiveSeekComplete();
			}
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					for (OnLiveSeekCompleteListener onLiveSeekCompleteListener : mOnLiveSeekCompleteListeners) {
						onLiveSeekCompleteListener.onLiveSeekComplete();
					}
				}
			});
		}
	}

	/**
	 * 直播结束回调
	 */
	private IMediaPlayer.OnCompletionListener mComletionListener = new IMediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(IMediaPlayer mp) {
			// KL.i(LivePlayerManager.class,
			// "OnCompletionListener------------>");
			notifyLiveEnd();
		}
	};

	private void notifyLiveEnd() {
		if (Looper.getMainLooper() == Looper.myLooper()) {
			for (OnLiveEndListener onLiveEndListener : mOnLiveEndListeners) {
				if (onLiveEndListener != null) {
					onLiveEndListener.onLiveEnd();
				}
			}
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					for (OnLiveEndListener onLiveEndListener : mOnLiveEndListeners) {
						if (onLiveEndListener != null) {
							onLiveEndListener.onLiveEnd();
						}
					}
				}
			});
		}
	}

	/**
	 * 播放器错误回调
	 */
	private IMediaPlayer.OnErrorListener mErroristener = new IMediaPlayer.OnErrorListener() {
		@Override
		public boolean onError(IMediaPlayer mp, int what, int extra) {// 直播出错回调
			// KL.w(LivePlayerManager.class, "onError what = {}", what);

			/**
			 * 404、502、700——" 直播连接失败，请重试 " 503——"直播还未开始"
			 * 701——连续收到701，15次，显示"直播已结束"，改变状态;
			 */
			if (what == IjkMediaPlayer.MEDIA_ERROR_IJK_PLAYER) {
				switch (extra) {
				case IjkMediaPlayer.GET_STREAM_FAILED_SUB_ERROR_IJK_PLAYER:
					//replayCurrentM3U8();
					break;
				case IjkMediaPlayer.BAD_GATEWAY_SUB_ERROR_IJK_PLAYER:
				case IjkMediaPlayer.NO_ID_SUB_ERROR_IJK_PLAYER:
				case IjkMediaPlayer.DOMAIN_SUB_ERROR_IJK_PLAYER:
					if (mIsLiveComplete) {
						break;
					}
					// ToastUtil.showToast(mContext,
					// R.string.live_play_error_str,
					// Toast.LENGTH_SHORT);
					break;
				case IjkMediaPlayer.NO_FILE_SUB_ERROR_IJK_PLAYER:
					if (mIsLiveComplete) {
						break;
					}
					mHandler.removeCallbacks(mRunnable);
					mHandler.postDelayed(mRunnable, 20 * 1000);
					notifyOnAnchorConnectFailed();
					break;
				case IjkMediaPlayer.NO_UPDATE_SUB_ERROR_IJK_PLAYER:
					if (mIsLiveComplete) {
						break;
					}
					// start(mLiveData);
					replayCurrentM3U8();
					notifyOnAnchorConnectFailed();
					break;
				default:
					break;
				}
			}
			// String conferenceName = ChatManager.getInstance(mContext)
			// .getConferenceName();
			// if (!StringUtil.isEmpty(conferenceName)) {
			// StatisticsManager.getInstance(mContext).reportLiveError(
			// StatisticsManager.ErrorEventCode.LIVE_ERROR,
			// String.valueOf(mLiveID),
			// String.valueOf(mLiveData.getAlbumId()),
			// String.valueOf(mLiveData.getProgramId()));
			// }
			return true;
		}
	};

	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if (mIsLiveComplete) {
				return;
			}
			// start(mLiveData);
		}
	};

	/**
	 * 释放播放器
	 */
	private void release() {
		abandonAudioFocus();
		if (mMediaPlayer != null) {
			try {
				mMediaPlayer.release();
			} catch (IllegalStateException ill) {
				ill.printStackTrace();
			}
			mMediaPlayer = null;
		}
	}

	/**
	 * 将播放器状态置为idle状态
	 */
	private synchronized void resetMediaPlayer() {
		if (mMediaPlayer != null) {
			abandonAudioFocus();
			try {
				Log.d(TAG, "play m3u8 reset start");
				mMediaPlayer.reset();
				Log.d(TAG, "play m3u8 reset end");
				isLivePlayerPlaying = false;
				isLivePlayerReleaseSuccess = true;
				// KaolaNotificationManager.getInstance(mContext)
				// .updateLiveNotification(); // 通知状态栏播放按钮变化
				notifyStateChanged();
				Log.d(TAG, "notifyStateChanged,line 580");
				notifyOnReseted();// 通知调用者播放器已经被reset掉了
			} catch (IllegalStateException ill) {
				notifyOnReseted();// 通知调用者播放器已经被reset掉了
				ill.printStackTrace();
				isLivePlayerReleaseSuccess = false;
				notifyStateChanged();
				Log.d(TAG, "resetMediaPlayer error = {}" + ill.toString());
			}
		}
	}

	/**
	 * 当前直播播放器是否已经释放
	 * 
	 * @return
	 */
	public boolean isLivePlayerReleaseSuccess() {
		return isLivePlayerReleaseSuccess;
	}

	public static LivePlayerManager getInstance(Context context) {
		if (mLivePlayerManager == null) {
			synchronized (LivePlayerManager.class) {
				if (mLivePlayerManager == null) {
					mLivePlayerManager = new LivePlayerManager(
						context instanceof Activity ? context
							.getApplicationContext() : context);
				}
			}
		}
		return mLivePlayerManager;
	}

	/**
	 * 销毁播放器资源
	 */
	public void destroy() {
		mHandler.removeCallbacksAndMessages(null);
		release();
		try {
			mContext.unregisterReceiver(mPlayReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// NetworkMonitor.getInstance(mContext).removeNetworkStatusChangeListener(
		// mOnNetworkStatusChangedListener);
	}

	/**
	 * 获取音频焦点
	 */
	public void requestAudioFocus() {
		int flag = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
			AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	}

	/**
	 * 失去音频焦点
	 */
	public void abandonAudioFocus() {
		int flag = mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
		System.out.println("onAudioFocusChange flag--------------->" + flag);
	}

	/**
	 * 播放一个回放流
	 * 
	 * @param obj
	 */
	public void start(Object obj) {
		// 未完，待续
	}

	/**
	 * 播放一个直播流
	 * 
	 * @param liveData
	 */
	// public void start(LiveData liveData) {
	// if (liveData == null) {
	// return;
	// }
	// mLiveID = liveData.getLiveId();
	// mProgramID = liveData.getProgramId();
	// if (canShowMiUiSupport) {
	// MiUiSupport.getInstance().setLiveData(mContext, liveData);
	// }
	// isLivingPlayerPlayed = true;
	// play(liveData.getLiveUrl());
	// // 收听直播积分统计上报
	// if (liveData != null) {
	// new IntegralReportUtil(mContext).integralReport(
	// IntegralReportUtil.INTEGRAL_TASK_LISTEN_LIVE_CODE,
	// String.valueOf(liveData.getLiveId()), false, TAG);
	// }
	// }

	/**
	 * 暂停点播播放器
	 */
	private void pausePlayer() {
		// PlayerManager playerManager = PlayerManager.getInstance(mContext);
		// if (playerManager.isPlaying()) {
		// playerManager.pause();
		// }
	}

	/**
	 * 播放一个m3u8音频文件
	 * 
	 * @param m3u8Url
	 */
	public void start(String m3u8Url) {
		isLivingPlayerPlayed = false;
		play(m3u8Url);
	}

	/**
	 * 重新播放当前直播流
	 */
	public void replayCurrentM3U8() {
		if (TextUtils.isEmpty(mLiveUrl)) {
			return;
		}
		Log.d(TAG, "play m3u8 replayCurrentM3U8 mLiveUrl = {}" + mLiveUrl);
		play(mLiveUrl);
	}

	/**
	 * 播放函数是否已经执行完毕
	 */
	private void play(final String m3u8Url) {
		if (isLivePlayerPlaying) {
			pause();
		}
		// if (NetworkManager.getInstance() != null) {
		// NetworkManager.getInstance().setNetowrkCheckListener(
		// new NetworkManager.INetworkCheckListener() {
		// @Override
		// public void onNetworkStateEnable() {
		pausePlayer();
		mLiveUrl = m3u8Url;
		new Thread(new Runnable() {
			@Override
			public void run() {
				notifyOnLiveStreamBuffer();
				// PlayerManager.getInstance(mContext).setLivePlayerStart(true);
				setPlayerStart(false);
				// TODO Auto-generated method stub
				// int index = 0;
				// int resetCount = 0;
				Log.d(TAG, "play m3u8 start");
				// while (!isLivePlayerReleaseSuccess &&
				// resetCount > 3) {
				// SystemClock.sleep(10);
				// index++;
				// if (index > 50) { //
				// 如果500ms内还未重置播放器则重新重置一下播放器
				// index = 0;
				// resetMediaPlayer();
				// resetCount++;
				// }
				// }
				if (!isLivePlayerReleaseSuccess) {
					resetMediaPlayer();
				}

				String host = null;
				String port = null;
				String proxySuffix = " http_proxy=";
				if (SDKUtil.hasICE_CREAM_SANDWICH()) {
					host = System.getProperty("http.proxyHost");
					port = System.getProperty("http.proxyPort");
					Log.d(TAG, "host = {}, port = {}" + host + ":" + port);
					if (!TextUtils.isEmpty(host)) {
						proxySuffix += "http://" + host;
						if (!TextUtils.isEmpty(port)) {
							proxySuffix += ":" + port + "/";
						}
					}
					Log.d(TAG, "proxySuffix = {}" + proxySuffix);
				}
				try {
					Log.d(TAG, "play m3u8 middle");
					mMediaPlayer
					.setDataSource(mLiveUrl);
					//mMediaPlayer
						//.setDataSource(mLiveUrl
							//+ (false ? " http_proxy=http://3000004584:429664D0B5CD2879@kaolafm.proxy.10155.com:8080/"
								//: proxySuffix));
					mMediaPlayer.prepareAsync();
					Log.d(TAG, "play m3u8 end");
					// if (canShowMiUiSupport) {
					// MiUiSupport.getInstance().updateMiUiPlayState(
					// MiUiSupport.PLAYSTATE_PLAYING);
					// }
				} catch (IllegalStateException ill) {
					if (!isLivePlayerReleaseSuccess) {
						resetMediaPlayer();
					}
					// initPlayer();
					replayCurrentM3U8();
					ill.printStackTrace();
					Log.d(TAG, "play m3u8 error = {}" + ill.toString());
					Log.d(TAG, "start live player error = {}" + ill.toString());
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(TAG, "start live player error = {}" + e.toString());
				} finally {
					isLivePlayerStart = false;
					isLivePlayerReleaseSuccess = false;
				}

				// mHandler.postDelayed(new Runnable() { //
				// 确保推送处理完毕后能在状态栏中更新直播数据
				//
				// @Override
				// public void run() {
				// // TODO Auto-generated method stub
				// KaolaNotificationManager.getInstance(mContext)
				// .updateLiveNotification();
				// }
				// }, 1500);
			}
		}).start();
		// }

		// @Override
		// public void onNetworkStateDisable() {
		// // isPlayMethodExcuteComplete = true;
		// }
		// });
		// }
	}

	/**
	 * 启动当前直播频道
	 */
	public void reStartCurrentLivePlayer() {
		// start(mLiveData);
		play(mLiveUrl);
	}

	/**
	 * 切换直播播放状态（暂停、播放）
	 */
	public void switchPlayerStatus() {
		if (isLivingPlayerPlayed) {
			if (isPlaying()) {
				reset();
			} else {
				reStartCurrentLivePlayer();
			}
		} else {
			if (isPlaying()) {
				pause();
			} else {
				start();
			}
		}
	}

	/**
	 * 播放m3u8音频
	 */
	public void playM3U8() {
		if (isPlaying()) {
			return;
		}
		if (isLivingPlayerPlayed) {
			reStartCurrentLivePlayer();
		} else {
			start();
		}
	}

	/**
	 * 暂停m3u8音频
	 */
	public void pauseM3U8() {
		if (!isPlaying()) {
			return;
		}
		if (isLivingPlayerPlayed) {
			reset();
		} else {
			pause();
		}
	}

	/**
	 * 释放直播播放器
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void reset() {
		Log.d(TAG, "play m3u8 reset ysn start");
		// KaolaNotificationManager.getInstance(mContext).updateLiveNotification();
		new KaolaTask() {

			@Override
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				mHandler.removeCallbacksAndMessages(null);
				resetMediaPlayer();
				Log.d(TAG, "play m3u8 reset ysn end");
				// if (canShowMiUiSupport) {
				// MiUiSupport.getInstance().updateMiUiPlayState(
				// MiUiSupport.PLAYSTATE_PAUSED);
				// }
				return null;
			}

			protected void onPostExecute(Object result) {
				// notifyStateChanged();
				// canManageNotifycationPlayOrPause = true;
			}
		}.execute();
	}

	/**
	 * 获取当前直播播放器是否处于播放状态 true为是，false为否
	 * 
	 * @return true为播放，false为暂停或停止
	 */
	public boolean isPlaying() {
		if (mMediaPlayer == null) {
			return false;
		}
		return mMediaPlayer.isPlaying();
	}

	/**
	 * 获取直播播放器是否处于播放状态 此函数目前只给状态栏切换播放 播放或暂停状态使用
	 * 
	 * @return true为播放，false为暂停或停止
	 */
	public boolean isLivePlayerPlaying() {
		return isLivePlayerPlaying;
	}

	/**
	 * 监听网络变化
	 */
	// NetworkMonitor.OnNetworkStatusChangedListener
	// mOnNetworkStatusChangedListener = new
	// NetworkMonitor.OnNetworkStatusChangedListener() {
	// @Override
	// public void onStatusChanged(int newStatus, int oldStatus) {
	// // 这样做避免了从wifi切换到移动也会通知有网络了。对调用者产生影响
	// if (oldStatus == NetworkMonitor.STATUS_NO_NETWORK) {
	// if (newStatus == NetworkMonitor.STATUS_MOBILE
	// || newStatus == NetworkMonitor.STATUS_WIFI) {
	// if (mNetWorkStatusChangedListener != null) {
	// mNetWorkStatusChangedListener
	// .onNetWorkStatusChanged(true);
	// }
	// }
	// } else if (newStatus == NetworkMonitor.STATUS_NO_NETWORK) {
	// KL.i(LivePlayerManager.class, "网络状态SSTATUS_NO_NETWORK");
	// if (mNetWorkStatusChangedListener != null) {
	// mNetWorkStatusChangedListener.onNetWorkStatusChanged(false);
	// }
	// }
	//
	// }
	// };

	/**
	 * 获取当前直播url
	 * 
	 * @return
	 */
	public String getmLiveUrl() {
		return mLiveUrl;
	}

	/**
	 * 获取当前直播房间ID
	 * 
	 * @return
	 */
	public long getCurLiveId() {
		return mLiveID;
	}

	/**
	 * 获取当前直播计划ID
	 * 
	 * @return
	 */
	public long getCurProgramId() {
		return mProgramID;
	}

	public void addOnStateChangedListener(
		OnStateChangedListener onStateChangedListener) {
		if (mOnStateChangedListeners.contains(onStateChangedListener)) {
			return;
		}
		mOnStateChangedListeners.add(onStateChangedListener);
	}

	public void removeOnStateChangedListener(
		OnStateChangedListener onStateChangedListener) {
		if (mOnStateChangedListeners.contains(onStateChangedListener)) {
			mOnStateChangedListeners.remove(onStateChangedListener);
		}
	}

	private void notifyStateChanged() {
		if (Looper.getMainLooper() == Looper.myLooper()) {
			for (OnStateChangedListener onStateChangedListener : mOnStateChangedListeners) {
				onStateChangedListener.onStateChanged();
			}
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					for (OnStateChangedListener onStateChangedListener : mOnStateChangedListeners) {
						onStateChangedListener.onStateChanged();
					}
				}
			});
		}
	}

	private void notifyAutoStateChanged() {
		if (Looper.getMainLooper() == Looper.myLooper()) {
			for (OnStateChangedListener onStateChangedListener : mOnStateChangedListeners) {
				onStateChangedListener.onAutoStateChanged();
			}
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					for (OnStateChangedListener onStateChangedListener : mOnStateChangedListeners) {
						onStateChangedListener.onAutoStateChanged();
					}
				}
			});
		}
	}

	private BroadcastReceiver mPlayReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// if (ACTION_PLAY_OR_PAUSE.equals(action)) {
			// if (!ChatFragment.isPodcastState) {
			// if (canManageNotifycationPlayOrPause) {
			// mHandler
			// .sendEmptyMessage(HANDLE_NOTIFYCATION_PLAY_OR_PAUSE_MSG);
			// canManageNotifycationPlayOrPause = false;
			// }
			// } else {
			// }
			// }
		}
	};

	/**
	 * 处理耳机拔出操作
	 */
	public void manageHeadsetPlug() {
		if (!isPlaying()) {
			return;
		}
		if (isLivingPlayerPlayed) {
			reset();
		} else {
			pause();
			notifyAutoStateChanged();
		}
	}

	// public void setLiveData(LiveData liveData) {
	// mLiveData = liveData;
	// }

	// public LiveData getLiveData() {
	// return mLiveData;
	// }

	public boolean isShowLivePlayerEnabled() {
		return mShowLivePlayerEnabled; // && mIsPlaying
	}

	public void setIsLivingPlayerPlayed(boolean isLivingPlayerPlayed) {
		this.isLivingPlayerPlayed = isLivingPlayerPlayed;
	}

	public boolean isLivingPlayerPlayed() {
		return isLivingPlayerPlayed;
	}

	public void enableShowLivePlayer() {
		Log.d(TAG, "isJoininLivePlayer---enable = {}" + mShowLivePlayerEnabled);
		mShowLivePlayerEnabled = true;
	}

	public void disableShowLivePlayer() {
		Log.d(TAG, "isJoininLivePlayer---disable = {}" + mShowLivePlayerEnabled);
		mShowLivePlayerEnabled = false;
	}

	public void addLivePlayerDisAbleListener(
		OnLivePlayerDisAbleListener onLivePlayerDisAbleListener) {
		if (mLivePlayerDisAbleListener.contains(onLivePlayerDisAbleListener)) {
			return;
		}
		mLivePlayerDisAbleListener.add(onLivePlayerDisAbleListener);
	}

	/**
	 * 销毁注册监听
	 */
	public void removeLivePlayerDisableListener(
		OnLivePlayerDisAbleListener onLivePlayerDisAbleListener) {
		if (mLivePlayerDisAbleListener.contains(onLivePlayerDisAbleListener)) {
			mLivePlayerDisAbleListener.remove(onLivePlayerDisAbleListener);
		}
	}

	/**
	 * 让直播或直播回放播放器不可用且销毁直播或直播回放播放器
	 */
	public void disAbleLivePlayerAndRemoveLiveOrPlayBackPage() {
		disableShowLivePlayer();
		removeLiveOrPlayBackPlayer();
	}

	private void removeLiveOrPlayBackPlayer() {
		if (mLivePlayerDisAbleListener != null) {
			for (OnLivePlayerDisAbleListener onLivePlayerDisAbleListener : mLivePlayerDisAbleListener) {
				onLivePlayerDisAbleListener.onLivePlayerDisable();
			}
		}
	}

	/**
	 * 处理音频失去焦点逻辑
	 * 
	 * @param focusChange
	 */
	private boolean isLivePlayerStart;

	public void manageAudioFocusChange(int focusChange) {
		if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
			if (mReplay && !isLivePlayerStart) {
				isLivePlayerStart = true;
				mReplay = false;
				if (isLivingPlayerPlayed) {
					// start(mLiveData);
					reStartCurrentLivePlayer();
				} else {
					start();
					notifyAutoStateChanged();
				}
			}
		} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
			|| focusChange == AudioManager.AUDIOFOCUS_LOSS) {
			if (isPlaying()) {
				if (isLivingPlayerPlayed) {
					reset();
				} else {
					pause();
					notifyAutoStateChanged();
				}
				mReplay = true;
			}
		}
	}

	/**
	 * 处理挂断电话
	 */
	public void manageCallHandUp() {
		if (mReplay) {
			mReplay = false;
			if (isLivingPlayerPlayed) {
				// start(mLiveData);
			} else {
				start();
				notifyAutoStateChanged();
			}
		}
	}

	/**
	 * 处理打电话和接听电话
	 */
	public void manageCalling() {
		if (isPlaying()) {
			if (isLivingPlayerPlayed) {
				reset();
			} else {
				pause();
				notifyAutoStateChanged();
			}
			mReplay = true;
		}
	}

	public boolean ismIsLiveComplete() {
		return mIsLiveComplete;
	}

	public void setmIsLiveComplete(boolean mIsLiveComplete) {
		this.mIsLiveComplete = mIsLiveComplete;
	}

	/**
	 * 拖动播放器到某个时间点
	 * 
	 * @param seekTime
	 */
	public void seekTo(long seekTime) {
		if ((mMediaPlayer != null && mMediaPlayer.getDuration() < 1)) {
			notifyLiveSeekComplete();
			return;
		}
		if (mDuration == seekTime || mMediaPlayer == null) {
			notifyLiveSeekComplete();
			notifyLiveEnd();
			return;
		}
		try {
			isLivePlayerPlayingBeforeSeek = isLivePlayerPlaying();
			mNotNetBeforePotison = seekTime;
			if (isLivePlayerPlayingBeforeSeek) {
				mMediaPlayer.pause();
			}
			mMediaPlayer.seekTo(seekTime);
		} catch (IllegalStateException ill) {
			Log.d(TAG, "seekTo time {}---error：{}" + seekTime + ill.toString());
			notifyLiveSeekComplete();
		}
	}

	/**
	 * 启动播放器
	 */
	public void start() {
		if (mMediaPlayer == null || isPlaying()) {
			return;
		}
		try {
			mMediaPlayer.start();
			isLivePlayerPlaying = true;
			requestAudioFocus();
			notifyStateChanged();
			Log.d(TAG, "notifyStateChanged,line 1182");
			// KaolaNotificationManager.getInstance(mContext)
			// .updateLiveNotification();// 通知状态栏播放按钮变化
		} catch (IllegalStateException ill) {
			Log.d(TAG, "start MediaPlayer error：{}" + ill.toString());
		}
	}

	/**
	 * 暂停播放器
	 */
	public void pause() {
		if (!isPlaying()) {
			return;
		}

		try {
			mMediaPlayer.pause();
			isLivePlayerPlaying = false;
			abandonAudioFocus();
			notifyStateChanged();
			Log.d(TAG, "notifyStateChanged,line 1203");
			// KaolaNotificationManager.getInstance(mContext)
			// .updateLiveNotification(); // 通知状态栏播放按钮变化
		} catch (IllegalStateException ill) {
			Log.d(TAG, "pause MediaPlayer error：{}" + ill.toString());
		}
	}

	/**
	 * 获取播放器总时长
	 */
	public long getDuration() {
		return mDuration;
	}

	/**
	 * 获取当前播放器播放时间
	 * 
	 * @return
	 */
	public long getCurrentPosition() {
		return mCurrentPosition;
	}

	// -----------------------回调-----------------
	public interface OnStateChangedListener {
		void onStateChanged();

		void onAutoStateChanged();
	}

	public interface OnLivePlayerDisAbleListener {
		void onLivePlayerDisable();
	}

	public interface OnLiveSeekCompleteListener {
		void onLiveSeekComplete();
	}

	public interface OnLiveEndListener {
		void onLiveEnd();
	}

	public interface OnLiveStreamBufferListener {
		void onLiveStreamBuffer();
	}

	public interface OnLiveStreamBufferStartListener {
		void onLiveStreamBufferStart();
	}

	public interface OnLiveStreamBufferEndListener {
		void onLiveStreamBufferEnd();
	}

	public interface OnProcessListener {
		void onProcess(int position, int duration);
	}

	public interface OnLivePlayerStatusChangeListener {
		void onCompletion();

		void onAnchorConnectFailed();

		void onPrepared();

		void onReseted();
	}

	public interface OnNetWorkStatusChangedListener {
		void onNetWorkStatusChanged(boolean isAlive);
	}

	// -----------------------回调-----------------

	/**
	 * 向直播播放器中添加Seek成功回调事件
	 * 
	 * @param onLiveSeekCompleteListener
	 */
	public void addLiveSeekCompleteListener(
		OnLiveSeekCompleteListener onLiveSeekCompleteListener) {
		if (mOnLiveSeekCompleteListeners.contains(onLiveSeekCompleteListener)) {
			return;
		}
		mOnLiveSeekCompleteListeners.add(onLiveSeekCompleteListener);
	}

	/**
	 * 删除直播播放器中添加的seek成功回调事件
	 * 
	 * @param onLiveSeekCompleteListener
	 */
	public void removeLiveSeekCompleteListener(
		OnLiveSeekCompleteListener onLiveSeekCompleteListener) {
		if (mOnLiveSeekCompleteListeners.contains(onLiveSeekCompleteListener)) {
			mOnLiveSeekCompleteListeners.remove(onLiveSeekCompleteListener);
		}
	}

	/**
	 * add直播结束回调事件
	 * 
	 * @param onLiveEndListener
	 */
	public void addLiveEndListener(OnLiveEndListener onLiveEndListener) {
		this.mOnLiveEndListeners.add(onLiveEndListener);
	}

	/**
	 * del直播结束回调事件
	 */
	public void removeLiveEndListener(OnLiveEndListener onLiveEndListener) {
		if (mOnLiveEndListeners.contains(onLiveEndListener)) {
			mOnLiveEndListeners.remove(onLiveEndListener);
		}
	}

	/**
	 * 网络变化回调事件
	 */
	public void addOnNetWorkStatusChangedListener(
		OnNetWorkStatusChangedListener onNetWorkStatusChangedListener) {
		this.mNetWorkStatusChangedListener = onNetWorkStatusChangedListener;
	}

	/**
	 * 直播缓冲开始回调 ijk
	 * 
	 * @param onLiveStreamBufferStartListener
	 */
	public void addOnLiveStreamBufferStartListener(
		OnLiveStreamBufferStartListener onLiveStreamBufferStartListener) {
		if (mOnLiveStreamBufferStartListeners
			.contains(onLiveStreamBufferStartListener)) {
			return;
		}
		mOnLiveStreamBufferStartListeners.add(onLiveStreamBufferStartListener);
	}

	/**
	 * 直播缓冲结束回调 ijk
	 * 
	 * @param mOnLiveStreamBufferEndListener
	 */
	public void addOnLiveStreamBufferEndListener(
		OnLiveStreamBufferEndListener mOnLiveStreamBufferEndListener) {
		if (mOnLiveStreamBufferEndListeners
			.contains(mOnLiveStreamBufferEndListener)) {
			return;
		}
		mOnLiveStreamBufferEndListeners.add(mOnLiveStreamBufferEndListener);
	}

	/**
	 * 直播缓冲回调
	 * 
	 * @param onLiveStreamBufferListener
	 */
	public void addOnLiveStreamBufferListener(
		OnLiveStreamBufferListener onLiveStreamBufferListener) {
		if (mOnLiveStreamBufferListener.contains(onLiveStreamBufferListener)) {
			return;
		}
		mOnLiveStreamBufferListener.add(onLiveStreamBufferListener);
	}

	/**
	 * 删除直播缓冲回调
	 * 
	 * @param onLiveStreamBufferListener
	 */
	public void removeLiveStreamBufferListener(
		OnLiveStreamBufferListener onLiveStreamBufferListener) {
		if (mOnLiveStreamBufferListener.contains(onLiveStreamBufferListener)) {
			mOnLiveStreamBufferListener.remove(onLiveStreamBufferListener);
		}
	}

	/**
	 * 向直播播放器中添加播放进度回调事件
	 * 
	 * @param onProcessListener
	 */
	public void addProcessListener(OnProcessListener onProcessListener) {
		if (mOnProcessListeners.contains(onProcessListener)) {
			return;
		}
		mOnProcessListeners.add(onProcessListener);
	}

	/**
	 * 删除直播播放器中添加的播放进度回调事件
	 * 
	 * @param onProcessListener
	 */
	public void removeProcessListener(OnProcessListener onProcessListener) {
		if (mOnProcessListeners.contains(onProcessListener)) {
			mOnProcessListeners.remove(onProcessListener);
		}
	}

	/**
	 * 删除缓冲开始回调事件
	 * 
	 * @param onLiveStreamBufferStartListener
	 */
	public void removeLiveStreamBufferStartListener(
		OnLiveStreamBufferStartListener onLiveStreamBufferStartListener) {
		if (mOnLiveStreamBufferStartListeners
			.contains(onLiveStreamBufferStartListener)) {
			mOnLiveStreamBufferStartListeners
				.remove(onLiveStreamBufferStartListener);
		}
	}

	/**
	 * 删除缓冲结束回调事件
	 * 
	 * @param onLiveStreamBufferEndListener
	 */
	public void removeLiveStreamBufferEndListener(
		OnLiveStreamBufferEndListener onLiveStreamBufferEndListener) {
		if (mOnLiveStreamBufferEndListeners
			.contains(onLiveStreamBufferEndListener)) {
			mOnLiveStreamBufferEndListeners
				.remove(onLiveStreamBufferEndListener);
		}
	}

	public void addOnLivePlayerStatusChangeListener(
		OnLivePlayerStatusChangeListener onLivePlayerStatusChangeListener) {
		if (mOnLivePlayerStatusChangeListener
			.contains(onLivePlayerStatusChangeListener)) {
			return;
		}
		mOnLivePlayerStatusChangeListener.add(onLivePlayerStatusChangeListener);
	}

	public void removeOnLivePlayerStatusChangeListener(
		OnLivePlayerStatusChangeListener onLivePlayerStatusChangeListener) {
		if (mOnLivePlayerStatusChangeListener
			.contains(onLivePlayerStatusChangeListener)) {
			mOnLivePlayerStatusChangeListener
				.remove(onLivePlayerStatusChangeListener);
		}
	}
}
