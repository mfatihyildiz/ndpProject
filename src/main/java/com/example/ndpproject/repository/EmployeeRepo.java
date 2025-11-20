package com.example.ndpproject.repository;

import com.example.ndpproject.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUsername(String username);
    boolean existsByIdAndSkills_Id(Long employeeId, Long serviceId);
}
