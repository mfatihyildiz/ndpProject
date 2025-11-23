package com.example.ndpproject.controller;

import com.example.ndpproject.entity.AvailabilitySlot;
import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.service.AuthService;
import com.example.ndpproject.service.AvailabilitySlotService;
import com.example.ndpproject.service.EmployeeService;
import com.example.ndpproject.service.SalonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/availability")
public class AvailabilityController {

    private final AvailabilitySlotService slotService;
    private final EmployeeService employeeService;
    private final AuthService authService;
    private final SalonService salonService;

    @Autowired
    public AvailabilityController(AvailabilitySlotService slotService, EmployeeService employeeService,
                                  AuthService authService, SalonService salonService) {
        this.slotService = slotService;
        this.employeeService = employeeService;
        this.authService = authService;
        this.salonService = salonService;
    }

    @GetMapping
    public String listSlots(Model model) {
        List<AvailabilitySlot> slots;
        Optional<com.example.ndpproject.entity.Admin> admin = authService.getCurrentAdmin();
        Optional<Employee> employee = authService.getCurrentEmployee();

        if (admin.isPresent()) {
            List<com.example.ndpproject.entity.Salon> managedSalons = salonService.getSalonsByManager(admin.get());
            List<Employee> managedEmployees = managedSalons.stream()
                    .flatMap(salon -> employeeService.getEmployeesBySalon(salon).stream())
                    .collect(Collectors.toList());
            slots = managedEmployees.stream()
                    .flatMap(emp -> slotService.getSlotsForEmployee(emp).stream())
                    .collect(Collectors.toList());
            model.addAttribute("userRole", "ADMIN");
        } else if (employee.isPresent()) {
            slots = slotService.getSlotsForEmployee(employee.get());
            model.addAttribute("userRole", "EMPLOYEE");
        } else {
            slots = slotService.getAllSlots();
            model.addAttribute("userRole", "GUEST");
        }

        model.addAttribute("slots", slots);
        return "availability";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Optional<com.example.ndpproject.entity.Admin> admin = authService.getCurrentAdmin();
        Optional<Employee> employee = authService.getCurrentEmployee();

        List<Employee> employees;
        boolean isEmployee = false;
        Long selectedEmployeeId = null;

        if (admin.isPresent()) {
            List<com.example.ndpproject.entity.Salon> managedSalons = salonService.getSalonsByManager(admin.get());
            employees = managedSalons.stream()
                    .flatMap(salon -> employeeService.getEmployeesBySalon(salon).stream())
                    .collect(Collectors.toList());
            model.addAttribute("userRole", "ADMIN");
        } else if (employee.isPresent()) {
            employees = List.of(employee.get());
            selectedEmployeeId = employee.get().getId();
            isEmployee = true;
            model.addAttribute("userRole", "EMPLOYEE");
        } else {
            return "redirect:/login";
        }

        if (employees.isEmpty()) {
            model.addAttribute("error", "No employees available. Please add employees to your salon first.");
            return "redirect:/employees";
        }

        model.addAttribute("slot", new AvailabilitySlot());
        model.addAttribute("employees", employees);
        model.addAttribute("isEmployee", isEmployee);
        model.addAttribute("selectedEmployeeId", selectedEmployeeId);
        return "availability-form";
    }

    @PostMapping("/save")
    public String saveSlot(@RequestParam Long employeeId, @RequestParam String date, @RequestParam String startTime,
                           @RequestParam String endTime, RedirectAttributes redirectAttributes) {
        Optional<com.example.ndpproject.entity.Admin> admin = authService.getCurrentAdmin();
        Optional<Employee> currentEmployee = authService.getCurrentEmployee();

        Employee employee = employeeService.getEmployeeById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (admin.isPresent()) {
            if (employee.getSalon() == null ||
                    !salonService.isManagerOfSalon(employee.getSalon().getId(), admin.get())) {
                redirectAttributes.addFlashAttribute("error",
                        "You are not authorized to create availability for this employee. " +
                                "You can only create availability for employees in your managed salons.");
                return "redirect:/availability/new";
            }
        } else if (currentEmployee.isPresent()) {
            if (!currentEmployee.get().getId().equals(employeeId)) {
                redirectAttributes.addFlashAttribute("error",
                        "You can only create availability for yourself.");
                return "redirect:/availability/new";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to create availability.");
            return "redirect:/login";
        }

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setEmployee(employee);
        slot.setDate(LocalDate.parse(date));
        slot.setStartTime(LocalTime.parse(startTime));
        slot.setEndTime(LocalTime.parse(endTime));

        slotService.saveSlot(slot);
        redirectAttributes.addFlashAttribute("success", "Availability slot created successfully.");
        return "redirect:/availability";
    }

    @GetMapping("/delete/{id}")
    public String deleteSlot(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<com.example.ndpproject.entity.Admin> admin = authService.getCurrentAdmin();
        Optional<Employee> currentEmployee = authService.getCurrentEmployee();

        Optional<AvailabilitySlot> slotOpt = slotService.getSlotById(id);
        if (slotOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Availability slot not found.");
            return "redirect:/availability";
        }

        AvailabilitySlot slot = slotOpt.get();

        if (admin.isPresent()) {
            if (slot.getEmployee().getSalon() == null ||
                    !salonService.isManagerOfSalon(slot.getEmployee().getSalon().getId(), admin.get())) {
                redirectAttributes.addFlashAttribute("error",
                        "You are not authorized to delete this availability slot.");
                return "redirect:/availability";
            }
        } else if (currentEmployee.isPresent()) {
            if (!slot.getEmployee().getId().equals(currentEmployee.get().getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "You can only delete your own availability slots.");
                return "redirect:/availability";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to delete availability.");
            return "redirect:/login";
        }

        slotService.deleteSlot(id);
        redirectAttributes.addFlashAttribute("success", "Availability slot deleted successfully.");
        return "redirect:/availability";
    }
}
