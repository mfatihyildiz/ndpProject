package com.example.ndpproject.repository;

import com.example.ndpproject.entity.Admin;
import com.example.ndpproject.entity.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalonRepo extends JpaRepository<Salon, Long> {

    List<Salon> findByManager(Admin manager);
    boolean existsByIdAndManager(Long id, Admin manager);
}
