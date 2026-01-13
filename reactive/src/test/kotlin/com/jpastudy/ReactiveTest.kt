package com.jpastudy.reactive

import jakarta.persistence.EntityManager
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@SpringBootTest
@Transactional
class ReactiveTest {

    @Autowired
    lateinit var em: EntityManager

    @Autowired
    lateinit var rs: ReactiveService

    private fun flushAndClear() {
        em.flush()
        em.clear()
    }

    @Test
    @DisplayName("suspend 함수에서 지연로딩 실패")
    fun test1() = runTest {
        val guests = rs.getGuests()
    }
}