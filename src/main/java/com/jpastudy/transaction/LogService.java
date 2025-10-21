package com.jpastudy.transaction;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final DataSource dataSource;

    @Transactional
    public void save(Log log) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        System.out.println("Log save() connection: " + connection);
        logRepository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save_new(Log log) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        System.out.println("Log save_new() connection: " + connection);
        logRepository.save(log);
    }

    @Transactional
    public void save_fail(Log log) {
        logRepository.save(log);
        throw new RuntimeException();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save_new_fail(Log log) {
        logRepository.save(log);
        throw new RuntimeException();
    }
}
