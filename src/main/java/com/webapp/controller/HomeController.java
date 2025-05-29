package com.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/login_admin")
    public String loginAdmin() {
        return "login_admin";
    }

    @GetMapping("/login_student")
    public String loginStudent() {
        return "login_student";
    }

}
