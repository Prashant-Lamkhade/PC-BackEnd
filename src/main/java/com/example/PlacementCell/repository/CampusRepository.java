package com.example.PlacementCell.repository;

import com.example.PlacementCell.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {
    Campus findByCampusName(String campusName);
}