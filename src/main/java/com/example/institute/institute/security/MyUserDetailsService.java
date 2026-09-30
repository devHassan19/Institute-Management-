package com.example.institute.institute.security;

import com.example.institute.institute.model.User;
import com.example.institute.institute.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findByEmailAddress(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + email);
        }
        return new MyUserDetails(user);
    }

}
