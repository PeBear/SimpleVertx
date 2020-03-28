package com.xpeter.helper;

import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.tools.json.JSONParser;
import org.jooq.tools.json.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class ConfigReader {
    private static final Logger log = LogManager.getLogger(ConfigReader.class);

    private JsonObject config;
    private String dbUsername;
    private String dbPassword;
    private String dbUrl;
    private int vertxServerPort;

    public ConfigReader() {
        try {
            FileReader reader = new FileReader("conf.json");
            Object object = new JSONParser().parse(reader);
            config = new JsonObject(object.toString());
        } catch (IOException | ParseException e) {
            log.error("Read config.json file ERROR: " + e.getMessage());
        }
    }

    public JsonObject getConfig() {
        return config;
    }

    public String getDbUsername() {
        return config.getString("username");
    }

    public String getDbPassword() {
        return config.getString("password");
    }

    public String getDbUrl() {
        return config.getString("url");
    }

    public int getVertxServerPort() {
        return config.getInteger("serverPort");
    }
}
