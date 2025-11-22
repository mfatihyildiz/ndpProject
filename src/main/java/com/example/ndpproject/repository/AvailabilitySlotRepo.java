package com.example.ndpproject.repository;

import com.example.ndpproject.entity.AvailabilitySlot;
import com.example.ndpproject.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AvailabilitySlotRepo extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findByEmployee(Employee employee);
    List<AvailabilitySlot> findByEmployeeAndDate(Employee employee, LocalDate date);
    boolean existsByEmployeeAndDateAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            Employee employee,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    );
}
