package com.xpeter;

import com.xpeter.dao.CustomerDAO;
import com.xpeter.helper.ConfigReader;
import com.xpeter.helper.JooqHelper;
import com.xpeter.model.tables.records.CustomerRecord;
import com.xpeter.service.CustomerService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

public class MainVerticle extends AbstractVerticle {
    private int serverPort = new ConfigReader().getVertxServerPort();
    CustomerService customerService = new CustomerService();
    private static final Logger log = LogManager.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        HttpServer server = vertx.createHttpServer().requestHandler(req -> {
            String method = req.method().toString();
            HttpServerResponse response = req.response();
            response.putHeader("content-type", "application/json");

            if (method.equals("GET")) {
                response.end(customerService.getListCustomers());
            }
            if (method.equals("POST")) {
                req.handler(buffer -> {
                    System.out.println("POST received: " + buffer.toJson());
                    try {
                        JsonObject object = buffer.toJsonObject();
                        if (customerService.insertCustomer(object)) {
                            response.end("POST Done! Insert Success Customer " + object.getString("username"));
                            log.info("POST Done! Insert Success Customer " + object.getString("username"));
                        } else {
                            response.end("POST Done! Insert Failed");
                            log.info("POST Done! Insert Failed");
                        }
                    } catch (SQLException e) {
                        response.end(e.getMessage());
                        log.error(e.getMessage());
                    }
                });
            }
            if (method.equals("PUT")) {
                req.handler(buffer -> {
                    System.out.println("PUT received: " + buffer.toJson());
                    try {
                        JsonObject object = buffer.toJsonObject();
                        if (customerService.updateCustomer(object)) {
                            response.end("PUT Done! Update Success Customer " + object.getString("username"));
                            log.info("PUT Done! Update Success Customer " + object.getString("username"));
                        } else {
                            response.end("PUT Done! Update Failed");
                            log.info("PUT Done! Update Failed");
                        }
                    } catch (SQLException e) {
                        response.end(e.getMessage());
                        log.error(e.getMessage());
                    }
                });
            }
            if (method.equals("DELETE")) {
                req.handler(buffer -> {
                    System.out.println("DELETE received: " + buffer.toJson());
                    JsonObject object = buffer.toJsonObject();
                    try {
                        if (customerService.deleteCustomer(object.getString("username"))) {
                            response.end("DELETE Done! Delete Success Customer " + object.getString("username"));
                            log.info("DELETE Done! Delete Success Customer " + object.getString("username"));
                        } else {
                            response.end("DELETE Done! Delete Failed");
                            log.info("DELETE Done! Delete Failed");
                        }
                    } catch (SQLException e) {
                        response.end(e.getMessage());
                        log.error(e.getMessage());
                    }
                });
            }
        }).listen(serverPort, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                log.info("HttpServer started on port: " + serverPort);
            } else {
                log.error("Opps on HttpServer start: " + http.cause());
            }
        });
    }


}
