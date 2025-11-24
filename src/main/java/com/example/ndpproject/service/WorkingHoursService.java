package com.example.ndpproject.service;

import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.entity.Salon;
import com.example.ndpproject.entity.WorkingHours;
import com.example.ndpproject.repository.WorkingHoursRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkingHoursService {

    private final WorkingHoursRepo workingHoursRepo;

    @Autowired
    public WorkingHoursService(WorkingHoursRepo workingHoursRepo) {
        this.workingHoursRepo = workingHoursRepo;
    }

    public List<WorkingHours> getWorkingHoursBySalon(Salon salon) {
        return workingHoursRepo.findBySalon(salon);
    }

    public Optional<WorkingHours> getWorkingHoursBySalonAndDay(Salon salon, DayOfWeek dayOfWeek) {
        return workingHoursRepo.findBySalonAndDayOfWeek(salon, dayOfWeek);
    }

    public WorkingHours saveWorkingHours(WorkingHours workingHours) {
        if (workingHours.getSalon() == null && workingHours.getEmployee() == null) {
            throw new IllegalArgumentException("Working hours must be associated with either a salon or an employee");
        }
        if (workingHours.getSalon() != null && workingHours.getEmployee() != null) {
            throw new IllegalArgumentException("Working hours cannot be associated with both a salon and an employee");
        }

        if (!workingHours.getIsClosed()) {
            if (workingHours.getStartTime().isAfter(workingHours.getEndTime()) ||
                    workingHours.getStartTime().equals(workingHours.getEndTime())) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
        }
        return workingHoursRepo.save(workingHours);
    }

    public void deleteWorkingHours(Long id) {
        workingHoursRepo.deleteById(id);
    }

    public void deleteAllBySalon(Salon salon) {
        workingHoursRepo.deleteBySalon(salon);
    }

    public List<WorkingHours> getWorkingHoursByEmployee(Employee employee) {
        return workingHoursRepo.findByEmployee(employee);
    }

    public Optional<WorkingHours> getWorkingHoursByEmployeeAndDay(Employee employee, DayOfWeek dayOfWeek) {
        return workingHoursRepo.findByEmployeeAndDayOfWeek(employee, dayOfWeek);
    }

    public void deleteAllByEmployee(Employee employee) {
        workingHoursRepo.deleteByEmployee(employee);
    }

    public boolean isWithinEmployeeWorkingHours(Employee employee, LocalDateTime dateTime, int durationMinutes) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        LocalTime appointmentStart = dateTime.toLocalTime();
        LocalTime appointmentEnd = appointmentStart.plusMinutes(durationMinutes);

        Optional<WorkingHours> workingHours = workingHoursRepo.findByEmployeeAndDayOfWeek(employee, dayOfWeek);

        if (workingHours.isEmpty()) {
            return false;
        }

        WorkingHours hours = workingHours.get();
        if (hours.getIsClosed()) {
            return false;
        }

        return !appointmentStart.isBefore(hours.getStartTime()) &&
                !appointmentEnd.isAfter(hours.getEndTime());
    }

    public boolean isWithinWorkingHours(Salon salon, LocalDateTime dateTime, int durationMinutes) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        LocalTime appointmentStart = dateTime.toLocalTime();
        LocalTime appointmentEnd = appointmentStart.plusMinutes(durationMinutes);

        Optional<WorkingHours> workingHours = workingHoursRepo.findBySalonAndDayOfWeek(salon, dayOfWeek);

        if (workingHours.isEmpty()) {
            return false;
        }

        WorkingHours hours = workingHours.get();
        if (hours.getIsClosed()) {
            return false;
        }

        return !appointmentStart.isBefore(hours.getStartTime()) &&
                !appointmentEnd.isAfter(hours.getEndTime());
    }

    public Optional<WorkingHours> getForDay(Salon salon, DayOfWeek dayOfWeek) {
        return workingHoursRepo.findBySalonAndDayOfWeek(salon, dayOfWeek);
    }
}
