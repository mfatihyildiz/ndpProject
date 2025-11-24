package com.example.ndpproject.controller;

import com.example.ndpproject.entity.Admin;
import com.example.ndpproject.entity.Salon;
import com.example.ndpproject.entity.Services;
import com.example.ndpproject.service.AuthService;
import com.example.ndpproject.service.SalonService;
import com.example.ndpproject.service.ServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/services")
public class ServicesController {

    private final ServicesService servicesService;
    private final SalonService salonService;
    private final AuthService authService;

    @Autowired
    public ServicesController(ServicesService servicesService, SalonService salonService, AuthService authService) {
        this.servicesService = servicesService;
        this.salonService = salonService;
        this.authService = authService;
    }

    @GetMapping
    public String listServices(Model model) {
        Optional<Admin> currentAdmin = authService.getCurrentAdmin();

        List<Services> services;
        if (currentAdmin.isPresent()) {
            List<Salon> managedSalons = salonService.getSalonsByManager(currentAdmin.get());
            services = managedSalons.stream()
                    .flatMap(salon -> servicesService.getServicesBySalon(salon).stream())
                    .collect(Collectors.toList());
        } else {
            services = servicesService.getAllServices();
        }

        model.addAttribute("services", services);
        model.addAttribute("isAdmin", currentAdmin.isPresent());
        return "services";
    }

    @GetMapping("/new")
    public String showServiceForm(Model model, RedirectAttributes redirectAttributes) {
        Optional<Admin> currentAdmin = authService.getCurrentAdmin();
        if (currentAdmin.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in as an admin to add services.");
            return "redirect:/login";
        }

        List<Salon> managedSalons = salonService.getSalonsByManager(currentAdmin.get());

        if (managedSalons.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "You need to create a salon first before adding services.");
            return "redirect:/salons";
        }

        model.addAttribute("service", new Services());
        model.addAttribute("salons", managedSalons);
        return "service-form";
    }

    @PostMapping("/save")
    public String saveService(@ModelAttribute Services service, @RequestParam Long salonId, RedirectAttributes redirectAttributes) {
        Optional<Admin> currentAdmin = authService.getCurrentAdmin();
        if (currentAdmin.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in as an admin to add services.");
            return "redirect:/login";
        }

        Optional<Salon> salon = salonService.getSalonById(salonId);
        if (salon.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Selected salon not found.");
            return "redirect:/services/new";
        }

        if (!salonService.isManagerOfSalon(salonId, currentAdmin.get())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to add services to this salon. You can only add services to your own salons.");
            return "redirect:/services/new";
        }

        service.setSalon(salon.get());
        servicesService.saveService(service);
        redirectAttributes.addFlashAttribute("success", "Service added successfully.");
        return "redirect:/services";
    }

    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Admin> currentAdmin = authService.getCurrentAdmin();
        if (currentAdmin.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in as an admin to delete services.");
            return "redirect:/login";
        }

        Optional<Services> service = servicesService.getServiceById(id);
        if (service.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Service not found.");
            return "redirect:/services";
        }

        Salon salon = service.get().getSalon();
        if (salon == null || !salonService.isManagerOfSalon(salon.getId(), currentAdmin.get())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this service. You can only delete services from your own salons.");
            return "redirect:/services";
        }

        servicesService.deleteService(id);
        redirectAttributes.addFlashAttribute("success", "Service deleted successfully.");
        return "redirect:/services";
    }
}
