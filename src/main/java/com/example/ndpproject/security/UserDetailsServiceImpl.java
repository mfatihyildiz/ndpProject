package com.example.ndpproject.security;

import com.example.ndpproject.entity.User;
import com.example.ndpproject.repository.AdminRepo;
import com.example.ndpproject.repository.CustomerRepo;
import com.example.ndpproject.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CustomerRepo customerRepo;
    private final EmployeeRepo employeeRepo;
    private final AdminRepo adminRepo;

    @Autowired
    public UserDetailsServiceImpl(AdminRepo adminRepo, CustomerRepo customerRepo, EmployeeRepo employeeRepo) {
        this.customerRepo = customerRepo;
        this.employeeRepo = employeeRepo;
        this.adminRepo = adminRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;

        var admin = adminRepo.findByUsername(username).orElse(null);
        if (admin != null) user = admin;

        var customer = customerRepo.findByUsername(username);
        if (customer != null) user = customer;

        var employee = employeeRepo.findByUsername(username).orElse(null);

        if (employee != null) user = employee;

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(authority)
        );
    }
}

