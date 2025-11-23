package com.example.ndpproject.service;

import com.example.ndpproject.entity.Admin;
import com.example.ndpproject.entity.Customer;
import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.enums.Role;
import com.example.ndpproject.repository.AdminRepo;
import com.example.ndpproject.repository.CustomerRepo;
import com.example.ndpproject.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final AdminRepo adminRepo;
    private final CustomerRepo customerRepo;
    private final EmployeeRepo employeeRepo;

    @Autowired
    public AuthService(AdminRepo adminRepo, CustomerRepo customerRepo, EmployeeRepo employeeRepo) {
        this.adminRepo = adminRepo;
        this.customerRepo = customerRepo;
        this.employeeRepo = employeeRepo;
    }

    public Optional<Admin> getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String username = authentication.getName();

        Optional<Admin> admin = adminRepo.findByUsername(username);
        if (admin.isPresent() && admin.get().getRole() == Role.ADMIN) {
            return admin;
        }
        return Optional.empty();
    }

    public Optional<Customer> getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String username = authentication.getName();
        Customer customer = customerRepo.findByUsername(username);

        if (customer != null && customer.getRole() == Role.CUSTOMER) {
            return Optional.of(customer);
        }

        return Optional.empty();
    }

    public Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.of(authentication.getName());
    }

    public boolean isCurrentUserAdmin() {
        return getCurrentAdmin().isPresent();
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    public Optional<Employee> getCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String username = authentication.getName();
        Optional<Employee> employee = employeeRepo.findByUsername(username);

        if (employee.isPresent() && employee.get().getRole() == Role.EMPLOYEE) {
            return employee;
        }

        return Optional.empty();
    }
}
