package com.jpastudy

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository

@Entity
class Ceo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String,

    @OneToOne(fetch = FetchType.LAZY)
    var company: Company? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ceo) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

@Entity
class Company(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String,

    @OneToOne(mappedBy = "company", cascade = [CascadeType.ALL])
    var ceo: Ceo? = null
) {
    fun addCustomer(CEO: Ceo) {
        this.ceo = CEO
        CEO.company = this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Company) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

interface CompanyRepository : JpaRepository<Company, Long> {
    fun findByName(name: String): Company
}

interface CeoRepository : JpaRepository<Ceo, Long> {
    fun findByName(name: String): Ceo
}
