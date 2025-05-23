package com.example.PlacementCell.repository;

import com.example.PlacementCell.entity.PC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PCRepository extends JpaRepository<PC, Long> {
    
    Optional<PC> findByCollegeEmail(String collegeEmail);
    
    Optional<PC> findByPhone(String phone);
    
    boolean existsByCollegeEmail(String collegeEmail);
    
    boolean existsByPhone(String phone);
}