package com.cleancity.security;

import com.cleancity.entity.User;
import com.cleancity.entity.UserRole;
import com.cleancity.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Custom User Details Service for Spring Security
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UUID userId = UUID.fromString(username);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + username));

            return new org.springframework.security.core.userdetails.User(
                    user.getId().toString(),
                    user.getPasswordHash(),
                    user.getIsActive(),
                    true,
                    true,
                    true,
                    getAuthorities(user.getRole())
            );
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid user ID format: " + username);
        }
    }

    /**
     * Get authorities based on user role
     */
    private Collection<? extends GrantedAuthority> getAuthorities(UserRole role) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role-based authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        
        // Add specific permissions based on role
        switch (role) {
            case ADMIN:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_READ_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_WRITE_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_DELETE_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_USERS"));
                break;
            case COLLECTOR:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_READ_PICKUPS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_UPDATE_PICKUPS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_READ_ZONE"));
                break;
            case RESIDENT:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_CREATE_PICKUP"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_READ_OWN"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_CREATE_REPORT"));
                break;
        }
        
        return authorities;
    }
}
