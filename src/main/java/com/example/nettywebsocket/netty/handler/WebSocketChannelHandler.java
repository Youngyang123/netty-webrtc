package com.example.nettywebsocket.netty.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.nettywebsocket.netty.pool.UserChannelPool;
import com.example.nettywebsocket.netty.utils.response.ObjectResponseResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebSocketChannelHandler extends SimpleChannelInboundHandler<Object> {

    //    @Value("${netty.url}")
    private String websocketUrl = "ws://localhost:8008/websocket";

    private WebSocketServerHandshaker handshaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {
        // 判断msg 是哪一种类型 分别做出不同的反应
        if (msg instanceof CloseWebSocketFrame) {
            log.info("[关闭]");
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) msg);
            return;
        }

        if (!(msg instanceof TextWebSocketFrame)) {
            log.info("只支持文本");

            throw new UnsupportedOperationException("只支持文本");
        }

        TextWebSocketFrame t_frame = (TextWebSocketFrame) msg;
        String text = t_frame.text();
        JSONObject jsonObject = JSON.parseObject(text);
        String type = jsonObject.get("type").toString();
        log.info("[type={}]", type);
        String name = null;
        String sendToName = null;
        switch (type) {
            case "login":
                name = jsonObject.get("name").toString();
                // 用户已经存在
                if (UserChannelPool.channelMap.containsKey(name)) {
                    log.info("[{}]--用户已存在", name);
                    ObjectResponseResult objectResponseResult = ObjectResponseResult.result()
                            .success(false)
                            .type("login");
                    UserChannelPool.sendMessage(ctx.channel(), objectResponseResult);
                    return;
                }
                //
                log.info("[{}]--登录成功", name);
                UserChannelPool.channelMap.put(name, ctx.channel());
                UserChannelPool.sendMessage(ctx.channel(), ObjectResponseResult.result().type("login"));
                return;
            case "offer":
                sendToName = jsonObject.get("name").toString();
                log.info("发送offer给[{}]", sendToName);
                UserChannelPool.sendToUser(sendToName, ObjectResponseResult.result()
                        .offer(jsonObject.get("offer"))
                        .type("offer"));
                return ;

            case "answer":
                name = jsonObject.get("name").toString();
                UserChannelPool.sendToUser(name, ObjectResponseResult.result()
                        .answer(jsonObject.get("answer"))
                        .type("answer"));
                return ;
            case "candidate":
                name = jsonObject.get("name").toString();
                UserChannelPool.sendToUser(name, ObjectResponseResult.result()
                        .answer(jsonObject.get("candidate"))
                        .type("candidate"));

            case "leave":
                name = jsonObject.get("name").toString();
                UserChannelPool.sendToUser(name, ObjectResponseResult.result()
                        .type("leave"));

        }

    }


    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest msg) {
        // http 解码失败
        String tmp = msg.headers().get("Upgrade");
        if (!msg.decoderResult().isSuccess() || (!"websocket".equals(msg.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, (FullHttpRequest) msg,
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
        }

        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(
                websocketUrl, null, false);
        handshaker = factory.newHandshaker(msg);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }
        // 进行连接
        handshaker.handshake(ctx.channel(), (FullHttpRequest) msg);
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


}
