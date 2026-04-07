package com.task.smartgrocerybe.repository;

import com.task.smartgrocerybe.model.User;
import com.task.smartgrocerybe.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("""
            SELECT u FROM User u
            WHERE u.role = :role
            AND (
                :search IS NULL
                OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            """)
    Page<User> findByRoleWithSearch(
            @Param("role") Role role,
            @Param("search") String search,
            Pageable pageable);
}
