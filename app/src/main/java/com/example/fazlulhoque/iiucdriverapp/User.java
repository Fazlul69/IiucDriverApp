package com.example.fazlulhoque.iiucdriverapp;

/**
 * Created by Fazlul Hoque on 12/6/2017.
 */
public class User {
    private String email,password,name,phone,imageUrl;

    User(){
    }

    public User(String email, String password, String name, String phone,String imageUrl) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.imageUrl=imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
