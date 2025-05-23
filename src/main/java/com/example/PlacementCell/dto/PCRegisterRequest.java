package com.example.PlacementCell.dto;

import lombok.Data;

@Data
public class PCRegisterRequest {
    private String name;
    private String phone;
    private String collegeEmail;
    private String password;
    private Integer tpoId;
}
