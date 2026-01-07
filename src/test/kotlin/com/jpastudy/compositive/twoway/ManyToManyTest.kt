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
}