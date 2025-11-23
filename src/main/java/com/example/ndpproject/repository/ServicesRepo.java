package com.example.ndpproject.repository;

import com.example.ndpproject.entity.Services;
import com.example.ndpproject.entity.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicesRepo extends JpaRepository<Services, Long> {
    List<Services> findBySalon(Salon salon);
}
