package com.jpastudy.compositive.twoway

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@SpringBootTest
@Transactional
class ManyToManyTest {

    @Autowired
    lateinit var em: EntityManager

    @Autowired
    lateinit var br: BoardRepository

    @Autowired
    lateinit var tr: TagRepository

    @Autowired
    lateinit var wr: WorkerRepository

    @Autowired
    lateinit var jr: JobRepository

    private fun flushAndClear() {
        em.flush()
        em.clear()
    }

    @Test
    @DisplayName("ManyToMany에서 상대 테이블에 persist 영속성 전이 가능")
    fun test1() {
        val board = Board(name = "board")
        val tags = listOf(Tag(name = "tag1"), Tag(name = "tag2"))

        board.addTags(*tags.toTypedArray())
        br.save(board)

        flushAndClear()

        val savedBoard = br.findById(board.id!!).get()
        assertThat(savedBoard.tags.size).isEqualTo(tags.size)
    }

    @Test
    @DisplayName("ManyToMany에서 중간 테이블에 대한 orphanRemoval은 자동 적용된다. 상대 테이블에는 orphanRemoval을 적용할 수 없다.")
    fun test2() {
        val board = Board(name = "board")
        val tags = listOf(Tag(name = "tag1"), Tag(name = "tag2"))

        board.addTags(*tags.toTypedArray())
        br.save(board)

        flushAndClear()

        var savedBoard = br.findById(board.id!!).get()
        savedBoard.clearTags()

        flushAndClear()

        savedBoard = br.findById(board.id!!).get()
        assertThat(savedBoard.tags.size).isEqualTo(0)
    }

    @Test
    @DisplayName("ManyToMany에서 상대 테이블에 remove 영속성 전이를 걸어도 FK 제약조건 위반으로 삭제가 불가능할 수 있다.")
    fun test3() {
        val boards = listOf(Board(name = "board1"), Board(name = "board2"))
        val tags = listOf(Tag(name = "tag1"), Tag(name = "tag2"))

        boards.forEach { it.addTags(*tags.toTypedArray()) }
        br.saveAll(boards)

        flushAndClear()

        val savedBoard = br.findAll().first()

        assertThatThrownBy {
            br.delete(savedBoard)
            flushAndClear()
        }.isInstanceOf(ConstraintViolationException::class.java)
    }

    @Test
    @DisplayName("ManyToMany에서 중간 테이블에 대한 영속성 전이는 자동 적용된다.")
    fun test4() {
        val worker = Worker(name = "worker")
        val jobs = listOf(Job(name = "job1"), Job(name = "job2"))

        worker.addJobs(*jobs.toTypedArray())
        wr.save(worker)

        flushAndClear()

        val savedWorker = wr.findById(worker.id!!).get()
        wr.delete(savedWorker)

        flushAndClear()

        val savedJob = jr.findById(jobs.first().id!!).get()
        assertThat(savedJob.workers.size).isEqualTo(0)
    }

    @Test
    @DisplayName("ManyToMany에서 clear 호출 시 M + 2 쿼리가 발생할 수 있음 (M: job의 개수)")
    fun test5() {
        val workers = listOf(Worker(name = "worker1"), Worker(name = "worker2"))
        val jobs = listOf(Job(name = "job1"), Job(name = "job2"))

        workers.forEach { it.addJobs(*jobs.toTypedArray()) }
        wr.saveAll(workers)

        flushAndClear()

        val savedWorkers = wr.findAllWithJobs()
        val savedJobs = jr.findAll()

        savedWorkers.forEach { it.removeJobs(*savedJobs.toTypedArray()) } // 영속성 컨텍스트에 있는 job을 넣어야 됨

        // savedWorker 마다 it.removeJobs 호출 n 번 { job 마다 removeWorker 호출 m번 } 쿼리 n * m 라고 생각할 수 있지만,
        // 결과적으로 최종 job의 개수인 m개 만큼만 추가로 나감 (savedWorker마다 같은 job을 공유할 수 있기 때문에)
        // 이게 싫으면 savedJobs를 가져올 때 fetch join으로 가져오면 되지만, 그냥 배치사이즈 쓰는게 나은 것 같음
        flushAndClear()
    }
}