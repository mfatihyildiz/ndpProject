package com.example.ndpproject.controller;

import com.example.ndpproject.entity.Admin;
import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.entity.Salon;
import com.example.ndpproject.service.AuthService;
import com.example.ndpproject.service.EmployeeService;
import com.example.ndpproject.service.SalonService;
import com.example.ndpproject.service.ServicesService;
import com.example.ndpproject.service.WorkingHoursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SalonService salonService;
    private final ServicesService servicesService;
    private final AuthService authService;
    private final WorkingHoursService workingHoursService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, SalonService salonService, ServicesService servicesService,
                              AuthService authService, WorkingHoursService workingHoursService) {
        this.employeeService = employeeService;
        this.salonService = salonService;
        this.servicesService = servicesService;
        this.authService = authService;
        this.workingHoursService = workingHoursService;
    }

    @GetMapping
    public String listEmployees(Model model) {
        Optional<Admin> currentAdmin = authService.getCurrentAdmin();

        if (currentAdmin.isPresent()) {
            List<Salon> managedSalons = salonService.getSalonsByManager(currentAdmin.get());
            List<Employee> employees = managedSalons.stream()
                    .flatMap(salon -> employeeService.getEmployeesBySalon(salon).stream())
                    .collect(Collectors.toList());

            employees.forEach(employee -> {
                if (employee.getWorkingHours() == null || employee.getWorkingHours().isEmpty()) {
                    employee.setWorkingHours(workingHoursService.getWorkingHoursByEmployee(employee));
                }
            });

            model.addAttribute("employees", employees);
        } else {
            model.addAttribute("employees", List.of());
        }
        return "employees";
    }

    @GetMapping("/new")
    public String showAddEmployeeForm(Model model) {
        Optional<Admin> currentAdmin = authService.getCurrentAdmin();

        if (currentAdmin.isEmpty()) {
            return "redirect:/login";
        }

        List<Salon> managedSalons = salonService.getSalonsByManager(currentAdmin.get());

        if (managedSalons.isEmpty()) {
            model.addAttribute("error", "You need to create a salon first before adding employees.");
            return "redirect:/salons";
        }

        List<com.example.ndpproject.entity.Services> services = managedSalons.stream()
                .flatMap(salon -> servicesService.getServicesBySalon(salon).stream())
                .collect(Collectors.toList());

        model.addAttribute("employee", new Employee());
        model.addAttribute("salons", managedSalons);
        model.addAttribute("servicesList", services);
        model.addAttribute("daysOfWeek", DayOfWeek.values());
        return "employee-form";
    }

    @PostMapping("/save")
    public String saveEmployee(@ModelAttribute Employee employee, @RequestParam Long salonId,
                               @RequestParam(value = "serviceIds", required = false) List<Long> serviceIds,
                               @RequestParam Map<String, String> allParams, RedirectAttributes redirectAttributes) {
        Optional<Admin> currentAdmin = authService.getCurrentAdmin();

        if (currentAdmin.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in as an admin to add employees.");
            return "redirect:/login";
        }

        Salon salon = salonService.getSalonById(salonId)
                .orElseThrow(() -> new IllegalArgumentException("Salon not found"));

        if (!salonService.isManagerOfSalon(salonId, currentAdmin.get())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to add employees to this salon. Only the salon manager can add employees.");
            return "redirect:/employees/new";
        }

        employee.setSalon(salon);

        if (serviceIds != null && !serviceIds.isEmpty()) {
            employee.setSkills(new HashSet<>(servicesService.getServicesByIds(serviceIds)));
        } else {
            employee.setSkills(new HashSet<>());
        }

        Employee savedEmployee = employeeService.saveEmployee(employee);

        saveWorkingHoursForEmployee(savedEmployee, allParams);

        redirectAttributes.addFlashAttribute("success", "Employee added successfully.");
        return "redirect:/employees";
    }

    private void saveWorkingHoursForEmployee(Employee employee, Map<String, String> allParams) {
        workingHoursService.deleteAllByEmployee(employee);

        for (DayOfWeek day : DayOfWeek.values()) {
            String dayName = day.name();
            String closedParam = "closed_" + dayName;
            String startTimeParam = "startTime_" + dayName;
            String endTimeParam = "endTime_" + dayName;

            boolean isClosed = allParams.containsKey(closedParam) &&
                    "on".equals(allParams.get(closedParam));

            com.example.ndpproject.entity.WorkingHours workingHours;
            if (isClosed) {
                workingHours = new com.example.ndpproject.entity.WorkingHours(employee, day, true);
            } else {
                String startTimeStr = allParams.get(startTimeParam);
                String endTimeStr = allParams.get(endTimeParam);

                if (startTimeStr != null && !startTimeStr.isEmpty() &&
                        endTimeStr != null && !endTimeStr.isEmpty()) {
                    java.time.LocalTime startTime = java.time.LocalTime.parse(startTimeStr);
                    java.time.LocalTime endTime = java.time.LocalTime.parse(endTimeStr);
                    workingHours = new com.example.ndpproject.entity.WorkingHours(employee, day, startTime, endTime);
                } else {
                    continue;
                }
            }

            workingHoursService.saveWorkingHours(workingHours);
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Admin> currentAdmin = authService.getCurrentAdmin();

        if (currentAdmin.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in as an admin to delete employees.");
            return "redirect:/login";
        }

        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (employee.getSalon() == null ||
                !salonService.isManagerOfSalon(employee.getSalon().getId(), currentAdmin.get())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this employee. Only the salon manager can delete employees.");
            return "redirect:/employees";
        }

        employeeService.deleteEmployee(id);
        redirectAttributes.addFlashAttribute("success", "Employee deleted successfully.");
        return "redirect:/employees";
    }
}
