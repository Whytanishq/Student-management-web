package com.webapp.controller;

import com.webapp.entity.User;
import com.webapp.respository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("users", userRepo.findAll());
        return "index";
    }

    @GetMapping("/add")
    public String addUserForm(User user) {
        return "add";
    }

    @PostMapping("/add")
    public String addUser(User user) {
        userRepo.save(user);
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userRepo.findById(id).orElseThrow());
        return "edit";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable("id") Long id, User user) {
        user.setId(id);
        userRepo.save(user);
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userRepo.deleteById(id);
        return "redirect:/";
    }
}
