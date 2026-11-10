package com.example.institute.institute.controller;

import com.example.institute.institute.model.User;
import com.example.institute.institute.model.UserProfile;
import com.example.institute.institute.service.EnrollmentService;
import com.example.institute.institute.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MeController {
    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        User u = EnrollmentService.getCurrentLogginUser();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("email", u.getEmailAddress());
        m.put("role", u.getRole());
        UserProfile p = u.getUserProfile();
        if (p != null) {
            m.put("firstName", p.getFirstName());
            m.put("lastName", p.getLastName());
            m.put("mobileNumber", p.getMobileNumber());
            m.put("image", p.getImage());
        }
        return m;
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> update(@RequestParam String firstName,
                                      @RequestParam String lastName,
                                      @RequestParam String mobileNumber,
                                      @RequestPart(value = "image", required = false) MultipartFile image) {
        userService.updateProfile(firstName, lastName, mobileNumber, image);
        return me();
    }
}

