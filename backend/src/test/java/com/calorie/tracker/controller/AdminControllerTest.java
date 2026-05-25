package com.calorie.tracker.controller;

import com.calorie.tracker.model.Role;
import com.calorie.tracker.model.User;
import com.calorie.tracker.repository.UserRepository;
import com.calorie.tracker.security.CustomUserDetails;
import com.calorie.tracker.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private String adminToken;
    private String userToken;

    @BeforeEach
    public void setUp() {
        // Clear potential conflicts
        userRepository.findByEmail("admin_test@example.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("user_test@example.com").ifPresent(userRepository::delete);

        // Create Admin
        User admin = User.builder()
                .email("admin_test@example.com")
                .name("Admin Test")
                .passwordHash("hashedpassword")
                .role(Role.ROLE_ADMIN)
                .build();
        userRepository.save(admin);
        adminToken = jwtUtils.generateTokenFromUsername("admin_test@example.com");

        // Create Regular User
        User user = User.builder()
                .email("user_test@example.com")
                .name("User Test")
                .passwordHash("hashedpassword")
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);
        userToken = jwtUtils.generateTokenFromUsername("user_test@example.com");
    }

    @Test
    public void testAdminAccessToAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testUserAccessToAdminEndpointsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAnonymousAccessToAdminEndpointsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCustomUserDetailsAuthorities_Admin() {
        User adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .name("Admin")
                .role(Role.ROLE_ADMIN)
                .passwordHash("hashed")
                .build();

        CustomUserDetails userDetails = CustomUserDetails.build(adminUser);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    public void testCustomUserDetailsAuthorities_User() {
        User regularUser = User.builder()
                .id(2L)
                .email("user@example.com")
                .name("User")
                .role(Role.ROLE_USER)
                .passwordHash("hashed")
                .build();

        CustomUserDetails userDetails = CustomUserDetails.build(regularUser);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }
}
