package com.xpeter.service;

import com.xpeter.dao.CustomerDAO;
import com.xpeter.dto.CustomerDTO;
import com.xpeter.model.tables.records.CustomerRecord;
import io.vertx.core.json.JsonObject;

import java.sql.SQLException;
import java.util.List;

public class CustomerService {
    CustomerDAO customerDAO = new CustomerDAO();

    public List<CustomerDTO> getListCustomers() {
        return customerDAO.getListCustomers();
    }

    public String getCustomerById(String username){
        return customerDAO.getCustomerById(username);
    }

    public boolean insertCustomer(JsonObject object) throws SQLException {
        CustomerRecord record = getModel(object);
        return customerDAO.insertCustomer(record);
    }

    public boolean updateCustomer(JsonObject object) throws SQLException {
        CustomerRecord record = getModel(object);
        return customerDAO.updateCustomer(record);
    }

    public boolean deleteCustomer(String username) throws SQLException {
        return customerDAO.deleteCustomer(username);
    }

    private CustomerRecord getModel(JsonObject object) {
        String username = object.getString("username");
        String password = object.getString("password");
        String fullname = object.getString("fullname");
        boolean gender = object.getBoolean("gender");
        String email = object.getString("email");
        CustomerRecord record = new CustomerRecord(username, password, fullname, gender, email);
        return record;
    }
}
