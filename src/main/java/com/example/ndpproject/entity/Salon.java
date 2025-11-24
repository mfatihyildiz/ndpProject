package com.example.ndpproject.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "salons")
public class Salon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;

    @ManyToOne
    @JoinColumn(name = "manager_id", nullable = false)
    private Admin manager;

    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employee> employees;

    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Services> services;

    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkingHours> workingHours;

    public Salon() {}

    public Salon(String name, String address, Admin manager) {
        this.name = name;
        this.address = address;
        this.manager = manager;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<Employee> getEmployees() { return employees; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }

    public List<Services> getServices() { return services; }
    public void setServices(List<Services> services) { this.services = services; }

    public List<WorkingHours> getWorkingHours() { return workingHours; }
    public void setWorkingHours(List<WorkingHours> workingHours) { this.workingHours = workingHours; }

    public Admin getManager() { return manager; }
    public void setManager(Admin manager) { this.manager = manager; }

    @Override
    public String toString() {
        return "Salon{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
