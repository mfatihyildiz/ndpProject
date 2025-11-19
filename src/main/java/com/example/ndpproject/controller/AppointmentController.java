package com.example.ndpproject.controller;

import com.example.ndpproject.entity.Appointment;
import com.example.ndpproject.entity.Customer;
import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.entity.Services;
import com.example.ndpproject.enums.Status;
import com.example.ndpproject.service.AppointmentService;
import com.example.ndpproject.service.CustomerService;
import com.example.ndpproject.service.EmployeeService;
import com.example.ndpproject.service.ServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final ServicesService servicesService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService,
                                 CustomerService customerService,
                                 EmployeeService employeeService,
                                 ServicesService servicesService) {
        this.appointmentService = appointmentService;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.servicesService = servicesService;
    }

    @GetMapping
    public String listAppointments(Model model) {
        model.addAttribute("appointments", appointmentService.getAllAppointments());
        return "appointments";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("services", servicesService.getAllServices());
        return "appointment-form";
    }

    @PostMapping("/save")
    public String createAppointment(@RequestParam Long customerId,
                                    @RequestParam Long employeeId,
                                    @RequestParam Long serviceId,
                                    @RequestParam String dateTime) {
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Employee employee = employeeService.getEmployeeById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Services services = servicesService.getServiceById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        LocalDateTime dt = LocalDateTime.parse(dateTime);

        if (!appointmentService.isEmployeeAvailable(employee, dt)) {
            throw new RuntimeException("Employee already has an appointment at this time!");
        }

        Appointment appointment = new Appointment(dt, Status.PENDING,
                customer, employee, services);

        appointmentService.saveAppointment(appointment);
        return "redirect:/appointments";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam Status status) {
        appointmentService.updateStatus(id, status);
        return "redirect:/appointments";
    }

    @GetMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return "redirect:/appointments";
    }
}
