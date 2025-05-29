package com.webapp.controller;

import com.webapp.entity.Role;
import com.webapp.entity.User;
import com.webapp.respository.RoleRepository;
import com.webapp.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Controller
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // create register.html
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, @RequestParam String role, Model model) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName(role).orElseThrow();
        user.setRoles(Collections.singleton(userRole));
        userRepository.save(user);
        model.addAttribute("message", "Registration successful!");
        return "login_student"; // or redirect as needed
    }
}
