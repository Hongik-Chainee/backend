// src/main/java/com/chaineeproject/chainee/repository/UserRepository.java
package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"positions", "mainProject"})
    @Query("""
        select u
        from User u
        where
             lower(u.email) like concat('%', lower(:q), '%')
          or lower(coalesce(u.provider, '')) like concat('%', lower(:q), '%')
          or lower(coalesce(u.providerId, '')) like concat('%', lower(:q), '%')
        """)
    Page<User> searchTalents(@Param("q") String q, Pageable pageable);
}
