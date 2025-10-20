package org.example.jpastudy

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@SpringBootTest
@Transactional
class JpaTest {

    @Autowired
    lateinit var em: EntityManager

    @Test
    fun test1() {
        val company = Company(name = "company", customer = null)
        val customer = Customer(name = "company", company = null)
        em.persist(company)
        company.addCustomer(customer)
        println("name: ${company.customer?.name}")
    }
}