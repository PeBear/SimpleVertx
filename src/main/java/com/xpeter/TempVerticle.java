package com.xpeter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class TempVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        EventBus eb = vertx.eventBus();
        eb.consumer("temp.verticle", mess -> {
            System.out.println("TempVerticle received mess: " + mess.body());
        });
    }
}
