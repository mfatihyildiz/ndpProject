package com.example.ndpproject.service;

import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.enums.Role;
import com.example.ndpproject.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepo employeeRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeService(EmployeeRepo employeeRepo, PasswordEncoder passwordEncoder) {
        this.employeeRepo = employeeRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepo.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepo.findById(id);
    }

    public List<Employee> getEmployeesBySalon(com.example.ndpproject.entity.Salon salon) {
        return employeeRepo.findBySalon(salon);
    }

    @Transactional
    public Employee saveEmployee(Employee employee) {
        employee.setRole(Role.EMPLOYEE);
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return employeeRepo.save(employee);
    }

    public void deleteEmployee(Long id) {
        employeeRepo.deleteById(id);
    }

    public boolean canPerformService(Long employeeId, Long serviceId) {
        return employeeRepo.existsByIdAndSkills_Id(employeeId, serviceId);
    }
}
