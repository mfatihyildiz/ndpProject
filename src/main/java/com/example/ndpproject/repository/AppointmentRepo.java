package com.example.ndpproject.repository;

import com.example.ndpproject.entity.Appointment;
import com.example.ndpproject.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {
    List<Appointment> findByEmployee(Employee employee);
}
