package com.example.PlacementCell.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tpo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String idNumber;
    private String designation;
    private String campus;
    private String phone;

    @Column(unique = true)
    private String collegeEmail;

    private String password;

    private boolean isApproved = false;
}