package com.example.PlacementCell.service;

import com.example.PlacementCell.entity.PC;
import com.example.PlacementCell.repository.PCRepository;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class PCDetailsService implements UserDetailsService {

    private final PCRepository pcRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        PC pc = pcRepo.findByCollegeEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new User(
                pc.getCollegeEmail(),
                pc.getPassword(),
                Collections.emptyList() // Add roles if needed later
        );
    }
}
