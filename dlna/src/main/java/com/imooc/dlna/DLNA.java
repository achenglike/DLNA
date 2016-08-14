package com.imooc.dlna;

import android.content.Context;

import com.imooc.dlna.dmc.IController;
import com.imooc.dlna.dmc.MultiPointController;
import com.imooc.dlna.mediaserver.SimpleWebServer;
import com.imooc.dlna.mediaserver.config.DefaultWebConfig;
import com.imooc.dlna.mediaserver.config.IWebConfig;

import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DLNA {
    
    private static final String TAG = "DLNA insatnce";
    private static final String MUTE = "1";
    private static final String UNMUTE = "0";
    private static final String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";
    
    private static DLNA singleton;
    
    private ControlPoint mControlPoint;
    private IController mControlHander;
    // 媒体服务器
    private SimpleWebServer mSimpleWebServer;
    private IWebConfig webConfig;
    
    // 线程池
    private ExecutorService EXECUTOR_POOL = Executors.newCachedThreadPool();
    
    // 用户选定的Device
    private Device selectedDevice;

    public void init(Context context, DeviceChangeListener listener) {
        this.init(listener, new DefaultWebConfig(context));
    }
    
    public void init(DeviceChangeListener listener, IWebConfig webConfig) {
        this.webConfig = webConfig;
        if (mControlPoint == null) {
            mControlPoint = new ControlPoint();
            mControlPoint.addDeviceChangeListener(listener);
            EXECUTOR_POOL.execute(new Runnable() {
                @Override
                public void run() {
                    mControlPoint.start();
                }
            });
            
        }
        if (mControlHander == null) {
            mControlHander = new MultiPointController();
        }
    }
    
    public void search() {
        if (mControlPoint != null) {
            EXECUTOR_POOL.execute(new Runnable() {
                
                @Override
                public void run() {
                    mControlPoint.search();
                }
            });
        }
    }
    
    public void stop() {
        // 关闭nanohttpd
        if (mSimpleWebServer != null) {
            mSimpleWebServer.stop();
        }
        // 释放control
        EXECUTOR_POOL.execute(new Runnable() {
            @Override
            public void run() {
                mControlHander.stop(selectedDevice);
            }
        });
        mControlPoint.stop();
    }
    
    /**
     * 播放本地媒体
     * @param fileAbsPath
     */
    public void playSDCardMedia(String fileAbsPath) {
        if (fileAbsPath == null) {
            throw new NullPointerException("fileAbsPath could't be null");
        }
        safeStateChecker();
        startMediaServer();
        if (fileAbsPath.startsWith("/")) {
            fileAbsPath = fileAbsPath.substring(1);
        }
        final String playUrl = String.format("http://%s:%d/%s", webConfig.getHost(), webConfig.getPort(), fileAbsPath);
        EXECUTOR_POOL.execute(new Runnable() {
            
            @Override
            public void run() {
                mControlHander.play(selectedDevice, playUrl);
            }
        });
    }
    
    /**
     * 打开媒体服务器以传输本地内容
     * @return
     */
    private boolean startMediaServer() {
        // 开启nanohttpd
        if (mSimpleWebServer == null)
            mSimpleWebServer = new SimpleWebServer(webConfig.getHost(), webConfig.getPort(), new File("/"), true);
        if (!mSimpleWebServer.isAlive()) {
            try {
                mSimpleWebServer.stop();
                mSimpleWebServer.start();
            } catch (IOException ioe) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 播放在线媒体
     * @param url
     */
    public void playMedia(final String url) {
        if (url == null) {
            throw new NullPointerException("url is null");
        }
        safeStateChecker();
        EXECUTOR_POOL.execute(new Runnable() {
            
            @Override
            public void run() {
                mControlHander.play(selectedDevice, url);
            }
        });
    }
    /**
     * 以下内容被注释掉
     * ① 现在不需要
     * ②IController 方法的使用需要另起线程，所以以下方法还不能用
     * 
     * IController(#mControlHander)所有的操作都需要另起线程
     */
//    /**
//     * 在pausePosition位置继续播放
//     * @param pausePosition
//     * @return
//     */
//    public boolean goon(String pausePosition) {
//        safeStateChecker();
//        final boolean isSuccess = mControlHander.goon(selectedDevice,
//                pausePosition);
//        LogUtil.d(TAG, isSuccess ? "play success" :"play failed..");
//        return isSuccess;
//    }
//
//    /**
//     * 获得device状态  
//     * @return "STOPPED" // "PLAYING" // "TRANSITIONING"//
//     * "PAUSED_PLAYBACK"// "PAUSED_RECORDING"// "RECORDING" //
//     * "NO_MEDIA_PRESENT//
//     */
//    public String getTransportState(Device device) {
//        safeStateChecker();
//        return mControlHander.getTransportState(selectedDevice);
//    }
//
//    /**
//     * 最小音量值
//     * @return
//     */
//    public int getMinVolumeValue() {
//        return 0;
//    }
//
//    /**
//     * 最大音量值
//     * @return
//     */
//    public int getMaxVolumeValue() {
//        safeStateChecker();
//        final int maxVolumnValue = mControlHander
//                .getMaxVolumeValue(selectedDevice);
//        if (maxVolumnValue <= 0) {
//            LogUtil.d(TAG, "get current voice failed");
//            return 100;
//        }
//        return maxVolumnValue;
//    }
//
//    /**
//     * 滑动到targetPosition位置进行播放
//     * @param targetPosition 格式 "00:00:00"
//     */
//    public boolean seek(String targetPosition) {
//        safeStateChecker();
//        boolean isSuccess = mControlHander.seek(selectedDevice, targetPosition);
//        LogUtil.d(TAG, isSuccess ? "play success" :"play failed..");
//        return isSuccess;
//    }
//
//    /**
//     * 或得到当前播放的位置.
//     * @return null 或者00:00:00格式的时间
//     */
//    public String getPositionInfo() {
//        safeStateChecker();
//        String position = mControlHander.getPositionInfo(selectedDevice);
//        LogUtil.d(TAG, "Get position info and the value is " + position);
//        return (TextUtils.isEmpty(position)
//                || NOT_IMPLEMENTED.equals(position)) ? null : position;
//    }
//
//    /**
//     * 获得媒体总时长
//     * @return 00:00:00
//     */
//    public String getMediaDuration() {
//        safeStateChecker();
//        final String mediaDuration = mControlHander
//                .getMediaDuration(selectedDevice);
//        LogUtil.d(TAG, "Get media duration and the value is "
//                + mediaDuration);
//        return (TextUtils.isEmpty(mediaDuration)
//                || NOT_IMPLEMENTED.equals(mediaDuration)) ? null : mediaDuration;
//    }
//
//    /**
//     * 设置静音
//     * @param targetValue   true /false
//     */
//    public boolean setMute(boolean targetValue) {
//        safeStateChecker();
//        return mControlHander.setMute(selectedDevice, targetValue ? MUTE : UNMUTE);
//    }
//
//    /**
//     * 是否静音状态
//     */
//    public boolean getMute() {
//        safeStateChecker();
//        final String mute = mControlHander.getMute(selectedDevice);
//        if (mute == null) {
//            LogUtil.d(TAG, "get mute failed...");
//            return false;
//        } else {
//            return  mute.equals(MUTE) ? true : false;
//        }
//    }
//
//    /**
//     * 设置音量
//     * @param voice
//     */
//    public boolean setVoice(int value) {
//        safeStateChecker();
//        return mControlHander.setVoice(selectedDevice, value);
//    }
//
//    /**
//     * 获得音量
//     * @return
//     */
//    public int getVoice() {
//        safeStateChecker();
//        return mControlHander.getVoice(selectedDevice);
//    }
//
//    /**
//     * 停止播放
//     */
//    public boolean stopPlay() {
//        safeStateChecker();
//        return mControlHander.stop(selectedDevice);
//    }
//
//    /**
//     * 暂停播放
//     */
//    public boolean pause(Device device) {
//        safeStateChecker();
//        final boolean isSuccess = mControlHander.pause(selectedDevice);
//        LogUtil.d(TAG, isSuccess ? "play success" :"play failed..");
//        return isSuccess;
//    }
//    
    public Device getSelectedDevice() {
        return selectedDevice;
    }

    public void setSelectedDevice(Device selectedDevice) {
        this.selectedDevice = selectedDevice;
    }

    public static DLNA getInstance() {
        if(singleton==null){
            singleton=new DLNA();
        }
        return singleton;
    }
    
    public static void destroyDlan(){
        singleton = null;
    }
    private void safeStateChecker() {
        if (selectedDevice == null)
            throw new IllegalStateException("setOperatedDevice haven't been called");
    }
}
