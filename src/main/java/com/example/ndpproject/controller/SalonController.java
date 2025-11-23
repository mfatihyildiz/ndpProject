package com.example.ndpproject.controller;

import com.example.ndpproject.entity.Admin;
import com.example.ndpproject.entity.Customer;
import com.example.ndpproject.entity.Salon;
import com.example.ndpproject.service.AuthService;
import com.example.ndpproject.service.SalonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/salons")
public class SalonController {

    private final SalonService salonService;
    private final AuthService authService;

    @Autowired
    public SalonController(SalonService salonService, AuthService authService) {
        this.salonService = salonService;
        this.authService = authService;
    }

    @GetMapping
    public String listSalons(Model model) {
        model.addAttribute("salons", salonService.getAllSalons());

        Optional<Admin> currentAdmin = authService.getCurrentAdmin();
        if (currentAdmin.isPresent()) {
            model.addAttribute("currentManager", currentAdmin.get().getFullName());
            model.addAttribute("isAdmin", true);
        } else {
            model.addAttribute("isAdmin", false);
        }

        model.addAttribute("isAuthenticated", authService.isAuthenticated());

        return "salons";
    }

    @GetMapping("/new")
    public String showSalonForm(Model model) {
        if (!authService.isAuthenticated()) {
            return "redirect:/login";
        }
        model.addAttribute("salon", new Salon());
        return "salon-form";
    }

    @PostMapping("/save")
    public String saveSalon(@ModelAttribute Salon salon, RedirectAttributes redirectAttributes) {
        if (!authService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to create a salon.");
            return "redirect:/login";
        }

        try {
            Optional<Admin> currentAdmin = authService.getCurrentAdmin();

            if (currentAdmin.isPresent()) {
                if (salon.getId() != null) {
                    salonService.saveSalon(salon, currentAdmin.get());
                    redirectAttributes.addFlashAttribute("success", "Salon updated successfully.");
                } else {
                    salonService.saveSalon(salon, currentAdmin.get());
                    redirectAttributes.addFlashAttribute("success", "Salon created successfully.");
                }
            } else {
                Optional<Customer> currentCustomer = authService.getCurrentCustomer();
                if (currentCustomer.isPresent()) {
                    salonService.saveSalonForCustomer(salon, currentCustomer.get());
                    redirectAttributes.addFlashAttribute("success",
                            "Salon created successfully! Your account has been converted to ADMIN. " +
                                    "Please log out and log back in with the same username and password to access admin features.");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Unable to identify user. Please try again.");
                    return "redirect:/salons";
                }
            }
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/salons";
        }

        return "redirect:/salons";
    }

    @GetMapping("/delete/{id}")
    public String deleteSalon(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Admin> currentAdmin = authService.getCurrentAdmin();
        if (currentAdmin.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in as an admin to delete salons.");
            return "redirect:/login";
        }

        try {
            salonService.deleteSalon(id, currentAdmin.get());
            redirectAttributes.addFlashAttribute("success", "Salon deleted successfully.");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/salons";
    }
}
