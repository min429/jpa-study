package com.jpastudy.compositive.twoway

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@SpringBootTest
@Transactional
class OneToOneTest {

    @Autowired
    lateinit var em: EntityManager

    @Autowired
    lateinit var cmpr: CompanyRepository

    @Autowired
    lateinit var ceor: CeoRepository

    @Test
    @DisplayName("객체참조는 영속성 전이와 별도로 직접 설정해줘야 한다.")
    fun test1() {
        val company = Company(name = "company")
        val ceo = Ceo(name = "ceo")
        company.addCustomer(ceo) // 양쪽 객체 참조 설정

        em.persist(company) // repository.save()에서도 내부적으로 호출함
        em.flush()
        em.clear()

        val savedCeo = ceor.findByName("ceo")
        println("savedCmp class: ${savedCeo.company?.javaClass}") // proxy object

//        val savedCmp = cmpr.findByName("company")
//        println("savedCeo class: ${savedCmp.ceo?.javaClass}") // real object (JPA 스펙 상 OneToOne에서 연관관계 주인 엔티티(FK를 가진 엔티티)는 무조건 즉시로딩됨)
    }
}
