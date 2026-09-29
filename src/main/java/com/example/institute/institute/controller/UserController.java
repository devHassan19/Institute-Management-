package com.example.institute.institute.controller;

import com.example.institute.institute.model.User;
import com.example.institute.institute.model.request.LoginRequest;
import com.example.institute.institute.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        System.out.println("Calling createUser ==>");
        return userService.loginUser(loginRequest);

    }

}
