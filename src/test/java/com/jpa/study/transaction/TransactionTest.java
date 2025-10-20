package com.jpa.study.transaction;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional // 테스트 종료 시 롤백
@SpringBootTest
public class TransactionTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private LogService logService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LogRepository logRepository;

    private Member member;

    private Log log;

    @BeforeEach
    void setup() {
        member = Member.builder()
            .name("member")
            .build();

        log = Log.builder()
            .description("log")
            .build();
    }

    @AfterEach
    void cleanup() {
        memberRepository.deleteAll();
        logRepository.deleteAll();
    }

    /**
     * Test1 ~ 6: "다른 스레드에서 발생한 커넥션은 아예 분리된(상관없는) 트랜잭션이다."
     */

    @Test
    @DisplayName("다른 스레드에서 REQUIRED -> 다른 커넥션")
    void test1() {
        memberService.save(member);

        CompletableFuture.runAsync(() -> {
            logService.save(log);
        }).join(); // 저장 완료 시까지 대기
    }

    @Test
    @DisplayName("다른 스레드에서 REQUIRES_NEW -> 다른 커넥션")
    void test2() {
        memberService.save(member);

        CompletableFuture.runAsync(() -> {
            logService.save_new(log);
        }).join(); // 저장 완료 시까지 대기
    }

    @Test
    @DisplayName("같은 스레드에서 REQUIRED -> 동일한 커넥션")
    void test3() {
        memberService.save(member);
        logService.save(log);
    }

    @Test
    @DisplayName("같은 스레드에서 REQUIRES_NEW -> 다른 커넥션")
    void test4() {
        memberService.save(member);
        logService.save_new(log);
    }

    @Test
    @DisplayName("다른 스레드에서 REQUIRED 내부 실패 시 내부만 롤백된다.")
    void test5() {
        memberService.save(member);

        CompletableFuture.runAsync(() -> {
            try {
                logService.save_fail(log);
            } catch (Exception e) {
            }
        }).join(); // 저장 완료 시까지 대기

        assertThat(memberRepository.count()).isEqualTo(1);
        assertThat(logRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("다른 스레드에서 REQUIRES_NEW 내부 실패 시 내부만 롤백된다.")
    void test6() {
        memberService.save(member);

        CompletableFuture.runAsync(() -> {
            try {
                logService.save_new_fail(log);
            } catch (Exception e) {
            }
        }).join(); // 저장 완료 시까지 대기

        assertThat(memberRepository.count()).isEqualTo(1);
        assertThat(logRepository.count()).isEqualTo(0);
    }
}
