//package com.jpastudy.oneway.onetomany.test1;
//
//import jakarta.persistence.EntityManager;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.support.TransactionTemplate;
//
//@RequiredArgsConstructor
//@Service
//public class PT1Runner implements CommandLineRunner {
//
//    private final Team1Repository teamRepository;
//    private final Player1Repository playerRepository;
//    private final TransactionTemplate transactionTemplate;
//    private final EntityManager em;
//
//    // @Override
//    // @Transactional
//    // public void run(String... args) throws Exception {
//    // 	Team1 team = new Team1("t1");
//    // 	Player1 faker = new Player1("faker");
//    // 	Player1 keria = new Player1("keria");
//    // 	team.add(faker);
//    // 	team.add(keria);
//    //
//    // 	team1Repository.save(team);
//    //
//    // 	// commit -> 변경감지 o -> 자동 flush -> update 쿼리 발생
//    // }
//
//    @Override
//    public void run(String... args) throws Exception {
//        transactionTemplate.executeWithoutResult(status -> {
//            Team1 team = new Team1("t1");
//            Player1 faker = new Player1("faker");
//            Player1 keria = new Player1("keria");
//            team.add(faker);
//            team.add(keria);
//
//            teamRepository.save(team);
//        });
//
//        transactionTemplate.executeWithoutResult(status -> {
//            Player1 faker = playerRepository.findByName("faker");
//            faker.setName("god");
//
//            em.flush();
//
//            status.setRollbackOnly(); // rollback -> 변경감지 x -> 수동 flush (자동 안됨) -> update 쿼리 발생x
//        });
//    }
//}
