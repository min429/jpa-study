package com.jpa.study.oneway.manytoone;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.jpa.study.oneway.manytoone.test1.Department1;
import com.jpa.study.oneway.manytoone.test1.Department1Repository;
import com.jpa.study.oneway.manytoone.test1.Employee1;
import com.jpa.study.oneway.manytoone.test1.Employee1Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
public class ManyToOneTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private Department1Repository department1Repository;

    @Autowired
    private Employee1Repository employee1Repository;

    void init() {
        Department1 d = new Department1("D%d".formatted(1), 1L);
        department1Repository.save(d);

        for (int i = 1; i <= 3; i++) {
            Employee1 e = new Employee1("E%d".formatted(i), (long)i, d);
            employee1Repository.save(e);
        }

        em.flush();
        em.clear();
    }

    void init2() {
        List<Department1> departments = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Department1 d = new Department1("D%d".formatted(i), (long)i);
            departments.add(d);
        }
        department1Repository.saveAll(departments);

        for (int i = 1; i <= 3; i++) {
            Employee1 e = new Employee1("E%d".formatted(i), (long)i, departments.get(i - 1));
            employee1Repository.save(e);
        }

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("@SoftDelete & 즉시로딩으로 연관 엔티티를 조회할 때 deleted 필터링 하는지 확인")
    void test1() {
        init();
        employee1Repository.findAll();
        // 이유는 모르겠지만 이 프로젝트에서는 @SoftDelete와 지연로딩을 같이 쓸 수 있음
    }

    @Test
    @DisplayName("즉시로딩시 N+1 문제 발생")
    void test2() {
        init2();
        employee1Repository.findAll();
        // 이유는 모르겠지만 이 프로젝트에서는 @SoftDelete와 지연로딩을 같이 쓸 수 있음
    }
}
