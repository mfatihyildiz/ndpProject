package com.example.ndpproject.repository;

import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.entity.Salon;
import com.example.ndpproject.entity.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkingHoursRepo extends JpaRepository<WorkingHours, Long> {
    List<WorkingHours> findBySalon(Salon salon);
    Optional<WorkingHours> findBySalonAndDayOfWeek(Salon salon, DayOfWeek dayOfWeek);
    void deleteBySalon(Salon salon);

    List<WorkingHours> findByEmployee(Employee employee);
    Optional<WorkingHours> findByEmployeeAndDayOfWeek(Employee employee, DayOfWeek dayOfWeek);
    void deleteByEmployee(Employee employee);
}
