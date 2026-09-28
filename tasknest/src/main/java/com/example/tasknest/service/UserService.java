package com.example.tasknest.service;

import com.example.tasknest.dto.UserRequest;
import com.example.tasknest.entity.User;
import com.example.tasknest.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;

@Service
public class UserService {

    private static final int HASH_ITERATIONS = 120_000;
    private static final int HASH_BITS = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(UserRequest request) {
        String email = request.getEmail().trim();
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(hashPassword(request.getPassword()));

        return userRepository.save(user);
    }

    public User authenticate(String email, String password) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .filter(user -> user.getPasswordHash() != null
                        && verifyPassword(password, user.getPasswordHash()))
                .orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
 
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = deriveKey(password, salt, HASH_ITERATIONS);
        return "$pbkdf2$" + HASH_ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 5 || !"pbkdf2".equals(parts[1])) {
                return false;
            }

            int iterations = Integer.parseInt(parts[2]);
            byte[] salt = Base64.getDecoder().decode(parts[3]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[4]);
            byte[] actualHash = deriveKey(password, salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private byte[] deriveKey(String password, byte[] salt, int iterations) {
        char[] passwordChars = password.toCharArray();
        PBEKeySpec keySpec = new PBEKeySpec(passwordChars, salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to process password securely.", exception);
        } finally {
            keySpec.clearPassword();
            java.util.Arrays.fill(passwordChars, '\0');
        }
    }
}