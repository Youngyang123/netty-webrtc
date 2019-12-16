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
    public static Map<String, Channel> channelMap = new ConcurrentHashMap<>();
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    public static void sendToAllUser(ObjectResponseResult objectResponseResult) {
        for (Channel channel : channels) {
            sendMessage(channel, objectResponseResult);
            channel.flush();
        }
    }

    public static void sendToUser(String user, ObjectResponseResult objectResponseResult) {
        Channel channel = channelMap.get(user);
        if (channel != null) {
            sendMessage(channel, objectResponseResult);
        }
    }

    public static void sendMessage(Channel channel, ObjectResponseResult objectResponseResult) {
        channel.write(resultToFrame(objectResponseResult));
        channel.flush();
    }

    public static TextWebSocketFrame resultToFrame(ObjectResponseResult objectResponseResult) {
        return new TextWebSocketFrame(JSON.toJSONString(objectResponseResult));
    }
}
