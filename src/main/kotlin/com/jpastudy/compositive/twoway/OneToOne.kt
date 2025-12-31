package com.jpastudy.compositive.twoway

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository

@Entity
data class Ceo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @OneToOne(fetch = FetchType.LAZY) // FK를 가지고 있는 쪽에서는 지연로딩을 적용할 수 있다.
    @JoinColumn(nullable = true, updatable = true)
    var company: Company? = null
)

@Entity
data class Company(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "company") // FK가 없는 쪽에서는 FetchType.LAZY를 해도 강제로 즉시로딩된다.
    var ceo: Ceo? = null
) {
    fun addCustomer(CEO: Ceo) {
        this.ceo = CEO
        CEO.company = this
    }
}

interface CompanyRepository : JpaRepository<Company, Long> {
}

interface CeoRepository : JpaRepository<Ceo, Long> {
    fun findByName(name: String): Ceo
}
