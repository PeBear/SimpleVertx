package com.xpeter;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.xpeter.dao.CustomerDAO;
import com.xpeter.dto.CustomerDTO;
import com.xpeter.helper.ConfigReader;
import com.xpeter.helper.JooqHelper;
import com.xpeter.model.tables.records.CustomerRecord;
import com.xpeter.service.CustomerService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
            String contentType = req.getHeader("content-type");
            if (method.equals("GET")) {
                processGetMethod(response);
            } else {
                if (contentType == null ||
                        !contentType.equalsIgnoreCase("application/json")) {
                    response.end(getTemplateResponse(response, 400, false, "Bad Request"));
                } else {
                    if (method.equals("POST")) {
                        processPostMethod(req, response);
                    }
                    if (method.equals("PUT")) {
                        processPutMethod(req, response);
                    }
                    if (method.equals("DELETE")) {
                        processDeleteMethod(req, response);
                    }
                }
            }
        }).listen(serverPort, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                log.info("HttpServer started on port: " + serverPort);
            } else {
                log.error("Opps on HttpServer start: " + http.cause());
            }
        });
        EventBus eb = vertx.eventBus();
        eb.consumer("main.verticle", result -> {
            System.out.println("MainVerticle EventBus receive mess: " + result.body());
            result.reply("Yo, Reply from MainVerticle");
            eb.publish("temp.verticle", "PUBLISH MESS FROM MainVerticle");
        });
        vertx.deployVerticle("com.xpeter.SubVerticle");
        vertx.deployVerticle("com.xpeter.TempVerticle");
    }

    private void processGetMethod(HttpServerResponse response) {
        String mess = "Get list success";
        response.end(getTemplateResponse(response, 200, true, customerService.getListCustomers(), mess));
    }

    private void processPostMethod(HttpServerRequest req, HttpServerResponse response) {
        req.handler(buffer -> {
            try {
                String mess = "";
                String error = checkInput(buffer, "POST");
                if (error.isEmpty()) {
                    JsonObject object = buffer.toJsonObject();
                    if (customerService.insertCustomer(object)) {
                        mess = "POST Done! Insert Success Customer " + object.getString("username");
                        String end = getTemplateResponse(response, 201, true, mess);
                        response.end(end);
                        log.info(mess);
                    } else {
                        mess = "POST Done! Insert Failed";
                        String end = getTemplateResponse(response, 400, false, mess);
                        response.end(end);
                        log.info(mess);
                    }
                } else {
                    String end = getTemplateResponse(response, 400, false, error);
                    response.end(end);
                }

            } catch (SQLException e) {
                response.end(getTemplateResponse(response, 400, false, e.getMessage()));
                log.error(e.getMessage());
            } catch (DataAccessException e) {
                response.end(getTemplateResponse(response, 409, false, "An existing item already exists"));
                log.error(e.getCause().getMessage());
            }
        });
    }

    private void processPutMethod(HttpServerRequest req, HttpServerResponse response) {
        req.handler(buffer -> {
            String error = checkInput(buffer, "PUT");
            if (error.isEmpty()) {
                try {
                    JsonObject object = buffer.toJsonObject();
                    String mess = "";
                    if (customerService.updateCustomer(object)) {
                        mess = "PUT Done! Update Success Customer " + object.getString("username");
                        response.end(getTemplateResponse(response, 200, true, mess));
                        log.info(mess);
                    } else {
                        mess = "PUT Done! Update Failed";
                        response.end(getTemplateResponse(response, 400, false, mess));
                        log.info(mess);
                    }
                } catch (SQLException e) {
                    response.end(getTemplateResponse(response, 400, false, e.getMessage()));
                    log.error(e.getMessage());
                }
            } else {
                String end = getTemplateResponse(response, 400, false, error);
                response.end(end);
            }
        });
    }

    private void processDeleteMethod(HttpServerRequest req, HttpServerResponse response) {
        req.handler(buffer -> {
            String error = "";
            error = checkInput(buffer, "DELETE");
            if (error.isEmpty()) {
                try {
                    JsonObject object = new JsonObject(buffer);
                    String mess = "";
                    if (customerService.deleteCustomer(object.getString("username"))) {
                        mess = "DELETE Done! Delete Success Customer " + object.getString("username");
                        response.end(getTemplateResponse(response, 200, true, mess));
                        log.info(mess);
                    } else {
                        mess = "DELETE Done! Delete Failed";
                        response.end(getTemplateResponse(response, 400, false, mess));
                        log.info(mess);
                    }
                } catch (SQLException e) {
                    response.end(getTemplateResponse(response, 400, false, e.getMessage()));
                    log.error(e.getMessage());
                }
            } else {
                String end = getTemplateResponse(response, 400, false, error);
                response.end(end);
            }
        });
    }

    private String getTemplateResponse(HttpServerResponse response, int code, boolean status, List<CustomerDTO> data, String message) {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        object.put("status", status);
        object.put("code", code);
        data.forEach(cus -> {
            array.add(new JsonObject(new Gson().toJson(cus)));
        });
        object.put("data", array);
        object.put("message", message);
        return object.toString();
    }

    private String getTemplateResponse(HttpServerResponse response, int code, boolean status, String message) {
        JsonObject object = new JsonObject();
        object.put("status", status);
        object.put("code", code);
        object.put("message", message);
        return object.toString();
    }

    private String checkInput(Buffer buffer, String method) {
        JsonObject object = null;
        try {
            object = buffer.toJsonObject();
        } catch (Exception e) {
            log.error(e.getMessage());
            return "bad request";
        }

        if (!object.containsKey("username")) {
            return "Username can't null";
        }
        if (!method.equalsIgnoreCase("DELETE")) {
            if (!object.containsKey("password")) {
                return "Password can't null";
            }
            if (!object.containsKey("fullname")) {
                return "Fullname can't null";
            }
            if (!object.containsKey("gender")) {
                return "Gender can't null";
            }
            if (!object.containsKey("email")) {
                return "Email can't null";
            }
        }
        return "";
    }
}
