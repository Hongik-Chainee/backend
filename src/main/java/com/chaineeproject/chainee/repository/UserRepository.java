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
    Optional<User> findByDid(String did);

    // N+1 완화: positions, mainProject만 프리패치
    @EntityGraph(attributePaths = {"positions", "mainProject"})
    Page<User> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"positions", "mainProject"})
    @Query("""
        select distinct u
        from User u
        left join u.positions p
        left join u.myProjects pr
        where
               lower(u.name)       like concat('%', lower(:q), '%')
            or lower(u.inLocation) like concat('%', lower(:q), '%')
            or lower(p)            like concat('%', lower(:q), '%')
            or (
                   pr.isPublic = true
               and pr.requiredSkills is not null
               and pr.requiredSkills like concat('%', :q, '%')
            )
        """)
    Page<User> searchTalents(@Param("q") String q, Pageable pageable);
}
