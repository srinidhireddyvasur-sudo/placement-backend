package com.placement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @JsonIgnore @OneToOne @JoinColumn(name = "user_id") public AppUser user;
    public String name, email, phone, branch;
    public double cgpa;
    public int backlogs;
    @Column(length = 500) public String skills;

    public String getUsername() { return user == null ? null : user.username; }
}
