package com.example.nettywebsocket.netty.utils.response;

import lombok.Data;

@Data
public class ObjectResponseResult {
    private String type;
    private Object offer;
    private Object answer;
    private Object candidate;
    private String name;
    private Boolean success = true;
    private String message;

    public static ObjectResponseResult result() {
        return new ObjectResponseResult();
    }

    public ObjectResponseResult type(String type) {
        this.type = type;
        return this;
    }

    public ObjectResponseResult offer(Object offer) {
        this.offer = offer;
        return this;
    }

    public ObjectResponseResult answer(Object answer) {
        this.answer = answer;
        return this;
    }

    public ObjectResponseResult candidate(Object candidate) {
        this.candidate = candidate;
        return this;
    }

    public ObjectResponseResult name (String name) {
        this.name = name;
        return this;
    }

    public ObjectResponseResult success(boolean success) {
        this.success = success;
        return this;
    }

    public ObjectResponseResult message(String message) {
        this.message = message;
        return this;
    }

}
