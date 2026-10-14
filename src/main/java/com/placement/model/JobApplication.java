package com.placement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "job_id"}))
public class JobApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "student_id") public Student student;
    @ManyToOne(optional = false) @JoinColumn(name = "job_id") public Job job;
    public String status = "APPLIED";
    public LocalDateTime appliedOn = LocalDateTime.now();
}
