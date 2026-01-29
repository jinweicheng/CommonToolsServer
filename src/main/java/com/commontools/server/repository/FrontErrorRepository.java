package com.commontools.server.repository;

import com.commontools.server.model.FrontError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FrontErrorRepository extends JpaRepository<FrontError, Long> {
}

