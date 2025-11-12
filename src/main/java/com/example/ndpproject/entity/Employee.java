package com.example.ndpproject.entity;

import com.example.ndpproject.enums.Role;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "employees")
public class Employee extends User {

    private String fullName;
    private String specialization;
    private String availability; // e.g. "Mon-Fri 09:00-17:00"

    @ManyToOne
    @JoinColumn(name = "salon_id")
    private Salon salon;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    public Employee() {}

    public Employee(String username, String password, Role role, String fullName,
                    String specialization, String availability, Salon salon) {
        super(username, password, role);
        this.fullName = fullName;
        this.specialization = specialization;
        this.availability = availability;
        this.salon = salon;
    }

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public Salon getSalon() { return salon; }
    public void setSalon(Salon salon) { this.salon = salon; }

    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }

    @Override
    public String toString() {
        return "Employee{" +
                "fullName='" + fullName + '\'' +
                ", specialization='" + specialization + '\'' +
                ", salon=" + (salon != null ? salon.getName() : "none") +
                '}';
    }
}
