package com.xpeter.dao;

import com.xpeter.helper.JooqHelper;
import com.xpeter.model.tables.records.CustomerRecord;
import io.vertx.core.json.JsonArray;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

import static com.xpeter.model.Tables.CUSTOMER;

public class CustomerDAO {

    private DSLContext context;

    public CustomerDAO() {
        Connection connection = JooqHelper.getConnection();
        Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.MYSQL);
        context = DSL.using(configuration);
    }

    public String getListCustomers() {
        Result<Record> result = context.select().from(CUSTOMER).fetch();
        JsonArray array = new JsonArray();
//        result.forEach(data -> {
//            array.add(data.formatJSON());
//        });
        return result.formatJSON(JSONFormat.DEFAULT_FOR_RECORDS);
    }

    public String getCustomerById(String value) {
        Record record = context.select().from(CUSTOMER).where(CUSTOMER.USERNAME.eq(value)).fetchOne();
        if (record == null) {
            return null;
        }
        return record.formatJSON();
    }

    public boolean insertCustomer(CustomerRecord record) throws SQLException {
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

    public boolean deleteCustomer(String username) throws SQLException{
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
