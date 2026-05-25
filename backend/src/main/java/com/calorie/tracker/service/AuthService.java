package com.calorie.tracker.service;

import com.calorie.tracker.dto.AuthRequest;
import com.calorie.tracker.dto.AuthResponse;
import com.calorie.tracker.dto.RegisterRequest;
import com.calorie.tracker.model.User;
import com.calorie.tracker.repository.UserRepository;
import com.calorie.tracker.security.CustomUserDetails;
import com.calorie.tracker.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.calorie.tracker.model.AuthProvider;
import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${google.clientId}")
    private String googleClientId;

    public AuthResponse authenticateUser(AuthRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return new AuthResponse(jwt, "dummy_refresh_token", 86400);
    }

    public User registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        int defaultCalories = 2000;
        if (signUpRequest.getGoal() != null) {
            switch (signUpRequest.getGoal()) {
                case FAT_LOSS:
                    defaultCalories = 1800;
                    break;
                case MUSCLE_GAIN:
                    defaultCalories = 2500;
                    break;
                case MAINTENANCE:
                default:
                    defaultCalories = 2000;
                    break;
            }
        }

        User user = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .passwordHash(encoder.encode(signUpRequest.getPassword()))
                .height(signUpRequest.getHeight())
                .currentWeight(signUpRequest.getWeight())
                .goal(signUpRequest.getGoal())
                .dailyCalorieGoal(defaultCalories)
                .build();

        return userRepository.save(user);
    }

    public AuthResponse authenticateWithGoogle(String idTokenString) {
        try {
            System.out.println("DEBUG Auth: idTokenString = " + idTokenString);
            System.out.println("DEBUG Auth: googleClientId = " + googleClientId);
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String subjectId = payload.getSubject();

                Optional<User> optionalUser = userRepository.findByEmail(email);
                User user;
                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                    if (user.getAuthProvider() == AuthProvider.LOCAL) {
                        user.setAuthProvider(AuthProvider.GOOGLE);
                        user.setGoogleId(subjectId);
                        userRepository.save(user);
                    }
                } else {
                    user = User.builder()
                            .name(name != null ? name : "User")
                            .email(email)
                            .passwordHash(encoder.encode(java.util.UUID.randomUUID().toString()))
                            .authProvider(AuthProvider.GOOGLE)
                            .googleId(subjectId)
                            .dailyCalorieGoal(2000)
                            .build();
                    user = userRepository.save(user);
                }

                CustomUserDetails userDetails = CustomUserDetails.build(user);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                String jwt = jwtUtils.generateJwtToken(authentication);
                return new AuthResponse(jwt, "dummy_refresh_token", 86400);
            } else {
                throw new RuntimeException("Invalid Google ID token.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }
}
