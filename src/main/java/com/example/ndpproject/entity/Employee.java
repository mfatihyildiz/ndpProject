package com.example.ndpproject.entity;

import com.example.ndpproject.enums.Role;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "employees")
public class Employee extends User {

    private String fullName;
    private String specialization;

    @ManyToOne
    @JoinColumn(name = "salon_id")
    private Salon salon;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvailabilitySlot> availabilitySlots;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkingHours> workingHours;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "employee_services",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<Services> skills = new HashSet<>();

    public Employee() {}

    public Employee(String username, String password, Role role, String fullName,
                    String specialization, Salon salon) {
        super(username, password, role);
        this.fullName = fullName;
        this.specialization = specialization;
        this.salon = salon;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Salon getSalon() { return salon; }
    public void setSalon(Salon salon) { this.salon = salon; }

    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }

    public List<AvailabilitySlot> getAvailabilitySlots() { return availabilitySlots; }
    public void setAvailabilitySlots(List<AvailabilitySlot> availabilitySlots) { this.availabilitySlots = availabilitySlots; }

    public List<WorkingHours> getWorkingHours() { return workingHours; }
    public void setWorkingHours(List<WorkingHours> workingHours) { this.workingHours = workingHours; }

    public Set<Services> getSkills() { return skills; }
    public void setSkills(Set<Services> skills) { this.skills = skills; }

    @Override
    public String toString() {
        return "Employee{" +
                "fullName='" + fullName + '\'' +
                ", specialization='" + specialization + '\'' +
                ", salon=" + (salon != null ? salon.getName() : "none") +
                '}';
    }
}
