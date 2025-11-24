package com.example.ndpproject.service;

import com.example.ndpproject.entity.Appointment;
import com.example.ndpproject.entity.Customer;
import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.entity.WorkingHours;
import com.example.ndpproject.enums.Status;
import com.example.ndpproject.repository.AppointmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepo appointmentRepo;
    private final AvailabilitySlotService availabilitySlotService;
    private final WorkingHoursService workingHoursService;

    @Autowired
    public AppointmentService(AppointmentRepo appointmentRepo, AvailabilitySlotService availabilitySlotService, WorkingHoursService workingHoursService) {
        this.appointmentRepo = appointmentRepo;
        this.availabilitySlotService = availabilitySlotService;
        this.workingHoursService = workingHoursService;
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

    public boolean isWithinSalonWorkingHours(Employee employee, LocalDateTime dateTime, int durationMinutes) {
        if (employee.getSalon() == null) {
            return false;
        }
        return workingHoursService.isWithinWorkingHours(employee.getSalon(), dateTime, durationMinutes);
    }

    public void deleteAppointment(Long id) {
        appointmentRepo.deleteById(id);
    }

    public List<LocalDateTime> getAvailableTimeSlots(Employee employee, LocalDate date, int serviceDurationMinutes, int slotIntervalMinutes) {
        List<LocalDateTime> availableSlots = new ArrayList<>();

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<WorkingHours> workingHours = workingHoursService.getWorkingHoursByEmployeeAndDay(employee, dayOfWeek);

        if (workingHours.isEmpty() || workingHours.get().getIsClosed()) {
            return availableSlots;
        }

        WorkingHours hours = workingHours.get();
        LocalTime startTime = hours.getStartTime();
        LocalTime endTime = hours.getEndTime();

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(23, 59, 59);
        List<Appointment> existingAppointments = appointmentRepo.findByEmployeeAndDateTimeBetween(employee, dayStart, dayEnd).stream()
                .filter(app -> app.getStatus() != Status.CANCELLED)
                .collect(Collectors.toList());

        LocalTime currentTime = startTime;
        while (currentTime.plusMinutes(serviceDurationMinutes).isBefore(endTime) ||
                currentTime.plusMinutes(serviceDurationMinutes).equals(endTime)) {

            LocalDateTime slotDateTime = LocalDateTime.of(date, currentTime);
            LocalDateTime slotEnd = slotDateTime.plusMinutes(serviceDurationMinutes);

            boolean isAvailable = true;
            for (Appointment app : existingAppointments) {
                LocalDateTime appStart = app.getDateTime();
                int appDuration = app.getService() != null ? app.getService().getDuration() : serviceDurationMinutes;
                LocalDateTime appEnd = appStart.plusMinutes(appDuration);

                if (slotDateTime.isBefore(appEnd) && slotEnd.isAfter(appStart)) {
                    isAvailable = false;
                    break;
                }
            }

            if (isAvailable &&
                    !slotDateTime.toLocalTime().isBefore(startTime) &&
                    !slotEnd.toLocalTime().isAfter(endTime)) {
                availableSlots.add(slotDateTime);
            }

            currentTime = currentTime.plusMinutes(slotIntervalMinutes);

            if (currentTime.equals(startTime)) {
                break;
            }
        }

        return availableSlots;
    }

    public List<LocalDateTime> getAvailableTimeSlots(Employee employee, LocalDate date, int serviceDurationMinutes) {
        return getAvailableTimeSlots(employee, date, serviceDurationMinutes, 30);
    }
}
