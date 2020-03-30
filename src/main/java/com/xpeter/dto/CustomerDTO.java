package com.xpeter.dto;

public class CustomerDTO {
    private String username;
    private String password;
    private String fullname;
    private boolean gender;
    private String email;

    public CustomerDTO() {
    }

    public CustomerDTO(String username, String password, String fullname, boolean gender, String email) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.gender = gender;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "CustomerDTO{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", fullname='" + fullname + '\'' +
                ", gender=" + gender +
                ", email='" + email + '\'' +
                '}';
    }
}
