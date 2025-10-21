package com.jpastudy.oneway.onetomany.test2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Team2Repository extends JpaRepository<Team2, Long> {
}
