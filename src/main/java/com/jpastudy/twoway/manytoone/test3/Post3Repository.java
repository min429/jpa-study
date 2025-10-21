package com.jpastudy.twoway.manytoone.test3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Post3Repository extends JpaRepository<Post3, Long> {
}
