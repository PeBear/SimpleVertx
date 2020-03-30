package com.xpeter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class SubVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        EventBus eb = vertx.eventBus();
        eb.consumer("temp.verticle", mess ->{
            System.out.println("SubVerticle received mess: " + mess.body());
        });
        eb.request("main.verticle", "Hello from SubVerticle", reply -> {
            if (reply.succeeded()) {
                System.out.println("SubVerticle EventBus reply received: " + reply.result().body());
            } else {
                System.out.println("SubVerticle EventBus opps reply: " + reply.cause());
            }
        });
    }
}
