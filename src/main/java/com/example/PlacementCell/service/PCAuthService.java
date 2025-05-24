package com.example.PlacementCell.service;

import com.example.PlacementCell.entity.PC;
import com.example.PlacementCell.entity.Tpo;
import com.example.PlacementCell.security.*;
import com.example.PlacementCell.repository.PCRepository;
import com.example.PlacementCell.repository.TpoRepository;
import com.example.PlacementCell.dto.LoginRequest;
// import com.example.PlacementCell.dto.LoginRequest.JwtResponse;
// import com.example.PlacementCell.dto.PCLoginRequest;
import com.example.PlacementCell.dto.LoginResponse;
import com.example.PlacementCell.dto.PCRegisterRequest;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public LoginResponse loginPc(LoginRequest request) {
        Optional<PC> optionalPc = pcRepo.findByCollegeEmail(request.getCollegeEmail());

        if (optionalPc.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        PC pc = optionalPc.get();

        if (!passwordEncoder.matches(request.getPassword(), pc.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtUtil.generateToken(pc.getCollegeEmail());

        return new LoginResponse(token, "Login successful");
    }
}
