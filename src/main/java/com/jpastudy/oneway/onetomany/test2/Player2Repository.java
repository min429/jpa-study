package com.jpastudy.oneway.onetomany.test2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Player2Repository extends JpaRepository<Player2, Long> {
}
