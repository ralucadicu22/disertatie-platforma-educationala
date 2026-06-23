package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findAllByOrderByOrderIndexAsc();
    List<Module> findByProductIdOrderByOrderIndexAsc(Long productId);

}