package com.example.ndpproject.controller;

import com.example.ndpproject.entity.Appointment;
import com.example.ndpproject.entity.Customer;
import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.entity.Services;
import com.example.ndpproject.enums.Status;
import com.example.ndpproject.service.AvailabilitySlotService;
import com.example.ndpproject.service.AppointmentService;
import com.example.ndpproject.service.CustomerService;
import com.example.ndpproject.service.EmployeeService;
import com.example.ndpproject.service.SalonService;
import com.example.ndpproject.service.ServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final ServicesService servicesService;
    private final SalonService salonService;
    private final AvailabilitySlotService availabilitySlotService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, CustomerService customerService, EmployeeService employeeService,
                                 ServicesService servicesService, SalonService salonService, AvailabilitySlotService availabilitySlotService) {
        this.appointmentService = appointmentService;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.servicesService = servicesService;
        this.salonService = salonService;
        this.availabilitySlotService = availabilitySlotService;
    }

    @GetMapping
    public String listAppointments(Model model) {
        model.addAttribute("appointments", appointmentService.getAllAppointments());
        return "appointments";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        Map<Long, String> availabilitySummary = employees.stream()
                .collect(Collectors.toMap(
                        Employee::getId,
                        employee -> availabilitySlotService.getSlotsForEmployee(employee).stream()
                                .map(slot -> slot.getDate() + " " + slot.getStartTime() + "-" + slot.getEndTime())
                                .collect(Collectors.joining(" | "))
                ));

        model.addAttribute("appointment", new Appointment());
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("salons", salonService.getAllSalons());
        model.addAttribute("employees", employees);
        model.addAttribute("services", servicesService.getAllServices());
        model.addAttribute("employeeAvailability", availabilitySummary);
        return "appointment-form";
    }

    @PostMapping("/save")
    public String createAppointment(@RequestParam Long customerId, @RequestParam Long salonId, @RequestParam Long employeeId,
                                    @RequestParam Long serviceId, @RequestParam String dateTime) {
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Employee employee = employeeService.getEmployeeById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Services services = servicesService.getServiceById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (employee.getSalon() == null || !employee.getSalon().getId().equals(salonId)) {
            throw new RuntimeException("Selected employee does not belong to the chosen salon.");
        }

        if (!employeeService.canPerformService(employeeId, serviceId)) {
            throw new RuntimeException("Employee cannot perform the selected service!");
        }

        LocalDateTime dt = LocalDateTime.parse(dateTime);
        int duration = services.getDuration();

        if (!appointmentService.isWithinEmployeeAvailability(employee, dt, duration)) {
            throw new RuntimeException("Selected time is outside the employee's availability.");
        }

        if (!appointmentService.isEmployeeAvailable(employee, dt, duration)) {
            throw new RuntimeException("Employee already has an appointment that overlaps this time!");
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
