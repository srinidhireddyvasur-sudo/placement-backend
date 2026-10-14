package com.placement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "jobs")
public class Job {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "company_id") public Company company;
    public String title;
    @Column(length = 1000) public String description;
    public double minCgpa;
    public int maxBacklogs;
    public String branches = "ALL";
    public double salaryLpa;
    public LocalDate lastDate;

    public boolean allows(Student s) {
        if (s.cgpa < minCgpa || s.backlogs > maxBacklogs) return false;
        if (branches == null || branches.isBlank() || branches.trim().equalsIgnoreCase("ALL")) return true;
        if (s.branch == null) return false;
        for (String b : branches.split(",")) {
            if (b.trim().equalsIgnoreCase(s.branch.trim())) return true;
        }
        return false;
    }
}
