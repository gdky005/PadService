package com.gdky005.padservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.gdky005.padservice.service.PadService;
import com.gdky005.padservice.utils.ServiceIntent;
import com.kaolafm.mediaplayer.PlayItem;
import com.kaolafm.mediaplayer.PlayerService;

public class MainActivity extends Activity implements View.OnClickListener {

    public static final String PAD_SERVICE_FLAG = "com.gdky005.PAD_SERVICE";

    ServiceConnection connection;

    PadService padService;

    String mp3UrlSJK;
    String m3u8UrlZB;
    String mp3Url;

    static {
        System.loadLibrary("kaolafmutil");
        System.loadLibrary("kaolafmsdl");
        System.loadLibrary("kaolafmffmpeg");
        System.loadLibrary("kaolafmplayer");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        connection = new PadServiceConnection();

        mp3UrlSJK = "http://image.kaolafm" +
                ".net/mz/audios/201603/b860d25b-87e1-4901-9fd1-44ce4ddc6e61.mp3";

        m3u8UrlZB = "http://ugclbs.kaolafm" +
                ".com/aaf30a55ec51aa6f/1486224311_1585306448/playlist.m3u8";

        mp3Url = "http://other.web.rh01.sycdn.kuwo" +
                ".cn/7abff6cff1ad4ab6f7418ab5dee80b97/56d65047/resource/n3/69/22/26734815.mp3";


//        VLCMediaPlayClient.getInstance().bindVLCPlayService(
//                getApplicationContext());
//
//
//        VLCMediaPlayClient.getInstance().sendPlayURLToService(m3u8UrlZB,
//                "测试标题",
//                0,
//                0,
//                0,
//                false);

//        bindService();


    }

    private void bindService() {
        bindService(new Intent(this, PlayerService.class),
                this.mPlayerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    PlayerService.PlayerBinder mPlayerBinder;

    private ServiceConnection mPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder iBinder) {
            if (iBinder instanceof PlayerService.PlayerBinder) {
                mPlayerBinder = (PlayerService.PlayerBinder) iBinder;
            }


            try {
                PlayItem playItem = new PlayItem();
                playItem.setPlayUrl(mp3UrlSJK);
                mPlayerBinder.start(playItem);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
//            try {
//                mPlayerBinder.addPlayerStateListener(mPlayerStateListener);
//                for (int i = 0, size = mUnAddedPlayerStateListeners.size(); i < size; i++) {
//                    PlayerService.IPlayerStateListener listener = mUnAddedPlayerStateListeners.get(i);
//                    if (listener == null) {
//                        continue;
//                    }
//                    mPlayerBinder.addPlayerStateListener(listener);
//                }
////                for (PlayerService.IPlayerStateListener listener : mUnAddedPlayerStateListeners) {
////                    mPlayerBinder.addPlayerStateListener(listener);
////                }
//                mUnAddedPlayerStateListeners.clear();
//            } catch (Throwable e) {
//                e.printStackTrace();
//                logger.error("Add player state listener error.");
//            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
//            PlayerBroadcastRadioManager playerBroadcastRadioManager = PlayerBroadcastRadioManager.getInstance(mContext);
//            playerBroadcastRadioManager.removePlayerStateListener(playerBroadcastRadioManager.getPlayerStateListener());
//            try {
//                mPlayerBinder.removePlayerStateListener(mPlayerStateListener);
//            } catch (Throwable e) {
//                e.printStackTrace();
//                logger.error("Remove player state listener error.");
//            }
            mPlayerBinder = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        VLCMediaPlayClient.getInstance().unBindVLCPlayService();
    }

    private class PadServiceConnection implements ServiceConnection {

//        只有在MyService中的onBind方法中返回一个IBinder实例才会在Bind的时候
//        调用onServiceConnection回调方法
//        第二个参数service就是MyService中onBind方法return的那个IBinder实例，可以利用这个来传递数据

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            log("onServiceConnected: ");

            PadService.PadBinder padBinder = (PadService.PadBinder) service;

            padService = padBinder.getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            /* SDK上是这么说的：
                 * This is called when the connection with the service has been unexpectedly disconnected
                 * that is, its process crashed. Because it is running in our same process, we should never see this happen.
                 * 所以说，只有在service因异常而断开连接的时候，这个方法才会用到*/
            log("onServiceDisconnected: ");

            connection = null;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bingService:
                bind();
                break;
            case R.id.startService:
                start();
                break;
            case R.id.stopService:
                stop();
                break;
            case R.id.unbindService:
                unbind();
                break;
        }
    }

    public void start() {
        log("Start button clicked");

        Intent service = new Intent(this, PadService.class);
        startService(service);
    }

    public void stop() {
        log("stop button clicked");
        stopService(getPadIntent());
    }


    public void bind() {
//        Intent intent = new Intent(LocalServiceTestActivity.this, MyService.class);//这样也可以的

        log("bind button clicked");
        bindService(getPadIntent(), connection, Context.BIND_AUTO_CREATE);//bind多次也只会调用一次onBind方法
    }

    public void unbind() {
//        这边如果重复unBind会报错，提示该服务没有注册的错误——IllegalArgumentException:
//        // Service not registered: null
//        // 所以一般会设置一个flag去看这个service
//        // bind后有没有被unBind过，没有unBind过才能调用unBind方法(这边我就不设置了哈~\(≧▽≦)/~啦啦啦)

        log("unbind button clicked");
        unbindService(connection);
    }

    @NonNull
    private Intent getPadIntent() {
        return ServiceIntent.getPadIntent(this);
    }

    public void log(String message) {
        Log.i(MainActivity.class.getSimpleName(), message);
    }
}
