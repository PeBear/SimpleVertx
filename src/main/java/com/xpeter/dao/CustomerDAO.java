package com.xpeter.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.xpeter.dto.CustomerDTO;
import com.xpeter.helper.JooqHelper;
import com.xpeter.model.tables.records.CustomerRecord;
import io.vertx.core.json.JsonArray;

import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.tools.json.JSONObject;
import org.jooq.tools.json.JSONParser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.xpeter.model.Tables.CUSTOMER;

public class CustomerDAO {

    private DSLContext context;

    public CustomerDAO() {
        Connection connection = JooqHelper.getConnection();
        Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.MYSQL);
        context = DSL.using(configuration);
    }

    public List<CustomerDTO> getListCustomers() {
        List<CustomerDTO> list = context.select().from(CUSTOMER).fetchInto(CustomerDTO.class);
        return list;
    }

    public String getCustomerById(String value) {
        CustomerDTO record = context.select().from(CUSTOMER).where(CUSTOMER.USERNAME.eq(value)).fetchOneInto(CustomerDTO.class);
        if (record == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        String result = "";
        try {
            System.out.println(mapper.writeValueAsString(record));
            result = mapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean insertCustomer(CustomerRecord record) throws SQLException {
        if (getCustomerById(record.getUsername()) != null){
            return false;
        }
        int affect =
                context.insertInto(CUSTOMER, CUSTOMER.USERNAME, CUSTOMER.PASSWORD, CUSTOMER.FULLNAME, CUSTOMER.GENDER, CUSTOMER.EMAIL)
                        .values(record.getUsername(), record.getPassword(), record.getFullname(), record.getGender(), record.getEmail())
                        .execute();
        if (affect > 0) {
            return true;
        }
        return false;
    }

    public boolean updateCustomer(CustomerRecord object) throws SQLException {
        if (getCustomerById(object.getUsername()) == null) {
            return false;
        }

        int affect =
                context.update(CUSTOMER)
                        .set(CUSTOMER.PASSWORD, object.getPassword())
                        .set(CUSTOMER.FULLNAME, object.getFullname())
                        .set(CUSTOMER.GENDER, object.getGender())
                        .set(CUSTOMER.EMAIL, object.getEmail())
                        .where(CUSTOMER.USERNAME.eq(object.getUsername()))
                        .execute();
        if (affect > 0) {
            return true;
        }
        return false;
    }

    public boolean deleteCustomer(String username) throws SQLException {
        if (getCustomerById(username) == null) {
            return false;
        }
        int affect =
                context.delete(CUSTOMER)
                        .where(CUSTOMER.USERNAME.eq(username))
                        .execute();
        if (affect > 0) {
            return true;
        }
        return false;
    }
}
