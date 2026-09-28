package com.example.tasknest.repository;



import com.example.tasknest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	java.util.Optional<User> findByEmailIgnoreCase(String email);
}