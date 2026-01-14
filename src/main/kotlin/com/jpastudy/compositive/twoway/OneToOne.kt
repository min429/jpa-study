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

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "ceo")
    var company: Company? = null
)

@Entity
data class Company(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @OneToOne(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @JoinColumn(nullable = true, updatable = true)
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
