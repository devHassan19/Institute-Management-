package com.example.institute.institute.controller;

import com.example.institute.institute.model.User;
import com.example.institute.institute.model.request.LoginRequest;
import com.example.institute.institute.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/auth")
public class UserController {
    private UserService userService;

    @PostMapping("/users/register")
    public User register(@RequestBody User user){
        System.out.println("Calling createUser ==>");
        return userService.createUser(user);
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){
        System.out.println("Calling loginUser ==>");
        return userService.loginUser(loginRequest);
    }

    // الجديد: رابط التفعيل
    @GetMapping("/users/verify")
    public ResponseEntity<String> verify(@RequestParam String token){
        return userService.verifyUser(token);
    }
}