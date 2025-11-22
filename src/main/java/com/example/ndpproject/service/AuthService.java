package com.example.ndpproject.service;

import com.example.ndpproject.entity.Admin;
import com.example.ndpproject.entity.Customer;
import com.example.ndpproject.enums.Role;
import com.example.ndpproject.repository.AdminRepo;
import com.example.ndpproject.repository.CustomerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final AdminRepo adminRepo;
    private final CustomerRepo customerRepo;

    @Autowired
    public AuthService(AdminRepo adminRepo, CustomerRepo customerRepo) {
        this.adminRepo = adminRepo;
        this.customerRepo = customerRepo;
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

        Customer customer = customerRepo.findByUsername(username);
        if (customer != null && customer.getRole() == Role.ADMIN) {
            if (admin.isEmpty()) {
                Admin newAdmin = new Admin(customer.getUsername(), customer.getPassword(), customer.getFullName());
                adminRepo.save(newAdmin);
                return Optional.of(newAdmin);
            }
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
}
