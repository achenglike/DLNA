package com.imooc.dlna.mediaserver.config;

public interface IWebConfig {
    
    /**
     * 媒体服务器的host
     * @return
     */
    public String getHost();
    
    /**
     * 媒体服务器的端口
     * @return
     */
    public int getPort();

}
