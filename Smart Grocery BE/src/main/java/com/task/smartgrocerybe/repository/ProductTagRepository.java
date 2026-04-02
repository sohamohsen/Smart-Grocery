package com.task.smartgrocerybe.repository;

import com.task.smartgrocerybe.model.Product;
import com.task.smartgrocerybe.model.ProductTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ProductTagRepository extends JpaRepository<ProductTags, Integer> {

    Collection<ProductTags> findByProductId(Integer id);
}
