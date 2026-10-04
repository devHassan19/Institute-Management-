package com.example.institute.institute.service;

import com.example.institute.institute.exception.InformationExistException;
import com.example.institute.institute.model.Student;
import com.example.institute.institute.model.User;
import com.example.institute.institute.model.VerificationToken;
import com.example.institute.institute.model.request.LoginRequest;
import com.example.institute.institute.model.response.LoginResponse;
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

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private MyUserDetails myUserDetails;

    @Autowired
    public UserService(UserRepository userRepository,
                       @Lazy PasswordEncoder passwordEncoder,
                       JWTUtils jwtUtils,
                       @Lazy AuthenticationManager authenticationManager,
                       @Lazy MyUserDetails myUserDetails,
                       VerificationTokenRepository tokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.myUserDetails = myUserDetails;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public User createUser(User userObject) {
        System.out.println("Calling createUser ==>");
        if (!userRepository.existsByEmailAddress(userObject.getEmailAddress())) {
            userObject.setPassword(passwordEncoder.encode(userObject.getPassword()));
            userObject.setEnabled(false);

            if ("STUDENT".equals(userObject.getRole())) {
                Student student = new Student();
                student.setName(userObject.getUsername());
                student.setUser(userObject);

                userObject.setStudent(student);
            }

            User saved = userRepository.save(userObject);
            VerificationToken token = new VerificationToken(saved);

            tokenRepository.save(token);
            emailService.sendVerificationEmail(saved.getEmailAddress(), token.getToken());
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
}