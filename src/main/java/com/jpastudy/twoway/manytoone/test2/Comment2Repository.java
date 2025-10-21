package com.jpastudy.twoway.manytoone.test2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Comment2Repository extends JpaRepository<Comment2, Long> {
}
