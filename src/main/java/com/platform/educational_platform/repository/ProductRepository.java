package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByOrderIndexAsc();

    @Query("SELECT p.id FROM Product p JOIN p.modules m JOIN m.quizzes q WHERE q.id = :quizId")
    Optional<Long> findProductIdByQuizId(@Param("quizId") Long quizId);
}