package com.jpa.study.oneway.onetomany.test3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Team3Repository extends JpaRepository<Team3, Long> {
}
