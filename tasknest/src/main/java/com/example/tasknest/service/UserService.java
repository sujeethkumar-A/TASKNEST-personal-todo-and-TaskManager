package com.example.tasknest.service;

import com.example.tasknest.dto.UserRequest;
import com.example.tasknest.entity.User;
import com.example.tasknest.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(UserRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}