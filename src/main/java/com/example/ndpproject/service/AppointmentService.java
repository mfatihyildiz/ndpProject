package com.example.ndpproject.service;

import com.example.ndpproject.entity.Appointment;
import com.example.ndpproject.entity.Customer;
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
    private final AvailabilitySlotService availabilitySlotService;

    @Autowired
    public AppointmentService(AppointmentRepo appointmentRepo,
                              AvailabilitySlotService availabilitySlotService) {
        this.appointmentRepo = appointmentRepo;
        this.availabilitySlotService = availabilitySlotService;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    public List<Appointment> getAppointmentsByCustomer(Customer customer) {
        return appointmentRepo.findByCustomer(customer);
    }

    public List<Appointment> getAppointmentsByEmployee(Employee employee) {
        return appointmentRepo.findByEmployee(employee);
    }

    public Appointment saveAppointment(Appointment appointment) {
        return appointmentRepo.save(appointment);
    }

    public void updateStatus(Long id, Status status) {
        Appointment appointment = appointmentRepo.findById(id).orElseThrow();
        appointment.setStatus(status);
        appointmentRepo.save(appointment);
    }

    public boolean isEmployeeAvailable(Employee employee, LocalDateTime dateTime, int durationMinutes) {
        List<Appointment> existing = appointmentRepo.findByEmployee(employee);
        LocalDateTime requestedEnd = dateTime.plusMinutes(durationMinutes);
        for (Appointment app : existing) {
            if (app.getStatus() == Status.CANCELLED) continue;
            LocalDateTime existingStart = app.getDateTime();
            int existingDuration = app.getService() != null ? app.getService().getDuration() : durationMinutes;
            LocalDateTime existingEnd = existingStart.plusMinutes(existingDuration);

            boolean overlaps = dateTime.isBefore(existingEnd) && requestedEnd.isAfter(existingStart);
            if (overlaps) {
                return false;
            }
        }
        return true;
    }

    public boolean isWithinEmployeeAvailability(Employee employee, LocalDateTime start, int durationMinutes) {
        return availabilitySlotService.isWithinAnySlot(employee, start, durationMinutes);
    }

    public void deleteAppointment(Long id) {
        appointmentRepo.deleteById(id);
    }
}
