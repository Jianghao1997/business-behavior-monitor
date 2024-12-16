package com.hoodee.monitor.sdk.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.hoodee.monitor.sdk.model.LogMessage;
import com.hoodee.monitor.sdk.push.IPush;
import com.hoodee.monitor.sdk.push.impl.RedisPush;

import java.util.Arrays;

/**
 * @author jh
 * @Description
 * @Date 2024/12/14
 */
public class CustomAppender<E> extends AppenderBase<E> {

    private String systemName;

    private String groupId;

    private String host;

    private int port;

    private final IPush push = new RedisPush();

    @Override
    protected void append(E eventObject) {
        if (eventObject instanceof ILoggingEvent) {
            ILoggingEvent event = (ILoggingEvent) eventObject;
            String methodName = "unknown";
            String className = "unknown";
            StackTraceElement[] callerDataArray = event.getCallerData();
            if (callerDataArray != null && callerDataArray.length > 0) {
                StackTraceElement caller = callerDataArray[0];
                methodName = caller.getMethodName();
                className = caller.getClassName();
            }
            if (groupId != null && !className.startsWith(groupId)) {
                return;
            }
            LogMessage logMessage = new LogMessage(
                    systemName, className, methodName,
                    Arrays.asList(event.getFormattedMessage().split(" "))
            );
            push.open(host, port);
            push.send(logMessage);
        }
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
