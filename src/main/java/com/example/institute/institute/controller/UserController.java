package com.example.institute.institute.controller;

import com.example.institute.institute.model.User;
import com.example.institute.institute.model.UserProfile;
import com.example.institute.institute.model.request.ChangePasswordRequest;
import com.example.institute.institute.model.request.ForgotPasswordRequest;
import com.example.institute.institute.model.request.LoginRequest;
import com.example.institute.institute.model.request.ResetPasswordRequest;
import com.example.institute.institute.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/auth")
public class UserController {
    private UserService userService;

//    @PostMapping("/users/register")
//    public User register(@RequestBody User user){
//        System.out.println("Calling createUser ==>");
//        return userService.createUser(user);
//    }

//    @PostMapping(value = "/users/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public User register(@RequestPart("user") User user, @RequestPart(value = "image",
//            required = false) MultipartFile image
//    ) {
//        System.out.println("Calling createUser ==>");
//        return userService.createUser(user, image);
//    }

    @PostMapping(
            value = "/users/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public User register(
            @RequestParam String username,
            @RequestParam String emailAddress,
            @RequestParam String password,
            @RequestParam(required = false) String role,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String mobileNumber,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {

        System.out.println("Calling createUser ==>");

        User user = new User();

        user.setUsername(username);
        user.setEmailAddress(emailAddress);
        user.setPassword(password);

        // Default role
        if (role == null || role.isBlank()) {
            role = "STUDENT";
        }

        user.setRole(role);

        UserProfile profile = new UserProfile();

        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setMobileNumber(mobileNumber);

        user.setUserProfile(profile);

        return userService.createUser(user, image);
    }



    @PostMapping("/users/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        System.out.println("Calling loginUser ==>");
        return userService.loginUser(loginRequest);
    }

    @GetMapping("/users/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {
        return userService.verifyUser(token);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deactivated successfully");
    }

    @PutMapping("/users/{id}/reactivate")
    public ResponseEntity<String> reactivateUser(@PathVariable Long id) {
        userService.reactivateUser(id);
        return ResponseEntity.ok("User reactivated successfully");
    }

    @PutMapping("/users/changePassword")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request) {

        userService.changePassword(request);

        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/users/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        userService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(
                "Password reset email sent successfully"
        );
    }

    @PostMapping("/users/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        userService.resetPassword(request);

        return ResponseEntity.ok(
                "Password reset successfully"
        );
    }

    @PostMapping(value = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public User createUser(@RequestPart("user") User userObject,
                           @RequestPart(value = "image", required = false) MultipartFile image) {
        return userService.createUser(userObject, image);
    }
}