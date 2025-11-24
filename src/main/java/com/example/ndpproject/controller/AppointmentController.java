package com.example.ndpproject.controller;

import com.example.ndpproject.entity.Appointment;
import com.example.ndpproject.entity.Customer;
import com.example.ndpproject.entity.Employee;
import com.example.ndpproject.entity.Services;
import com.example.ndpproject.enums.Status;
import com.example.ndpproject.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final AuthService authService;
    private final WorkingHoursService workingHoursService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, CustomerService customerService, EmployeeService employeeService,
                                 ServicesService servicesService, SalonService salonService, AvailabilitySlotService availabilitySlotService,
                                 AuthService authService, WorkingHoursService workingHoursService) {
        this.appointmentService = appointmentService;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.servicesService = servicesService;
        this.salonService = salonService;
        this.availabilitySlotService = availabilitySlotService;
        this.authService = authService;
        this.workingHoursService = workingHoursService;
    }

    @GetMapping
    public String listAppointments(Model model) {
        List<Appointment> appointments;
        boolean canApprove = false;

        Optional<com.example.ndpproject.entity.Admin> admin = authService.getCurrentAdmin();
        Optional<Customer> customer = authService.getCurrentCustomer();
        Optional<Employee> employee = authService.getCurrentEmployee();

        if (admin.isPresent()) {
            appointments = appointmentService.getAllAppointments();
            canApprove = true;
            model.addAttribute("userRole", "ADMIN");
        } else if (employee.isPresent()) {
            appointments = appointmentService.getAppointmentsByEmployee(employee.get());
            canApprove = true;
            model.addAttribute("userRole", "EMPLOYEE");
        } else if (customer.isPresent()) {
            appointments = appointmentService.getAppointmentsByCustomer(customer.get());
            canApprove = false;
            model.addAttribute("userRole", "CUSTOMER");
        } else {
            appointments = appointmentService.getAllAppointments();
            canApprove = false;
            model.addAttribute("userRole", "GUEST");
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("canApprove", canApprove);
        model.addAttribute("statuses", Status.values());
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

        Optional<Customer> currentCustomer = authService.getCurrentCustomer();
        Optional<com.example.ndpproject.entity.Admin> currentAdmin = authService.getCurrentAdmin();
        Optional<Employee> currentEmployee = authService.getCurrentEmployee();

        boolean isCustomer = currentCustomer.isPresent() &&
                (currentAdmin.isEmpty() && currentEmployee.isEmpty());

        model.addAttribute("appointment", new Appointment());

        if (isCustomer) {
            model.addAttribute("customers", List.of(currentCustomer.get()));
            model.addAttribute("selectedCustomerId", currentCustomer.get().getId());
            model.addAttribute("isCustomer", true);
        } else {
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("isCustomer", false);
        }

        model.addAttribute("salons", salonService.getAllSalons());
        model.addAttribute("employees", employees);
        model.addAttribute("services", servicesService.getAllServices());
        model.addAttribute("employeeAvailability", availabilitySummary);
        return "appointment-form";
    }

    @GetMapping("/available-slots")
    @ResponseBody
    public List<String> getAvailableSlots(@RequestParam Long employeeId, @RequestParam String date, @RequestParam int serviceDuration) {
        Employee employee = employeeService.getEmployeeById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LocalDate appointmentDate = LocalDate.parse(date);
        List<LocalDateTime> slots = appointmentService.getAvailableTimeSlots(employee, appointmentDate, serviceDuration);

        return slots.stream()
                .map(slot -> slot.toString())
                .collect(java.util.stream.Collectors.toList());
    }

    @PostMapping("/save")
    public String createAppointment(@RequestParam Long customerId, @RequestParam Long salonId, @RequestParam Long employeeId,
                                    @RequestParam Long serviceId, @RequestParam String dateTime, RedirectAttributes redirectAttributes) {
        Optional<Customer> currentCustomer = authService.getCurrentCustomer();
        Optional<com.example.ndpproject.entity.Admin> currentAdmin = authService.getCurrentAdmin();
        Optional<Employee> currentEmployee = authService.getCurrentEmployee();

        boolean isCustomer = currentCustomer.isPresent() &&
                (currentAdmin.isEmpty() && currentEmployee.isEmpty());

        if (isCustomer && !currentCustomer.get().getId().equals(customerId)) {
            redirectAttributes.addFlashAttribute("error", "You can only create appointments for yourself.");
            return "redirect:/appointments/new";
        }

        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Employee employee = employeeService.getEmployeeById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Services services = servicesService.getServiceById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (employee.getSalon() == null || !employee.getSalon().getId().equals(salonId)) {
            redirectAttributes.addFlashAttribute("error", "Selected employee does not belong to the chosen salon.");
            return "redirect:/appointments/new";
        }

        if (!employeeService.canPerformService(employeeId, serviceId)) {
            redirectAttributes.addFlashAttribute("error", "Employee cannot perform the selected service!");
            return "redirect:/appointments/new";
        }

        LocalDateTime dt = LocalDateTime.parse(dateTime);
        int duration = services.getDuration();

        if (!appointmentService.isWithinSalonWorkingHours(employee, dt, duration)) {
            redirectAttributes.addFlashAttribute("error",
                    "Selected time is outside the salon's working hours. Please check the salon's operating hours.");
            return "redirect:/appointments/new";
        }

        if (!workingHoursService.isWithinEmployeeWorkingHours(employee, dt, duration)) {
            redirectAttributes.addFlashAttribute("error",
                    "Selected time is outside the employee's working hours.");
            return "redirect:/appointments/new";
        }

        if (!appointmentService.isEmployeeAvailable(employee, dt, duration)) {
            redirectAttributes.addFlashAttribute("error",
                    "Employee already has an appointment that overlaps this time!");
            return "redirect:/appointments/new";
        }

        Appointment appointment = new Appointment(dt, Status.PENDING,
                customer, employee, services);

        appointmentService.saveAppointment(appointment);
        redirectAttributes.addFlashAttribute("success", "Appointment created successfully! It is pending approval.");
        return "redirect:/appointments";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam Status status, RedirectAttributes redirectAttributes) {
        Optional<com.example.ndpproject.entity.Admin> admin = authService.getCurrentAdmin();
        Optional<Employee> employee = authService.getCurrentEmployee();

        if (admin.isEmpty() && employee.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to change appointment status. Only admins and employees can approve appointments.");
            return "redirect:/appointments";
        }

        appointmentService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("success", "Appointment status updated successfully.");
        return "redirect:/appointments";
    }

    @GetMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return "redirect:/appointments";
    }
}
