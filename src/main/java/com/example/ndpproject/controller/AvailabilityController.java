package com.example.ndpproject.controller;

import com.example.ndpproject.entity.AvailabilitySlot;
import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.service.AvailabilitySlotService;
import com.example.ndpproject.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/availability")
public class AvailabilityController {

    private final AvailabilitySlotService slotService;
    private final EmployeeService employeeService;

    @Autowired
    public AvailabilityController(AvailabilitySlotService slotService,
                                  EmployeeService employeeService) {
        this.slotService = slotService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String listSlots(Model model) {
        model.addAttribute("slots", slotService.getAllSlots());
        return "availability";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("slot", new AvailabilitySlot());
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "availability-form.html";
    }

    @PostMapping("/save")
    public String saveSlot(@RequestParam Long employeeId,
                           @RequestParam String date,
                           @RequestParam String startTime,
                           @RequestParam String endTime) {
        Employee employee = employeeService.getEmployeeById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setEmployee(employee);
        slot.setDate(LocalDate.parse(date));
        slot.setStartTime(LocalTime.parse(startTime));
        slot.setEndTime(LocalTime.parse(endTime));

        slotService.saveSlot(slot);
        return "redirect:/availability";
    }

    @GetMapping("/delete/{id}")
    public String deleteSlot(@PathVariable Long id) {
        slotService.deleteSlot(id);
        return "redirect:/availability";
    }
}
