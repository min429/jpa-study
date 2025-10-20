package com.jpa.study.twoway.manytoone.test2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Post2Repository extends JpaRepository<Post2, Long> {
}
