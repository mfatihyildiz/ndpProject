package com.example.ndpproject.service;

import com.example.ndpproject.entity.Services;
import com.example.ndpproject.repository.ServicesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicesService {

    private final ServicesRepo servicesRepo;

    @Autowired
    public ServicesService(ServicesRepo servicesRepo) {
        this.servicesRepo = servicesRepo;
    }

    public List<Services> getAllServices() {
        return servicesRepo.findAll();
    }

    public Optional<Services> getServiceById(Long id) {
        return servicesRepo.findById(id);
    }

    public Services saveService(Services service) {
        return servicesRepo.save(service);
    }

    public void deleteService(Long id) {
        servicesRepo.deleteById(id);
    }
}

