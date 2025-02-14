package com.haswanth.vita;

public class User {

    public String fullname, email;

    public User(){

    }

    public  User(String fullname, String email) {
        this.fullname = fullname;
        this.email = email;
    }


    public String getfullname() {
        return this.fullname;
    }
}