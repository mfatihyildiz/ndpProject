package com.example.ndpproject.repository;

import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.entity.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long> {
    List<Employee> findBySalon(Salon salon);
    Optional<Employee> findByUsername(String username);
    boolean existsByIdAndSkills_Id(Long employeeId, Long serviceId);
}
