package com.jpastudy.transaction;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final DataSource dataSource;

    @Transactional

    public void save(Member member) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        System.out.println("Member save() connection: " + connection);
        memberRepository.save(member);
    }
}
