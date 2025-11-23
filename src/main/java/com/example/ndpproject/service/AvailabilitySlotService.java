package com.example.ndpproject.service;

import com.example.ndpproject.entity.AvailabilitySlot;
import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.repository.AvailabilitySlotRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AvailabilitySlotService {

    private final AvailabilitySlotRepo slotRepo;

    @Autowired
    public AvailabilitySlotService(AvailabilitySlotRepo slotRepo) {
        this.slotRepo = slotRepo;
    }

    public List<AvailabilitySlot> getAllSlots() {
        return slotRepo.findAll();
    }

    public List<AvailabilitySlot> getSlotsForEmployee(Employee employee) {
        return slotRepo.findByEmployee(employee);
    }

    public List<AvailabilitySlot> getSlotsForEmployeeOnDate(Employee employee, LocalDate date) {
        return slotRepo.findByEmployeeAndDate(employee, date);
    }

    public AvailabilitySlot saveSlot(AvailabilitySlot slot) {
        if (slot.getStartTime().isAfter(slot.getEndTime()) || slot.getStartTime().equals(slot.getEndTime())) {
            throw new IllegalArgumentException("Slot start time must be before end time");
        }
        return slotRepo.save(slot);
    }

    public void deleteSlot(Long id) {
        slotRepo.deleteById(id);
    }

    public Optional<AvailabilitySlot> getSlotById(Long id) {
        return slotRepo.findById(id);
    }

    public boolean isWithinAnySlot(Employee employee, LocalDateTime start, int durationMinutes) {
        LocalDate date = start.toLocalDate();
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        return slotRepo.existsByEmployeeAndDateAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                employee, date, startTime, endTime
        );
    }
}
