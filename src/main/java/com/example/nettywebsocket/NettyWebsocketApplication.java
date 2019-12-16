package com.example.nettywebsocket;

import com.example.nettywebsocket.netty.server.WebSocketServer;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
@Data
@Component
public class NettyWebsocketApplication implements CommandLineRunner {

    //    @Value("${netty.port}")
    private int port = 8008;

    @Autowired
    private WebSocketServer webSocketServer;

    public static void main(String[] args) {
        SpringApplication.run(NettyWebsocketApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        webSocketServer.start(port);
    }
}
