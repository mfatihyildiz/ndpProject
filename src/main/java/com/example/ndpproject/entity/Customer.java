package com.example.ndpproject.entity;

import com.example.ndpproject.enums.Role;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "customers")
public class Customer extends User {

    private String fullName;
    private String phone;
    private String email;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    public Customer() {}

    public Customer(String username, String password, Role role, String fullName,
                    String phone, String email) {
        super(username, password, role);
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    // Getters & Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }

    @Override
    public String toString() {
        return "Customer{" +
                "fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
