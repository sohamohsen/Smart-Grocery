package com.task.smartgrocerybe.repository;

import com.task.smartgrocerybe.model.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    Page<Favorite> findByUserId(Integer userId, Pageable pageable);

    boolean existsByUserIdAndProductId(Integer userId, Integer productId);

    Optional<Favorite> findByUserIdAndProductId(Integer userId, Integer productId);

    void deleteByUserIdAndProductId(Integer userId, Integer productId);
}