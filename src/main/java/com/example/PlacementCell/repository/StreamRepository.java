package com.example.PlacementCell.repository;

import com.example.PlacementCell.entity.Stream;
import com.example.PlacementCell.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StreamRepository extends JpaRepository<Stream, String> {
    List<Stream> findByCampus(Campus campus);

    List<Stream> findByStreamName(String streamName);
}