package com.jpastudy.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityManager
import jakarta.persistence.Id
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@SpringBootTest
@Transactional
class PersistenceTest {

    @Autowired
    lateinit var em: EntityManager

    @Autowired
    lateinit var tr: TRepository

    @Test
    @DisplayName("merge vs persist")
    fun test() {
        val test = TEntity(1, "test")

        tr.save(test) // id가 있으므로 merge 된다.
        test.name = "changed" // merge인 경우 컨텍스트에 반영되지 않는다.

        em.flush()
        em.clear()

        println("name: ${tr.findById(1).get().name}")

        // merge(entity)는 entity를 shallow copy한 객체를 영속성 컨텍스트에 저장 및 return 하고,
        // persist(entity)는 entity를 영속성 컨텍스트에 저장 및 return 한다.
    }
}

@Entity
class TEntity(
    @Id
    var id: Long? = null, // GenerationType 지정x

    @Column(unique = true)
    var name: String
)

interface TRepository : JpaRepository<TEntity, Long>