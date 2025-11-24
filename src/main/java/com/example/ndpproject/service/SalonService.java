package com.example.ndpproject.service;

import com.example.ndpproject.entity.Admin;
import com.example.ndpproject.entity.Customer;
import com.example.ndpproject.entity.Salon;
import com.example.ndpproject.entity.WorkingHours;
import com.example.ndpproject.enums.Role;
import com.example.ndpproject.repository.AdminRepo;
import com.example.ndpproject.repository.CustomerRepo;
import com.example.ndpproject.repository.SalonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SalonService {

    private final SalonRepo salonRepo;
    private final AdminRepo adminRepo;
    private final CustomerRepo customerRepo;
    private final PasswordEncoder passwordEncoder;
    private final WorkingHoursService workingHoursService;

    @Autowired
    public SalonService(SalonRepo salonRepo, AdminRepo adminRepo, CustomerRepo customerRepo,
                        PasswordEncoder passwordEncoder, WorkingHoursService workingHoursService) {
        this.salonRepo = salonRepo;
        this.adminRepo = adminRepo;
        this.customerRepo = customerRepo;
        this.passwordEncoder = passwordEncoder;
        this.workingHoursService = workingHoursService;
    }

    public List<Salon> getAllSalons() {
        return salonRepo.findAll();
    }

    public List<Salon> getSalonsByManager(Admin manager) {
        return salonRepo.findByManager(manager);
    }

    public Optional<Salon> getSalonById(Long id) {
        return salonRepo.findById(id);
    }

    public Salon saveSalon(Salon salon, Admin manager) {
        if (salon.getId() != null) {
            if (!salonRepo.existsByIdAndManager(salon.getId(), manager)) {
                throw new AccessDeniedException("You are not authorized to modify this salon. Only the salon manager can make changes.");
            }
        } else {
            salon.setManager(manager);
        }
        return salonRepo.save(salon);
    }

    @Transactional
    public Salon saveSalonForCustomer(Salon salon, Customer customer) {
        Optional<Admin> existingAdmin = adminRepo.findByUsername(customer.getUsername());
        Admin admin;

        if (existingAdmin.isPresent()) {
            admin = existingAdmin.get();
            admin.setRole(Role.ADMIN);
            adminRepo.save(admin);
        } else {
            admin = new Admin(customer.getUsername(), customer.getPassword(), customer.getFullName());
            adminRepo.save(admin);
        }

        customerRepo.delete(customer);

        salon.setManager(admin);
        return salonRepo.save(salon);
    }

    public void deleteSalon(Long id, Admin manager) {
        if (!salonRepo.existsByIdAndManager(id, manager)) {
            throw new AccessDeniedException("You are not authorized to delete this salon. Only the salon manager can delete it.");
        }
        salonRepo.deleteById(id);
    }

    public boolean isManagerOfSalon(Long salonId, Admin manager) {
        return salonRepo.existsByIdAndManager(salonId, manager);
    }

    @Transactional
    public void saveWorkingHoursForSalon(Salon salon, Map<String, String> allParams) {
        workingHoursService.deleteAllBySalon(salon);

        for (DayOfWeek day : DayOfWeek.values()) {
            String dayName = day.name();
            String closedParam = "closed_" + dayName;
            String startTimeParam = "startTime_" + dayName;
            String endTimeParam = "endTime_" + dayName;

            boolean isClosed = allParams.containsKey(closedParam) &&
                    "on".equals(allParams.get(closedParam));

            WorkingHours workingHours;
            if (isClosed) {
                workingHours = new WorkingHours(salon, day, true);
            } else {
                String startTimeStr = allParams.get(startTimeParam);
                String endTimeStr = allParams.get(endTimeParam);

                if (startTimeStr != null && !startTimeStr.isEmpty() &&
                        endTimeStr != null && !endTimeStr.isEmpty()) {
                    LocalTime startTime = LocalTime.parse(startTimeStr);
                    LocalTime endTime = LocalTime.parse(endTimeStr);
                    workingHours = new WorkingHours(salon, day, startTime, endTime);
                } else {
                    continue;
                }
            }

            workingHoursService.saveWorkingHours(workingHours);
        }
    }
}
