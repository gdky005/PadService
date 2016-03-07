package com.kaolafm.live.player;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gdky005.padservice.R;
import com.kaolafm.live.utils.DateFormatUtil;
import com.kaolafm.live.utils.LivePlayerManager;
import com.kaolafm.live.utils.LivePlayerManager.OnProcessListener;

import java.io.File;
import java.util.Random;

public class IjkMainActivity extends Activity implements OnClickListener {

	private LivePlayerManager mLivePlayerManager;

	private SeekBar mSeekBar;

	private TextView tv_duration;
	private TextView tv_current_duration_textView;

	private CustomOnSeekBarChangeListener mOnSeekBarChangeListener = new CustomOnSeekBarChangeListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ijkmedia_main);
		tv_duration = (TextView) findViewById(R.id.tv_duration);
		tv_current_duration_textView = (TextView) findViewById(R.id.tv_current_duration_textView);

		mSeekBar = (SeekBar) findViewById(R.id.seekbar);
		mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		findViewById(R.id.resume_button).setOnClickListener(this);
		findViewById(R.id.pause_button).setOnClickListener(this);
		findViewById(R.id.play_button).setOnClickListener(this);
		findViewById(R.id.reset_button).setOnClickListener(this);
		mLivePlayerManager = LivePlayerManager.getInstance(this);

		mLivePlayerManager.addProcessListener(new OnProcessListener() {

			@Override
			public void onProcess(int position, int duration) {
				// TODO Auto-generated method stub
				int maxValue = mSeekBar.getMax();
				System.out.println("position -----------> " + position
					+ "---> duration = " + duration + "---->" + maxValue
					+ "--->" + ((long) position));
				if (maxValue != duration) { // 09-22 12:17:21.984:
											// I/System.out(18433): position
											// -----------> 8590021031--->
											// duration =
											// 8589934592---->0--->86439
					mSeekBar.setMax((int) duration);
				}
				mSeekBar.setProgress((int) position);
			}
		});
	}

	private void refreshTime(boolean fromUser, int progress, int duration) {
		if (fromUser
			|| (mOnSeekBarChangeListener != null && !mOnSeekBarChangeListener.isOnStartTrackingTouch)) {
			tv_duration.setText(DateFormatUtil.getDescriptiveTime(duration));
			tv_current_duration_textView.setText(DateFormatUtil
				.getDescriptiveTime(progress));
		}
	}

	private class CustomOnSeekBarChangeListener implements
		SeekBar.OnSeekBarChangeListener {
		public boolean isOnStartTrackingTouch = false;

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
			// adjustThumbTextPosition(progress);
			System.out.println("fromUser = " + fromUser);
			refreshTime(fromUser, progress, seekBar.getMax());
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// mThumbTextView.setChecked(true);
			isOnStartTrackingTouch = true;
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			isOnStartTrackingTouch = false;
			Message msg = mHandler.obtainMessage();
			msg.what = SEEK_PLAYER_MSG;
			msg.arg1 = seekBar.getProgress();
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * SEEK消息
	 */
	private static final int SEEK_PLAYER_MSG = 1;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SEEK_PLAYER_MSG:
				mLivePlayerManager.seekTo(msg.arg1);
				break;
			default:
				break;
			}
		}
	};
	 public String getInnerSDCardPath() { 
		 
		  File sd=Environment.getExternalStorageDirectory(); 
		  String path=sd.getPath()+"/zhangyf"; 
		  File file=new File(path); 
		  if(!file.exists())  
			  file.mkdir(); 
		  return Environment.getExternalStorageDirectory().getPath(); 
	 }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.pause_button:
			mLivePlayerManager.pause();
			break;
		case R.id.resume_button:
			mLivePlayerManager.start();
			break;
		case R.id.play_button:
			// mLivePlayerManager
			// .start("http://play.kaolafm.net/20150901/1111111111_4444444444/playlist.m3u8");
			// http://play.kaolafm.net/20150901/1111111111_4444444444/playlist.m3u8
			Random random = new Random();
			int index = random.nextInt(mLiveUrls.length);
			if (index >= mLiveUrls.length) {
				return;
			}
			String liveUrl = mLiveUrls[index];
			Log.d("start = ", liveUrl);
			//String strfile = "/sdcard/tmp/test_mp3.aac";
			//String strfile = "/sdcard/tmp/test.aac";
			//String strfile = "/sdcard/tmp/123.opus";
			//mLivePlayerManager.start("http://play.kaolafm.net/20160111/abc_128/playlist.m3u8");
			//String strfile = "http://192.168.5.220/hongri.mp3";
			//String strfile = "http://192.168.5.220/123.opus";
			//String strfile = "http://192.168.5.220/by.aac";
			String strfile = "http://trslbs.kaolafm.com/016f63815d64d4db/1600000000510/1456977600000_1456981199000.m3u8";
			//String strfile = "rtmp://rtmp.hsrtv.cn/live/radio3";
			//String strfile= "/sdcard/tmp/hongri.mp3";
			mLivePlayerManager.start(strfile);
			break;
		case R.id.reset_button:
			mLivePlayerManager.reset();
			break;
		default:
			break;
		}
	}

	private String[] mLiveUrls = {
		"http://vod.kaolafm.net/kaolafm/1441598797_1583887407/playlist.m3u8",
		"http://vod.kaolafm.net/kaolafm/1423623156_1524315748/playlist.m3u8",
		"http://vod.kaolafm.net/kaolafm/1437170094_1544655850/playlist.m3u8",
		"http://vod.kaolafm.net/kaolafm/1421523003_1520603338/playlist.m3u8",
		"http://vod.kaolafm.net/kaolafm/1474716320_1541043734/playlist.m3u8",
		"http://vod.kaolafm.net/kaolafm/1455147418_1591103563/playlist.m3u8",
		"http://vod.kaolafm.net/kaolafm/1485294841_1506625248/playlist.m3u8",
		"http://vod.kaolafm.net/kaolafm/1405016347_1572193062/playlist.m3u8",
		"http://vod.kaolafm.net/kaolafm/1404565016_1529333794/playlist.m3u8",
		"http://vod.kaolafm.net/kaolafm/1429962088_1586288964/playlist.m3u8",
		"http://vod.kaolafm.net/kaolafm/1411150565_1556677313/playlist.m3u8",
		"http://vod.kaolafm.net/kaolafm/1494463447_1521276287/playlist.m3u8",
		"http://vod.kaolafm.net/kaolafm/1452244057_1578069773/playlist.m3u8" };
}
