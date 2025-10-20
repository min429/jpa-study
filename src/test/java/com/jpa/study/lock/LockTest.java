package com.jpa.study.lock;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// @Transactional // 하나의 트랜잭션을 여러 스레드가 공유할 수 없으므로 멀티 스레드 환경 테스트 시 사용하지 않는다.
@SpringBootTest
public class LockTest {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private Board1Repository board1Repository;

	@Autowired
	private Board2Repository board2Repository;

	@Autowired
	private Board1Service board1Service;

	@Autowired
	private Board2Service board2Service;

	private Long boardId1;
	private Long boardId2;

	private Board1 getBoard1() {
		return Board1.builder()
			.title("t1")
			.body("b1")
			.build();
	}

	private Board2 getBoard2() {
		return Board2.builder()
			.title("t2")
			.body("b2")
			.build();
	}

	@BeforeEach
	void setUp() {
		boardId1 = board1Repository.save(getBoard1()).getId();
		boardId2 = board2Repository.save(getBoard2()).getId();
	}

	/**
	 * 테스트 결과 정리
	 * 1. 락을 걸지 않은 경우 (65ms) -> 모두 성공, 정합성 문제 발생
	 * 2. 낙관적 락을 적용한 경우 (91ms) -> 일부 성공, 정합성 문제 없음
	 * 3. Update 락을 적용한 경우 (94ms) -> 모두 성공, 정합성 문제 없음
	 * 4. 쓰기 락을 적용한 경우 (189ms) -> 모두 성공, 정합성 문제 없음
	 * 5. 읽기 락을 적용한 경우 (374ms) -> 일부 성공, 정합성 문제 없음, 데드락 발생
	 * 쓰기 락은 Update 락에 비해 약 2배 느린 것으로 확인된다.
	 */

	@Test
	@DisplayName("락을 걸지 않았을 때")
	void test1() throws InterruptedException {
		int executeNumber = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		CountDownLatch latch = new CountDownLatch(executeNumber);

		final AtomicInteger successCount = new AtomicInteger();
		final AtomicInteger failCount = new AtomicInteger();

		long startTime = System.currentTimeMillis(); // 시작 시간 기록

		for (int i = 0; i < executeNumber; i++) {
			executorService.execute(() -> {
				try {
					board1Service.likeWithoutLock(boardId1);
					successCount.getAndIncrement();
				} catch (Exception e) {
					failCount.getAndIncrement();
					e.printStackTrace();
				}
				latch.countDown();
			});
		}

		latch.await(); // 모든 작업 끝날 때까지 대기

		long endTime = System.currentTimeMillis(); // 끝 시간 기록
		long duration = endTime - startTime;

		Board1 result = board1Repository.findById(boardId1).orElseThrow();
		printResult(successCount, failCount, result.getLikes(), duration);
		assertThat(result.getLikes()).isNotEqualTo(100L);
	}

