package com.example.nettywebsocket.netty.server;

import com.example.nettywebsocket.netty.handler.WebSocketChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

@Component
public class WebSocketServer {

    public void start(int port) {
        // 服务端需要2个线程组  boss处理客户端连接  work进行客服端连接之后的处理
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            // HttpServerCodec：将请求和应答消息解码为HTTP消息
                            pipeline.addLast("http-codec", new HttpServerCodec()); // Http消息编码解码
                            // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
                            pipeline.addLast("aggregator", new HttpObjectAggregator(65536)); // Http消息组装
                            // ChunkedWriteHandler：向客户端发送HTML5文件
                            pipeline.addLast("http-chunked", new ChunkedWriteHandler()); // WebSocket通信支持
                            // 进行设置心跳检测
                            pipeline.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
                            pipeline.addLast(new StringEncoder(Charset.forName("UTF-8")));
                            // 解码格式
                            pipeline.addLast(new StringDecoder(Charset.forName("UTF-8")));
                            //
                            pipeline.addLast("handler", new WebSocketChannelHandler());
                        }
                    });
            Channel ch = bootstrap.bind(port).sync().channel();//这里写你本机的IP地址
            System.out.println("web socket server started at port " + port + ".");
            System.out.println("open your browser and navigate to http://localhost:" + port + "/");
            ch.closeFuture().sync();
        } catch (Exception e) {

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }
}
