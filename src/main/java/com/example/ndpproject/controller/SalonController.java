package com.example.ndpproject.controller;

import com.example.ndpproject.entity.Salon;
import com.example.ndpproject.service.SalonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/salons")
public class SalonController {

    private final SalonService salonService;

    @Autowired
    public SalonController(SalonService salonService) {
        this.salonService = salonService;
    }

    @GetMapping
    public String listSalons(Model model) {
        model.addAttribute("salons", salonService.getAllSalons());
        return "salons";
    }

    @GetMapping("/new")
    public String showSalonForm(Model model) {
        model.addAttribute("salon", new Salon());
        return "salon-form";
    }

    @PostMapping("/save")
    public String saveSalon(@ModelAttribute Salon salon) {
        salonService.saveSalon(salon);
        return "redirect:/salons";
    }

    @GetMapping("/delete/{id}")
    public String deleteSalon(@PathVariable Long id) {
        salonService.deleteSalon(id);
        return "redirect:/salons";
    }
}
