package com.BiteBooking.backend.service;

import com.BiteBooking.backend.dto.auth.AuthResponse;
import com.BiteBooking.backend.dto.auth.GoogleAuthRequest;
import com.BiteBooking.backend.model.AuthProvider;
import com.BiteBooking.backend.model.Role;
import com.BiteBooking.backend.model.User;
import com.BiteBooking.backend.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final GoogleAuthService googleAuthService;

    public AuthResponse loginWithGoogle(GoogleAuthRequest request){

        GoogleIdToken.Payload payload = googleAuthService.verifyToken(request.idToken());

        if (payload == null){
            throw new IllegalArgumentException("Token de Google invalido o expirado");
        }

        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        String pictureUrl = (String) payload.get("picture");

        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;

        if(existingUser.isPresent()){

            user = existingUser.get();
            if (!user.isGoogleUser()){
                user.addAuthProvider(AuthProvider.GOOGLE);
                user.setGoogleProviderId(googleId);

                if (user.getImgUser() == null || user.getImgUser().isEmpty()){
                    user.setImgUser(pictureUrl);
                }
                userRepository.save(user);
            }
        } else {

            user = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .imgUser(pictureUrl)
                    .role(Role.USER)
                    .authProviders(java.util.Set.of(AuthProvider.GOOGLE))
                    .googleProviderId(googleId)
                    .birthdayDate(LocalDate.now())
                    .build();
            userRepository.save(user);
        }

        String jwtToken = jwtTokenService.generateToken(user);

        return new AuthResponse(
                jwtToken,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getImgUser()
        );
    }

}
