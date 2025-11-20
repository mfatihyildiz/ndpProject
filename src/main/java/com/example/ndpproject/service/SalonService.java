package com.example.ndpproject.service;

import com.example.ndpproject.entity.Salon;
import com.example.ndpproject.repository.SalonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalonService {

    private final SalonRepo salonRepo;

    @Autowired
    public SalonService(SalonRepo salonRepo) {
        this.salonRepo = salonRepo;
    }

    public List<Salon> getAllSalons() {
        return salonRepo.findAll();
    }

    public Salon saveSalon(Salon salon) {
        return salonRepo.save(salon);
    }

    public void deleteSalon(Long id) {
        salonRepo.deleteById(id);
    }

    public Optional<Salon> getSalonById(Long id) {
        return salonRepo.findById(id);
    }
}
