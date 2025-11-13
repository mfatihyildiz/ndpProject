package com.example.ndpproject.controller;

import com.example.ndpproject.entity.Customer;
import com.example.ndpproject.enums.Role;
import com.example.ndpproject.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/register")
public class RegisterController {

    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegisterController(CustomerService customerService, PasswordEncoder passwordEncoder) {
        this.customerService = customerService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "register";
    }

    @PostMapping
    public String registerUser(@ModelAttribute("customer") Customer customer, Model model) {
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setRole(Role.CUSTOMER);
        customerService.saveCustomer(customer);

        model.addAttribute("success", true);
        return "redirect:/login?registered";
    }
}
