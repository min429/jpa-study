package com.jpastudy.oneway.onetomany.test1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Player1Repository extends JpaRepository<Player1, Long> {
}
