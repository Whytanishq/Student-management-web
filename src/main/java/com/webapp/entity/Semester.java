package com.webapp.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // e.g., "Semester 1"
    private String year;  // e.g., "2023-24"

    // Getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getYear() {
        return year;
    }
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * Extracts the semester number from the name.
     * For example, "Semester 1" returns 1.
     */
    public int getNumber() {
        if (name == null) return 0;
        try {
            return Integer.parseInt(name.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0; // or throw an exception if preferred
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Semester semester = (Semester) o;
        return Objects.equals(id, semester.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
