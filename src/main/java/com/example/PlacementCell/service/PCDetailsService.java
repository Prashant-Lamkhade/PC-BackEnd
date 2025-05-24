package com.example.PlacementCell.service;

import com.example.PlacementCell.entity.PC;
import com.example.PlacementCell.repository.PCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class PCDetailsService implements UserDetailsService {

    @Autowired
    private PCRepository pcRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        PC pc = pcRepository.findByCollegeEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("PC not found with email: " + email));

        return new User(pc.getCollegeEmail(), pc.getPassword(), Collections.emptyList());
    }
}
