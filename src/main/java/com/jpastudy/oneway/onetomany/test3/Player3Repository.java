package com.jpastudy.oneway.onetomany.test3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Player3Repository extends JpaRepository<Player3, Long> {
}
