package com.example.PlacementCell.service;

import com.example.PlacementCell.entity.PC;
import com.example.PlacementCell.entity.Tpo;
import com.example.PlacementCell.security.*;
import com.example.PlacementCell.repository.PCRepository;
import com.example.PlacementCell.repository.TpoRepository;
import com.example.PlacementCell.dto.PCLoginRequest;
import com.example.PlacementCell.dto.PCRegisterRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PCAuthService {

    private final PCRepository pcRepo;
    private final TpoRepository tpoRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; // Make sure JwtUtil is a @Component and injectable

    public String registerPC(PCRegisterRequest req) {
        Tpo tpo = tpoRepo.findById(req.getTpoId())
                .orElseThrow(() -> new RuntimeException("TPO not found"));

        PC pc = new PC();
        pc.setName(req.getName());
        pc.setPhone(req.getPhone());
        pc.setCollegeEmail(req.getCollegeEmail());
        pc.setPassword(passwordEncoder.encode(req.getPassword()));
        pc.setTpo(tpo);

        pcRepo.save(pc);
        return "Placement Coordinator registered successfully";
    }

    public String login(PCLoginRequest req) {
        PC pc = pcRepo.findByCollegeEmail(req.getCollegeEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getPassword(), pc.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(pc.getCollegeEmail());
    }
}
