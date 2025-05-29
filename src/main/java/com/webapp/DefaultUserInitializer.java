package com.webapp;

import com.webapp.entity.Role;
import com.webapp.entity.User;
import com.webapp.entity.Student;
import com.webapp.respository.RoleRepository;
import com.webapp.respository.UserRepository;
import com.webapp.respository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
public class DefaultUserInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createDefaultUsers(
            UserRepository userRepository,
            RoleRepository roleRepository,
            StudentRepository studentRepository
    ) {
        return args -> {
            // Ensure roles exist
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ROLE_ADMIN");
                        return roleRepository.save(role);
                    });

            Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ROLE_STUDENT");
                        return roleRepository.save(role);
                    });

            // Default admin
            String adminUsername = "admin";
            String adminPassword = "admin123";
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRoles(Collections.singleton(adminRole));
                admin.setEnabled(true);
                userRepository.save(admin);
            }

            // Default student
            String enrollmentNo = "25CSE0001"; // used as username and password

            if (!studentRepository.existsByEnrollmentNo(enrollmentNo)) {
                Student student = new Student();
                student.setName("Test Student");
                student.setDepartment("Computer Science and Engineering");
                student.setEmail("student@example.com");
                student.setEnrollmentNo(enrollmentNo);
                studentRepository.save(student);
            }

            if (userRepository.findByUsername(enrollmentNo).isEmpty()) {
                // Create User entity with username = enrollmentNo
                User studentUser = new User();
                studentUser.setUsername(enrollmentNo);  // username = enrollmentNo
                studentUser.setPassword(passwordEncoder.encode(enrollmentNo)); // password = enrollmentNo
                studentUser.setRoles(Collections.singleton(studentRole));
                studentUser.setEnabled(true);
                userRepository.save(studentUser);
            }
        };
    }
}
