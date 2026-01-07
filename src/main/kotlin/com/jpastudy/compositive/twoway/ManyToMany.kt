package com.jpastudy.compositive.twoway

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Board(N) : Tag(M)
 */
@Entity
data class Board(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL]) // REMOVE는 사용할 이유가 없음 -> PERSIST, MERGE 정도만 사용
    @JoinTable(
        name = "board_tag",
        joinColumns = [JoinColumn(name = "board_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableList<Tag> = mutableListOf(),
) {
    fun addTags(vararg tags: Tag) {
        tags.forEach { it.addBoard(this) }
        this.tags.addAll(tags)
    }

    fun clearTags() {
        tags.forEach { it.removeBoard(this) }
        tags.clear()
    }
}

@Entity
data class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
    var boards: MutableList<Board> = mutableListOf(),
) {
    fun addBoard(board: Board) {
        boards.add(board)
    }

    fun removeBoard(board: Board) {
        boards.remove(board)
    }
}

interface BoardRepository : JpaRepository<Board, Long> {}
interface TagRepository : JpaRepository<Tag, Long> {}

/**
 * Job(N) : Worker(M)
 */
@Entity
data class Worker(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE] // REMOVE는 사용할 이유가 없음 -> PERSIST, MERGE 정도만 사용
    )
    @JoinTable(
        name = "worker_job",
        joinColumns = [JoinColumn(name = "worker_id")],
        inverseJoinColumns = [JoinColumn(name = "job_id")]
    )
    var jobs: MutableList<Job> = mutableListOf(),
) {
    fun addJobs(vararg jobs: Job) {
        jobs.forEach { it.addWorker(this) }
        this.jobs.addAll(jobs)
    }

    fun removeJobs(vararg jobs: Job) {
        jobs.forEach { it.removeWorker(this) }
        this.jobs.removeAll(jobs.toList())
    }

    fun clearJobs() {
        jobs.forEach { it.removeWorker(this) }
        jobs.clear()
    }
}

@Entity
data class Job(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "jobs")
    var workers: MutableList<Worker> = mutableListOf(),
) {
    fun addWorker(worker: Worker) {
        workers.add(worker)
    }

    fun removeWorker(worker: Worker) {
        workers.remove(worker)
    }
}

interface WorkerRepository : JpaRepository<Worker, Long> {
    @Query("select w from Worker w join fetch w.jobs j")
    fun findAllWithJobs(): MutableList<Worker>
}

interface JobRepository : JpaRepository<Job, Long> {
}
