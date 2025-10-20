package com.jpa.study.oneway.onetomany;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.jpa.study.oneway.onetomany.test1.Player1;
import com.jpa.study.oneway.onetomany.test1.Team1;
import com.jpa.study.oneway.onetomany.test1.Team1Repository;
import com.jpa.study.oneway.onetomany.test2.Player2;
import com.jpa.study.oneway.onetomany.test2.Team2;
import com.jpa.study.oneway.onetomany.test2.Team2Repository;
import com.jpa.study.oneway.onetomany.test3.Player3;
import com.jpa.study.oneway.onetomany.test3.Team3;
import com.jpa.study.oneway.onetomany.test3.Team3Repository;

// 테스트 환경에서는 @Transactional를 쓰면 개별 테스트가 끝날 때 트랜잭션이 롤백되고 영속성 컨텍스트가 사라짐
// flush란 영속성 컨텍스트의 내용을 DB에 반영하는 것.(커밋은 아님) 그 과정에서 쿼리가 출력됨
// 기본적으로 flush는 트랜잭션이 끝날 때(트랜잭션이 붙은 (가장 바깥)메서드가 종료될 때) 발생함
// flush가 호출되면 영속성 컨텍스트가 DB에 반영되고 데이터 스냅샷이 교체됨
// 변경감지는 DB에서 가져온 객체의 스냅샷과 영속성 컨텍스트의 객체 상태를 비교하는 것
// 따라서 DB에 존재하지 않는 엔티티는 변경감지가 작동하지 않음
// 또한, DB에 데이터가 존재하더라도 트랜잭션이 rollback 되면 변경감지가 작동하지 않음
// 결론적으로 변경감지 결과를 보고 싶으면 트랜잭션이 종료되기 전에 flush를 수동으로 해줘야함
@Transactional // 테스트 종료 시 롤백
@SpringBootTest
public class OneToManyTest {

    @Autowired
    private Team1Repository team1Repository;

    @Autowired
    private Team2Repository team2Repository;

    @Autowired
    private Team3Repository team3Repository;

    /**
     * [test1]
     * nullable = false, updatable = false 적용x
     */
    @Test
    @DisplayName("insert 시 update 쿼리가 발생한다.")
    void test1_1() {
        Team1 t1 = new Team1("T1");
        Player1 faker = new Player1("페이커");
        Player1 keria = new Player1("케리아");
        t1.add(faker);
        t1.add(keria);

        team1Repository.save(t1);

        faker.setName("faker");

        team1Repository.flush();

        // 1. t1이 저장된다.
        // 2. faker, keria가 저장된다.
        // 3. faker, keria의 team_id가 업데이트 된다.

        // GenerationType.IDENTITY 전략을 사용중일 때, save 메서드 호출 시 insert 쿼리가 발생한다.
        //
    }

    @Test
    @DisplayName("Player만 수정해도 DB에 반영된다.")
    void test1_2() {
        Team1 t1 = new Team1("T1");
        Player1 player = new Player1("페이커");
        t1.add(player);

        team1Repository.save(t1); // insert 쿼리 쓰기지연(대기) 상태

        player.setName("케리아");

        team1Repository.flush(); // insert 쿼리를 호출하고 바로 변경감지를 통해 update 쿼리도 호출함
        // 변경감지는 insert와 별개로 update 쿼리로만 이루어짐
        // flush()가 실행될 때 먼저 쓰기지연(대기) 상태인 SQL이 실행되고 바로 변경감지를 수행한 다음 변경된 엔티티가 있으면 곧바로 update 쿼리를 호출함

        // 일대다 연관관계에서는 연관관계의 주인(Team)이 아니더라도 DB반영이 가능하다.
    }

    @Test
    @DisplayName("변경감지로 인해 update 쿼리가 2번만 발생한다.")
    void test1_3() {
        Team1 t1 = new Team1("T1");
        Team1 drx = new Team1("DRX");
        Player1 keria = new Player1("케리아");
        Player1 deft = new Player1("데프트");
        drx.add(keria);
        drx.add(deft);

        team1Repository.save(drx);
        team1Repository.save(t1);

        drx.remove(keria);
        t1.add(keria);

        team1Repository.flush();

        // 변경감지로 인해 최종적으로 update 쿼리가 2번만 호출된다.
    }

    /**
     * [test2]
     * @JoinColumn에 (nullable = false, updatable = false) 옵션 적용
     */
    @Test
    @DisplayName("JoinColumn 옵션을 통해 update 쿼리를 막을 수 있다.")
    void test2_1() {
        Team2 t1 = new Team2("T1");
        Player2 faker = new Player2("페이커");
        Player2 keria = new Player2("케리아");
        t1.add(faker);
        t1.add(keria);

        team2Repository.save(t1);
        team2Repository.flush();
    }

    @Test
    @DisplayName("update 쿼리를 막으면 이후 수정이 불가능하다.")
    void test2_2() {
        Team2 t1 = new Team2("T1");
        Team2 drx = new Team2("DRX");
        Player2 keria = new Player2("케리아");
        Player2 deft = new Player2("데프트");
        drx.add(keria);
        drx.add(deft);

        team2Repository.save(drx);
        team2Repository.save(t1);

        drx.remove(keria);
        t1.add(keria);

        team2Repository.flush();

        // keria의 fk가 t1의 id로 바뀌어야(update) 하지만 반영되지 않는 것을 알 수 있다.
        // 한번 Team에 추가하고 나면, Team에서 방출도, 전출도 되지 않는다.(고정)
    }

    @Test
    @DisplayName("개별 Player에 대한 수정은 가능하다.")
    void test2_3() {
        Team2 t1 = new Team2("T1");
        Team2 drx = new Team2("DRX");
        Player2 faker = new Player2("페이커");
        Player2 deft = new Player2("데프트");
        t1.add(faker);
        drx.add(deft);

        team2Repository.save(t1);
        team2Repository.save(drx);

        faker.setName("Faker");

        team2Repository.flush();

        // faker의 이름이 페이커에서 Faker로 변경되었다.
    }

    /**
     * [test3]
     * @JoinColumn에 (nullable = false, updatable = false) 옵션 적용
     * orphanRemoval = true를 사용
     */
    @Test
    @DisplayName("test2의 상태에서 orphanRemoval = true를 사용")
    void test3_1() {
        Team3 t1 = new Team3("T1");
        Team3 drx = new Team3("DRX");
        Player3 keria = new Player3("케리아");
        Player3 deft = new Player3("데프트");
        drx.add(keria);
        drx.add(deft);

        team3Repository.save(drx);
        team3Repository.save(t1);

        drx.remove(keria); // 영속성 컨텍스트에서 엔티티(고아객체)를 제거 (delete 쿼리라서 가능함)

        Player3 newKeria = new Player3("케리아"); // 새로운 엔티티 생성
        t1.add(newKeria); // 새로운 엔티티에 대한 insert 쿼리라서 가능함

        team3Repository.flush();

        // 케리아가 drx에서 t1으로 옮겨진 것이 아니라 삭제한 뒤 추가한 것
        // 사용 방식이 까다롭고 헷갈린다. 가능하면 쓰지 말아야겠다.
    }
}
