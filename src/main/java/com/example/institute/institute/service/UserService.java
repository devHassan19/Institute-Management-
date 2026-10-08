package com.example.institute.institute.service;

import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.exception.InformationExistException;
import com.example.institute.institute.model.*;
import com.example.institute.institute.model.request.ChangePasswordRequest;
import com.example.institute.institute.model.request.LoginRequest;
import com.example.institute.institute.model.request.ResetPasswordRequest;
import com.example.institute.institute.model.response.LoginResponse;
import com.example.institute.institute.repository.PasswordResetTokenRepository;
import com.example.institute.institute.repository.UserRepository;
import com.example.institute.institute.repository.VerificationTokenRepository;
import com.example.institute.institute.security.JWTUtils;
import com.example.institute.institute.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private MyUserDetails myUserDetails;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private void checkAdmin() {
        User currentUser = StudentService.getCurrentLogginUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only admin can perform this action");
        }
    }

    private final String UPLOAD_DIR = "uploads/profiles/";

    private String saveImage(MultipartFile image) {

        try {

            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = image.getOriginalFilename();

            String extension = "";

            if (originalFileName != null &&
                    originalFileName.contains(".")) {

                extension = originalFileName.substring(
                        originalFileName.lastIndexOf(".")
                );
            }

            String fileName = UUID.randomUUID() + extension;

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(
                    image.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println("Image saved: " + filePath);

            return UPLOAD_DIR + fileName;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not save image",
                    e
            );
        }
    }

    @Autowired
    public UserService(UserRepository userRepository,
                       @Lazy PasswordEncoder passwordEncoder,
                       JWTUtils jwtUtils,
                       @Lazy AuthenticationManager authenticationManager,
                       @Lazy MyUserDetails myUserDetails,
                       VerificationTokenRepository tokenRepository,
                       EmailService emailService,
                       PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.myUserDetails = myUserDetails;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Transactional
    public User createUser(User userObject, MultipartFile image) {

        System.out.println("Calling createUser ==>");

        if (!userRepository.existsByEmailAddress(userObject.getEmailAddress())) {

            userObject.setPassword(
                    passwordEncoder.encode(userObject.getPassword())
            );

            userObject.setEnabled(false);
            userObject.setUserStatus(UserStatus.ACTIVE);

            if (image != null && !image.isEmpty()) {
                String imagePath = saveImage(image);

                if (userObject.getUserProfile() != null) {
                    userObject.getUserProfile().setImage(imagePath);
                }
            }

            if ("STUDENT".equals(userObject.getRole())) {
                Student student = new Student();
                student.setName(userObject.getUsername());
                student.setUser(userObject);

                userObject.setStudent(student);
            }

            User saved = userRepository.save(userObject);

            VerificationToken token = new VerificationToken(saved);

            tokenRepository.save(token);

            emailService.sendVerificationEmail(
                    saved.getEmailAddress(),
                    token.getToken()
            );

            return saved;

        } else {
            throw new InformationExistException("Already exists");
        }
    }

    public User findByEmailAddress(String emailAddress) {
        return userRepository.findByEmailAddress(emailAddress);
    }

    public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.
                    authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            myUserDetails = (MyUserDetails) authentication.getPrincipal();
            final String JWT = jwtUtils.generateJwtToken(myUserDetails);
            return ResponseEntity.ok(new LoginResponse(JWT));
        } catch (DisabledException e) {
            User user = userRepository.findByEmailAddress(loginRequest.getEmail());
            if (user != null && user.getUserStatus() == UserStatus.INACTIVE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new LoginResponse("Your account has been deactivated"));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new LoginResponse("Please verify your email first"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Email or password is incorrect"));
        }
    }

    @Transactional
    public ResponseEntity<String> verifyUser(String token) {
        VerificationToken vt = tokenRepository.findByToken(token).orElse(null);

        if (vt == null) {
            return ResponseEntity.badRequest().body("Invalid token");
        }
        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired");
        }

        User user = vt.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(vt);

        return ResponseEntity.ok("Account verified successfully");
    }

    @Transactional
    public void deleteUser(Long id) {
        checkAdmin();
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setUserStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void reactivateUser(Long id) {
        checkAdmin();
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        User currentUser = StudentService.getCurrentLogginUser();

        if (!passwordEncoder.matches(
                request.getOldPassword(),
                currentUser.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Old password is incorrect"
            );
        }

        currentUser.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(currentUser);
    }

    @Transactional
    public void forgotPassword(String email) {

        User user = userRepository.findByEmailAddress(email);

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        PasswordResetToken resetToken = new PasswordResetToken(user);

        passwordResetTokenRepository.save(resetToken);

        System.out.println("PASSWORD RESET TOKEN: " + resetToken.getToken());

        emailService.sendPasswordResetEmail(
                user.getEmailAddress(),
                resetToken.getToken()
        );
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByToken(request.getToken())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid reset token"
                                ));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {

            passwordResetTokenRepository.delete(resetToken);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reset token expired"
            );
        }

        User user = resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }
}