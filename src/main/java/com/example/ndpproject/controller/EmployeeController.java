package com.example.ndpproject.controller;

import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.entity.Salon;
import com.example.ndpproject.service.EmployeeService;
import com.example.ndpproject.service.SalonService;
import com.example.ndpproject.service.ServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SalonService salonService;
    private final ServicesService servicesService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, SalonService salonService, ServicesService servicesService) {
        this.employeeService = employeeService;
        this.salonService = salonService;
        this.servicesService = servicesService;
    }

    @GetMapping
    public String listEmployees(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "employees";
    }

    @GetMapping("/new")
    public String showAddEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("salons", salonService.getAllSalons());
        model.addAttribute("servicesList", servicesService.getAllServices());
        return "employee-form";
    }

    @PostMapping("/save")
    public String saveEmployee(@ModelAttribute Employee employee,
                               @RequestParam Long salonId,
                               @RequestParam(value = "serviceIds", required = false) List<Long> serviceIds) {
        Salon salon = salonService.getSalonById(salonId)
                .orElseThrow(() -> new IllegalArgumentException("Salon not found"));
        employee.setSalon(salon);

        if (serviceIds != null && !serviceIds.isEmpty()) {
            employee.setSkills(new HashSet<>(servicesService.getServicesByIds(serviceIds)));
        } else {
            employee.setSkills(new HashSet<>());
        }
        employeeService.saveEmployee(employee);
        return "redirect:/employees";
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/employees";
    }
}
