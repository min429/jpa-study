package com.jpastudy.persistence

import jakarta.persistence.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
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

    @Autowired
    lateinit var ur: UserRepository

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

    @Test
    @DisplayName(
        "id 할당 후 persist 시 EntityExistsException 예외 발생" +
                ", id 할당 후 merge 시 OptimisticLockException 예외 발생"
    )
    fun testUnsavedValue() {
        val user = User(id = 1L, name = "test")

        assertThrows<EntityExistsException> {
            em.persist(user) // EntityExistsException: detached entity passed to persist
        }

        assertThrows<OptimisticLockException> {
            em.merge(user) // OptimisticLockException: unsaved-value mapping was incorrect
        }
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

@Entity
@Table(name = "`user`")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String
)

interface UserRepository : JpaRepository<User, String>