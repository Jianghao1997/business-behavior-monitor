package com.hoodee.monitor.sdk.push;


import com.hoodee.monitor.sdk.model.LogMessage;

/**
 * @author jh
 * @Description
 * @Date 2024/12/14
 */
public interface IPush {

    void open(String host, Integer port);

    void send(LogMessage logMessage);
}
