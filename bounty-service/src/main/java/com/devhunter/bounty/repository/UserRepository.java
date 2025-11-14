package com.devhunter.bounty.repository;

import com.devhunter.bounty.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

