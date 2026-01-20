package com.jpastudy.compositive.twoway

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
data class Ceo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @Enumerated(EnumType.STRING)
    var gender: Gender? = null,

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "ceo")
    var company: Company? = null
) {
    enum class Gender {
        MAN, WOMAN
    }
}

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

    @Query("select c from Ceo c where c.gender = :gender")
    fun findByGenderQuery(gender: Ceo.Gender): Ceo
}
