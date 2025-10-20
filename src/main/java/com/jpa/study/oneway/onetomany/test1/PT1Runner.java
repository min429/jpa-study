package com.jpa.study.oneway.onetomany.test1;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PT1Runner implements CommandLineRunner {

    private final Team1Repository team1Repository;

    // @Override
    // @Transactional
    // public void run(String... args) throws Exception {
    // 	Team1 team = new Team1("t1");
    // 	Player1 faker = new Player1("faker");
    // 	Player1 keria = new Player1("keria");
    // 	team.add(faker);
    // 	team.add(keria);
    //
    // 	team1Repository.save(team);
    //
    // 	// commit -> flush() o, 변경감지 o -> update 쿼리 발생
    // }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Team1 team = new Team1("t1");
        Player1 faker = new Player1("faker");
        Player1 keria = new Player1("keria");
        team.add(faker);
        team.add(keria);

        team1Repository.save(team);

        TransactionAspectSupport.currentTransactionStatus()
            .setRollbackOnly(); // rollback -> flush()는 o, 변경감지는 x -> update 쿼리 발생x
    }
}
