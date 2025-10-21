package com.jpastudy.twoway.manytoone;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.jpastudy.twoway.manytoone.test1.Comment1;
import com.jpastudy.twoway.manytoone.test1.Comment1Repository;
import com.jpastudy.twoway.manytoone.test1.Post1;
import com.jpastudy.twoway.manytoone.test1.Post1Repository;
import com.jpastudy.twoway.manytoone.test2.Comment2;
import com.jpastudy.twoway.manytoone.test2.Comment2Repository;
import com.jpastudy.twoway.manytoone.test2.Post2;
import com.jpastudy.twoway.manytoone.test2.Post2Repository;
import com.jpastudy.twoway.manytoone.test3.Comment3;
import com.jpastudy.twoway.manytoone.test3.Comment3Repository;
import com.jpastudy.twoway.manytoone.test3.Post3;
import com.jpastudy.twoway.manytoone.test3.Post3Repository;

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
    private Post1Repository post1Repository;

    @Autowired
    private Comment1Repository comment1Repository;

    @Autowired
    private Post2Repository post2Repository;

    @Autowired
    private Comment2Repository comment2Repository;

    @Autowired
    private Post3Repository post3Repository;

    @Autowired
    private Comment3Repository comment3Repository;

    void init1() {
        for (int i = 1; i <= 3; i++) {
            Post1 p = Post1.builder()
                .title("title%d".formatted(i))
                .body("body%d".formatted(i))
                .build();

            for (int j = 1; j <= 3; j++) {
                Comment1 c = Comment1.builder()
                    .body("body%d".formatted(j))
                    .build();
                p.addComment(c);
            }

            post1Repository.save(p);
        }
        em.flush();
        em.clear();

        System.out.println("-------------------------------------------------------------------------");
        System.out.println("--------------------------------생성 완료--------------------------------");
        System.out.println("-------------------------------------------------------------------------");
    }

    void init2() {
        for (int i = 1; i <= 3; i++) {
            Post2 p = Post2.builder()
                .title("title%d".formatted(i))
                .body("body%d".formatted(i))
                .build();

            for (int j = 1; j <= 3; j++) {
                Comment2 c = Comment2.builder()
                    .body("body%d".formatted(j))
                    .build();
                p.addComment(c);
            }

            post2Repository.save(p);
        }
        em.flush();
        em.clear();

        System.out.println("-------------------------------------------------------------------------");
        System.out.println("--------------------------------생성 완료--------------------------------");
        System.out.println("-------------------------------------------------------------------------");
    }

    void init3() {
        for (int i = 1; i <= 3; i++) {
            Post3 p = Post3.builder()
                .title("title%d".formatted(i))
                .body("body%d".formatted(i))
                .build();

            for (int j = 1; j <= 3; j++) {
                Comment3 c = Comment3.builder()
                    .body("body%d".formatted(j))
                    .build();
                p.addComment(c);
            }

            post3Repository.save(p);
        }
        em.flush();
        em.clear();

        System.out.println("-------------------------------------------------------------------------");
        System.out.println("--------------------------------생성 완료--------------------------------");
        System.out.println("-------------------------------------------------------------------------");
    }

    /**
     * [test1]
     * 배치사이즈 적용x
     */
    @Test
    @DisplayName("Post 조회 시 Comment는 가져오지 않는다.")
    void test1_1() {
        init1();
        post1Repository.findAll();
    }

    @Test
    @DisplayName("Comment 조회 시 N+1 문제가 발생하도록 구현")
    void test1_2() {
        init1();
        List<Post1> posts = post1Repository.findAll();
        posts.forEach(p -> p.getComments().size());
    }

    @Test
    @DisplayName("Comment 조회 시 N+1 문제가 발생하지 않도록 구현")
    void test1_3() {
        init1();
        List<Post1> posts = post1Repository.findAll();
        comment1Repository.findAllByPostIdIn(posts.stream().map(Post1::getId).collect(Collectors.toList()));
    }

    @Test
    @DisplayName("Post 조회 시 N+1 문제가 발생")
    void test1_4() {
        init1();
        List<Comment1> comments = comment1Repository.findAll(); // Post의 FK만 가지고 있고, Post에 대해서는 조회하지 않음
        comments.forEach(c -> c.getPost().getBody()); // 각각 개별 Post에 대해 조회함
    }

    @Test
    @DisplayName("Post 조회 시 N+1 문제가 발생")
    void test1_5() {
        init1();
        List<Comment1> comments = comment1Repository.findAll(); // Post의 FK만 가지고 있고, Post에 대해서는 조회하지 않음
        comments.forEach(c -> c.getPost().getBody()); // 각각 개별 Post에 대해 조회함
    }

    /**
     * [test2]
     * 배치사이즈 적용o
     */
    @Test
    @DisplayName("Post 조회 시 Comment는 가져오지 않는다.")
    void test2_1() {
        init2();
        post2Repository.findAll();
    }

    @Test
    @DisplayName("Comment 조회 시 배치사이즈로 인해 N+1 문제가 발생하지 않음")
    void test2_2() {
        init2();
        List<Post2> posts = post2Repository.findAll();
        posts.forEach(p -> p.getComments().size());
        // 배치사이즈 만큼의 post_id에 해당하는 comments를 가져옴 -> 쿼리가 (N / 배치사이즈) + 1 또는 (N / 배치사이즈) + 2 만큼 발생
        // (N / 배치사이즈) + 2 : N이 배치사이즈로 나누어지지 않으면 쿼리가 한번 더 발생하는 것
        // 여기서 N은 post_id의 총 개수
    }

    @Test
    @DisplayName("Post 조회 시 배치사이즈로 인해 N+1 문제가 발생하지 않음")
    void test2_3() {
        init2();
        List<Comment2> comments = comment2Repository.findAll();
        comments.forEach(c -> c.getPost().getBody());
        // 배치사이즈 만큼의 post_id에 해당하는 posts를 가져옴
        // 이외에는 test2_2와 동일한 방식
    }

    /**
     * [test3]
     * @SoftDelete 적용
     */
    @Test
    @DisplayName("@SoftDelete & 즉시로딩으로 연관 엔티티를 조회할 때 deleted 필터링 하는지 확인")
    void test3_1() {
        init3();
        List<Post3> posts = post3Repository.findAll();
        posts.forEach(p -> p.getComments().size());
    }
}
