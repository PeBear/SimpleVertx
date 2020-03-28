package com.xpeter.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JooqHelper {
    private static final Logger log = LogManager.getLogger(JooqHelper.class);

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.error("Cant load JDBC MySQL Driver!");
        }
    }

    public static Connection getConnection() {
        ConfigReader config = new ConfigReader();
        Connection con = null;
        try {
            con = DriverManager.getConnection(config.getDbUrl(), config.getDbUsername(), config.getDbPassword());
        } catch (SQLException e) {
            log.error("Connecting to DB error: " + e.getMessage());
        }
        return con;
    }
}
