package com.jpastudy.oneway.onetomany.test1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Team1Repository extends JpaRepository<Team1, Long> {
}
