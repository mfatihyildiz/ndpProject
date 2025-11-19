package com.example.ndpproject.service;

import com.example.ndpproject.entity.Appointment;
import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.enums.Status;
import com.example.ndpproject.repository.AppointmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepo appointmentRepo;

    @Autowired
    public AppointmentService(AppointmentRepo appointmentRepo) {
        this.appointmentRepo = appointmentRepo;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    public Appointment saveAppointment(Appointment appointment) {
        return appointmentRepo.save(appointment);
    }

    public void updateStatus(Long id, Status status) {
        Appointment appointment = appointmentRepo.findById(id).orElseThrow();
        appointment.setStatus(status);
        appointmentRepo.save(appointment);
    }

    public boolean isEmployeeAvailable(Employee employee, LocalDateTime dateTime) {
        List<Appointment> existing = appointmentRepo.findByEmployee(employee);
        for (Appointment app : existing) {
            if (app.getDateTime().equals(dateTime) && app.getStatus() != Status.CANCELLED) {
                return false;
            }
        }
        return true;
    }

    public void deleteAppointment(Long id) {
        appointmentRepo.deleteById(id);
    }
}
