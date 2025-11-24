package com.example.ndpproject.entity;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "working_hours")
public class WorkingHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "salon_id", nullable = true)
    private Salon salon;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = true)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = false;

    public WorkingHours() {}

    public WorkingHours(Salon salon, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.salon = salon;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isClosed = false;
    }

    public WorkingHours(Salon salon, DayOfWeek dayOfWeek, boolean isClosed) {
        this.salon = salon;
        this.dayOfWeek = dayOfWeek;
        this.isClosed = isClosed;
        if (isClosed) {
            this.startTime = LocalTime.MIN;
            this.endTime = LocalTime.MIN;
        }
    }

    public WorkingHours(Employee employee, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.employee = employee;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isClosed = false;
    }

    public WorkingHours(Employee employee, DayOfWeek dayOfWeek, boolean isClosed) {
        this.employee = employee;
        this.dayOfWeek = dayOfWeek;
        this.isClosed = isClosed;
        if (isClosed) {
            this.startTime = LocalTime.MIN;
            this.endTime = LocalTime.MIN;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Salon getSalon() { return salon; }
    public void setSalon(Salon salon) { this.salon = salon; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Boolean getIsClosed() { return isClosed; }
    public void setIsClosed(Boolean isClosed) { this.isClosed = isClosed; }

    public boolean isWithinHours(LocalTime time) {
        if (isClosed) {
            return false;
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public boolean isWithinHours(LocalTime start, LocalTime end) {
        if (isClosed) {
            return false;
        }
        return !start.isBefore(startTime) && !end.isAfter(endTime);
    }

    @Override
    public String toString() {
        if (isClosed) {
            return dayOfWeek + ": Closed";
        }
        return dayOfWeek + ": " + startTime + " - " + endTime;
    }
}
