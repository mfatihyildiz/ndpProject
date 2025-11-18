package com.example.ndpproject.controller;

import com.example.ndpproject.entity.Services;
import com.example.ndpproject.service.SalonService;
import com.example.ndpproject.service.ServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/services")
public class ServicesController {

    private final ServicesService servicesService;
    private final SalonService salonService;

    @Autowired
    public ServicesController(ServicesService servicesService, SalonService salonService) {
        this.servicesService = servicesService;
        this.salonService = salonService;
    }

    @GetMapping
    public String listServices(Model model) {
        model.addAttribute("services", servicesService.getAllServices());
        return "services";
    }

    @GetMapping("/new")
    public String showServiceForm(Model model) {
        model.addAttribute("service", new Services());
        model.addAttribute("salons", salonService.getAllSalons());
        return "service-form";
    }

    @PostMapping("/save")
    public String saveService(@ModelAttribute Services services) {
        servicesService.saveService(services);
        return "redirect:/services";
    }

    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id) {
        servicesService.deleteService(id);
        return "redirect:/services";
    }
}
