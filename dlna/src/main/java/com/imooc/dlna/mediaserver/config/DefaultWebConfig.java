package com.imooc.dlna.mediaserver.config;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;


public class DefaultWebConfig implements IWebConfig {

    Context applicationContext;
    
    public DefaultWebConfig(Context mContext) {
        this.applicationContext = mContext.getApplicationContext();
    }

    @Override
    public String getHost() {
        // TODO Auto-generated method stub
        return getDeviceIp();
    }

    @Override
    public int getPort() {
        // TODO Auto-generated method stub
        return 12432;
    }

    private String getDeviceIp() {
        WifiManager wm = (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }
}
