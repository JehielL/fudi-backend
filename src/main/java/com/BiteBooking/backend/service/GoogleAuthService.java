package com.BiteBooking.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthService {

    @Value("${google.oauth.client-id}")
    private String clientId;

    public GoogleIdToken.Payload verifyToken(String idTokenString){
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new
                NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();
        try{
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null){
                return idToken.getPayload();
            }
        } catch (GeneralSecurityException | IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
