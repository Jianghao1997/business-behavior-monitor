package com.hoodee.monitor.sdk.push.impl;

import com.alibaba.fastjson2.JSON;
import com.hoodee.monitor.sdk.model.LogMessage;
import com.hoodee.monitor.sdk.push.IPush;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jh
 * @Description
 * @Date 2024/12/14
 */
public class RedisPush implements IPush {

    private final Logger logger = LoggerFactory.getLogger(RedisPush.class);

    private RedissonClient redissonClient;

    @Override
    public synchronized void open(String host, Integer port) {
        if (redissonClient != null && !redissonClient.isShutdown()) {
            return;
        }
        Config config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE);
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(10)
                .setIdleConnectionTimeout(1000)
                .setConnectTimeout(1000)
                .setRetryAttempts(3)
                .setRetryInterval(1000)
                .setPingConnectionInterval(0)
                .setKeepAlive(true);
        this.redissonClient = Redisson.create(config);
        RTopic topic = this.redissonClient.getTopic("business-behavior-monitor-sdk-topic");
        topic.addListener(LogMessage.class, new Listener());

    }

    class Listener implements MessageListener<LogMessage> {

        @Override
        public void onMessage(CharSequence charSequence, LogMessage logMessage) {
            logger.info("接收消息：{}", JSON.toJSONString(logMessage));
        }
    }

    @Override
    public void send(LogMessage logMessage) {
        try {
            RTopic topic = redissonClient.getTopic("business-behavior-monitor-sdk-topic");
            topic.publish(logMessage);
        } catch (Exception e) {
            logger.error("警告: 业务行为监控组件，推送日志消息失败", e);
        }
    }

}