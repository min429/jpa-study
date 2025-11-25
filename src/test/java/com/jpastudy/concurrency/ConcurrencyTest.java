package com.jpastudy.concurrency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrencyTest {

    @Nested
    class CAS {

        @Test
        @DisplayName("단일 연산 - CAS를 쓰지 않아도 됨")
        void test1() throws InterruptedException {
            Map<Integer, Integer> map = new ConcurrentHashMap<>();
            int threadCount = 1000;
            CountDownLatch latch = new CountDownLatch(threadCount);

            try (ExecutorService executor = Executors.newFixedThreadPool(20)) {
                for (int i = 0; i < threadCount; i++) {
                    int key = i;
                    executor.submit(() -> {
                        map.put(key, key);
                        latch.countDown();
                    });
                }
                latch.await();
            }

            assertThat(map.size()).isEqualTo(threadCount);
        }

        @Test
        @DisplayName("복합 연산 - 동시성 문제 발생 1")
        void test2() throws InterruptedException {
            Map<String, Integer> map = new ConcurrentHashMap<>();
            map.put("count", 0);

            int threadCount = 1000;
            CountDownLatch latch = new CountDownLatch(threadCount);

            try (ExecutorService executor = Executors.newFixedThreadPool(20)) {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        // 복합 연산 → 경쟁 조건 발생
                        int cur = map.get("count");
                        map.put("count", cur + 1);
                        latch.countDown();
                    });
                }
                latch.await();
            }

            assertThat(map.get("count")).isNotEqualTo(1000);
        }

        @Test
        @DisplayName("복합 연산 - CAS 연산을 통해 동시성 문제 방지 1")
        void test5() throws InterruptedException {
            Map<String, Integer> map = new ConcurrentHashMap<>();
            map.put("count", 0);

            int threadCount = 1000;
            CountDownLatch latch = new CountDownLatch(threadCount);

            try (ExecutorService executor = Executors.newFixedThreadPool(20)) {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        int cur;
                        do {
                            cur = map.get("count");
                        } while (!map.replace("count", cur, cur + 1));
                        latch.countDown();
                    });
                }
                latch.await();
            }

            assertThat(map.get("count")).isEqualTo(0);
        }

        @Test
        @DisplayName("복합 연산 - 동시성 문제 발생 2")
        void test3() throws InterruptedException {
            AtomicInteger a = new AtomicInteger(0);

            int threadCount = 1000;
            CountDownLatch latch = new CountDownLatch(threadCount);

            try (ExecutorService executor = Executors.newFixedThreadPool(20)) {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        int cur = a.get();
                        a.set(cur + 1);
                        latch.countDown();
                    });
                }
                latch.await();
            }

            assertThat(a.get()).isNotEqualTo(1000);
        }

        @Test
        @DisplayName("복합 연산 - CAS 연산을 통해 동시성 문제 방지 2")
        void test6() throws InterruptedException {
            AtomicInteger a = new AtomicInteger(0);

            int threadCount = 1000;
            CountDownLatch latch = new CountDownLatch(threadCount);

            try (ExecutorService executor = Executors.newFixedThreadPool(20)) {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        int cur;
                        do {
                            cur = a.get();
                        } while (!a.compareAndSet(cur, cur + 1));
                        latch.countDown();
                    });
                }
                latch.await();
            }

            assertThat(a.get()).isEqualTo(1000);
        }

        @Test
        @DisplayName("복합 연산 - 동시성 문제 발생 3")
        void test4() throws InterruptedException {
            AtomicInteger a = new AtomicInteger(0);

            int threadCount = 1000;
            CountDownLatch latch = new CountDownLatch(threadCount);

            try (ExecutorService executor = Executors.newFixedThreadPool(1000)) {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        int prev = a.get();
                        if (prev == 0) { // 동시에 여러 스레드가 들어감
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            int next = a.get() + 1;
                            a.set(next); // 동시성 보장x
                        }
                        latch.countDown();
                    });
                }
                latch.await();
            }

            assertThat(a.get()).isNotEqualTo(1);
        }

        @Test
        @DisplayName("복합 연산 - CAS 연산을 통해 동시성 문제 방지 3")
        void test7() throws InterruptedException {
            AtomicInteger a = new AtomicInteger(0);

            int threadCount = 1000;
            CountDownLatch latch = new CountDownLatch(threadCount);

            try (ExecutorService executor = Executors.newFixedThreadPool(1000)) {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        int next = a.get() + 1;
                        a.accumulateAndGet(next, (prev, x) -> {
                            if (prev == 0) { // 동시에 여러 스레드가 들어감
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                return x;
                            }
                            return prev;
                        }); // 값 반환 -> CAS 연산 시작 (동시성 보장)
                        latch.countDown();
                    });
                }
                latch.await();
            }

            assertThat(a.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("복합 연산 - CAS 연산을 통해 동시성 문제 방지 3 (직접 구현)")
        void test8() throws InterruptedException {
            AtomicInteger a = new AtomicInteger(0);

            int threadCount = 1000;
            CountDownLatch latch = new CountDownLatch(threadCount);

            try (ExecutorService executor = Executors.newFixedThreadPool(1000)) {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        int prev, next;
                        do {
                            prev = a.get();
                            if (prev == 0) { // 동시에 여러 스레드가 들어감
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                next = a.get() + 1;
                            } else {
                                next = prev;
                            }
                        } while (!a.compareAndSet(prev, next));

                        latch.countDown();
                    });
                }
                latch.await();
            }

            assertThat(a.get()).isEqualTo(1);
        }
    }
}
