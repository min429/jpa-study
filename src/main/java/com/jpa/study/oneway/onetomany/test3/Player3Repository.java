package com.jpa.study.oneway.onetomany.test3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jpa.study.oneway.onetomany.test2.Player2;

@Repository
public interface Player3Repository extends JpaRepository<Player3, Long> {
}