	@Test
	@DisplayName("좋아요에 낙관적 락을 걸었을 때")
	void test2() throws InterruptedException {
		int executeNumber = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		CountDownLatch latch = new CountDownLatch(executeNumber);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		long startTime = System.currentTimeMillis(); // 시작 시간 기록

		for (int i = 0; i < executeNumber; i++) {
			executorService.execute(() -> {
				try {
					board2Service.likeWithOptimisticLock(boardId2);
					successCount.getAndIncrement();
				} catch (Exception e) { // 낙관적 락 충돌 예외 발생 시
					failCount.getAndIncrement();
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await(); // 모든 스레드 완료 대기

		long endTime = System.currentTimeMillis(); // 끝 시간 기록
		long duration = endTime - startTime;

		Board2 result = board2Repository.findById(boardId2).orElseThrow();
		printResult(successCount, failCount, result.getLikes(), duration);
		assertThat(result.getLikes()).isEqualTo(successCount.get()); // like 수 == 성공한 트랜잭션 수
		assertThat(result.getLikes()).isLessThan(100L); // 동시성 충돌로 실패 발생
	}

	@Test
	@DisplayName("좋아요에 Update 락을 걸었을 때")
	void test3() throws InterruptedException {
		int executeNumber = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		CountDownLatch latch = new CountDownLatch(executeNumber);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		long startTime = System.currentTimeMillis(); // 시작 시간 기록

		for (int i = 0; i < executeNumber; i++) {
			executorService.execute(() -> {
				try {
					// Update 쿼리는 DB에서 알아서 레코드에 대한 쓰기 락을 걸어준다.
					// 다른 트랜잭션은 해당 행을 읽을 수는 있어도 쓸 수는 없다. (읽기 락도 불가능)
					board1Repository.incrementLikes(boardId1);
					successCount.getAndIncrement();
				} catch (Exception e) {
					failCount.getAndIncrement();
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		long endTime = System.currentTimeMillis(); // 끝 시간 기록
		long duration = endTime - startTime;

		Board1 result = board1Repository.findById(boardId1).orElseThrow();
		printResult(successCount, failCount, result.getLikes(), duration);
		assertThat(result.getLikes()).isEqualTo(100L);
	}

	@Test
	@DisplayName("좋아요에 비관적 락(쓰기 락)을 걸었을 때")
	void test4() throws InterruptedException {
		int executeNumber = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		CountDownLatch latch = new CountDownLatch(executeNumber);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		long startTime = System.currentTimeMillis(); // 시작 시간 기록

		for (int i = 0; i < executeNumber; i++) {
			executorService.execute(() -> {
				try {
					board1Service.likeWithWriteLock(boardId1);
					successCount.getAndIncrement();
				} catch (Exception e) {
					failCount.getAndIncrement();
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		long endTime = System.currentTimeMillis(); // 끝 시간 기록
		long duration = endTime - startTime;

		Board1 result = board1Repository.findById(boardId1).orElseThrow();
		printResult(successCount, failCount, result.getLikes(), duration);
		assertThat(result.getLikes()).isEqualTo(100L); // 비관적 쓰기 락이면 100개 성공해야 정상
	}

	@Test
	@DisplayName("좋아요에 비관적 락(읽기 락)을 걸었을 때")
	void test5() throws InterruptedException {
		int executeNumber = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		CountDownLatch latch = new CountDownLatch(executeNumber);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		long startTime = System.currentTimeMillis(); // 시작 시간 기록

		for (int i = 0; i < executeNumber; i++) {
			executorService.execute(() -> {
				try {
					board1Service.likeWithReadLock(boardId1);
					successCount.getAndIncrement();
				} catch (Exception e) {
					failCount.getAndIncrement();
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		long endTime = System.currentTimeMillis(); // 끝 시간 기록
		long duration = endTime - startTime;

		Board1 result = board1Repository.findById(boardId1).orElseThrow();
		printResult(successCount, failCount, result.getLikes(), duration);
		assertThat(result.getLikes()).isNotEqualTo(100L); // 비관적 읽기 락이면 100개 성공하지 않을 가능성 높음
		// 읽기 락은 여러 트랜잭션이 동시에 걸 수 있음(읽기 자체에는 제한이 없기 때문)
		// 읽기 락을 여러 트랜잭션에서 얻고, 트랜잭션 도중 쓰기 작업을 동시에 하게 되면 아무도 작업을 할 수 없음 (데드락)
		// DB가 데드락을 감지 후 한쪽은 롤백해버림 -> 실패
		// 트랜잭션 내에서 읽기, 쓰기 작업이 같이 있는 경우 읽기 락을 걸면 안된다. 쓰기 락을 걸어야 한다.
	}

	private static void printResult(AtomicInteger successCount, AtomicInteger failCount, Long result, long durationMs) {
		System.out.println("성공한 트랜잭션 개수: " + successCount.get());
		System.out.println("실패한 트랜잭션 개수: " + failCount.get());
		System.out.println("최종 like 수: " + result);
		System.out.println("전체 소요 시간: " + durationMs + "ms");
	}
}
