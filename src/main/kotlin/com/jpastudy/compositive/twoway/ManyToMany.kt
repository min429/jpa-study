package com.jpastudy.compositive.twoway

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Board(N) : Tag(1)
 */
@Entity
data class Board(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL]) // REMOVE는 사용할 이유가 없음 -> PERSIST, MERGE 정도만 사용
    @JoinTable(name = "board_tag")
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