package com.example.ndpproject.repository;

import com.example.ndpproject.entity.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalonRepo extends JpaRepository<Salon, Long> {
}
