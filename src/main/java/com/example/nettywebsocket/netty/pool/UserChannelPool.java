package com.example.nettywebsocket.netty.pool;

import com.alibaba.fastjson.JSON;
import com.example.nettywebsocket.netty.utils.response.ObjectResponseResult;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserChannelPool {
    // key:用户名， value：channel
    public static Map<String, Channel> channelMap = new ConcurrentHashMap<>();
    // netty提供的对websocketServer中所有channel的管理
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    /**
     * 发消息给所有用户
     * @param objectResponseResult
     */
    public static void sendToAllUser(ObjectResponseResult objectResponseResult) {
        for (Channel channel : channels) {
            sendMessage(channel, objectResponseResult);
            channel.flush();
        }
    }

    /**
     * 发消息给一个用户
     * @param user
     * @param objectResponseResult
     */
    public static void sendToUser(String user, ObjectResponseResult objectResponseResult) {
        Channel channel = channelMap.get(user);
        if (channel != null) {
            sendMessage(channel, objectResponseResult);
        }
    }

    /**
     * 发消息给当前用户
     * @param channel
     * @param objectResponseResult
     */
    public static void sendMessage(Channel channel, ObjectResponseResult objectResponseResult) {
        channel.write(resultToFrame(objectResponseResult));
        channel.flush();
    }

    public static TextWebSocketFrame resultToFrame(ObjectResponseResult objectResponseResult) {
        return new TextWebSocketFrame(JSON.toJSONString(objectResponseResult));
    }
}
