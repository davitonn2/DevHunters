package com.devhunter.bounty.repository;

import com.devhunter.bounty.model.entity.User;
import com.devhunter.bounty.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  User findByLogin(String login);

  List<User> findAllByRole(UserRole role);
}

