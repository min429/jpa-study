package com.jpastudy.twoway.manytoone.test1;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Comment1Repository extends JpaRepository<Comment1, Long> {
    List<Comment1> findAllByPostIdIn(List<Long> postIds);
}
