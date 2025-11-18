package com.devhunter.bounty.repository;

import com.devhunter.bounty.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  User findByLogin(String login);
}

