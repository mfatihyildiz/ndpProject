package com.example.ndpproject.entity;


import com.example.ndpproject.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public class Admin extends User {

    private String fullName;

    public Admin() {}

    public Admin(String username, String password, String fullName) {
        super(username, password, Role.ADMIN);
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}


