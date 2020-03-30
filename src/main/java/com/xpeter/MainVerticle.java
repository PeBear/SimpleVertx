package com.xpeter;

import com.google.gson.Gson;
import com.xpeter.dao.CustomerDAO;
import com.xpeter.dto.CustomerDTO;
import com.xpeter.helper.ConfigReader;
import com.xpeter.helper.JooqHelper;
import com.xpeter.model.tables.records.CustomerRecord;
import com.xpeter.service.CustomerService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
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
            if (method.equals("GET")) {
                processGetMethod(response);
            }
            if (method.equals("POST")) {
                processPostMethod(req, response);
            }
            if (method.equals("PUT")) {
                processPutMethod(req, response);
            }
            if (method.equals("DELETE")) {
                processDeleteMethod(req, response);
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

    private void processGetMethod(HttpServerResponse response) {
        String mess = "Get list success";
        response.end(getTemplateResponse(response, true, customerService.getListCustomers(), mess));
    }

    private void processPostMethod(HttpServerRequest req, HttpServerResponse response) {
        req.handler(buffer -> {
            try {
                JsonObject object = buffer.toJsonObject();
                String mess = "";
                if (customerService.insertCustomer(object)) {
                    mess = "POST Done! Insert Success Customer " + object.getString("username");
                    String end = getTemplateResponse(response, true, mess);
                    response.end(end);
                    log.info(mess);
                } else {
                    mess = "POST Done! Insert Failed";
                    String end = getTemplateResponse(response, false, mess);
                    response.end(end);
                    log.info(mess);
                }
            } catch (SQLException e) {
                response.end(getTemplateResponse(response, false, e.getMessage()));
                log.error(e.getMessage());
            }
        });
    }

    private void processPutMethod(HttpServerRequest req, HttpServerResponse response) {
        req.handler(buffer -> {
            try {
                JsonObject object = buffer.toJsonObject();
                String mess = "";
                if (customerService.updateCustomer(object)) {
                    mess = "PUT Done! Update Success Customer " + object.getString("username");
                    response.end(getTemplateResponse(response, true, mess));
                    log.info(mess);
                } else {
                    mess = "PUT Done! Update Failed";
                    response.end(getTemplateResponse(response, false, mess));
                    log.info(mess);
                }
            } catch (SQLException e) {
                response.end(getTemplateResponse(response, false, e.getMessage()));
                log.error(e.getMessage());
            }
        });
    }

    private void processDeleteMethod(HttpServerRequest req, HttpServerResponse response) {
        req.handler(buffer -> {
            JsonObject object = buffer.toJsonObject();
            String mess = "";
            try {
                if (customerService.deleteCustomer(object.getString("username"))) {
                    mess = "DELETE Done! Delete Success Customer " + object.getString("username");
                    response.end(getTemplateResponse(response, true, mess));
                    log.info(mess);
                } else {
                    mess = "DELETE Done! Delete Failed";
                    response.end(getTemplateResponse(response, false, mess));
                    log.info(mess);
                }
            } catch (SQLException e) {
                response.end(getTemplateResponse(response, false, e.getMessage()));
                log.error(e.getMessage());
            }
        });
    }

    private String getTemplateResponse(HttpServerResponse response, boolean status, List<CustomerDTO> data, String message) {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        object.put("status", status);
        object.put("code", response.getStatusCode());
        data.forEach(cus -> {
            array.add(new JsonObject(new Gson().toJson(cus)));
        });
        object.put("data", array);
        object.put("message", message);
        return object.toString();
    }

    private String getTemplateResponse(HttpServerResponse response, boolean status, String message) {
        JsonObject object = new JsonObject();
        object.put("status", status);
        object.put("code", response.getStatusCode());
        object.put("message", message);
        return object.toString();
    }
}
