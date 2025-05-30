package com.webapp.entity;

import jakarta.persistence.*;

@Entity
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., "Semester 1", "Semester 2"
    private String year; // e.g., "2024-25"

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
}
