package com.placement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class Company {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String name, location;
    @Column(length = 1000) public String description;
}
