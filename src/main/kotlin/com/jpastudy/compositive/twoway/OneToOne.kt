package com.jpastudy.compositive.twoway

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository

@Entity
class Ceo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, updatable = true)
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

    @Column(unique = true)
    var name: String,

    @OneToOne(mappedBy = "company", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ceo: Ceo? = null
) {
    fun addCustomer(CEO: Ceo) {
        this.ceo = CEO
        CEO.company = this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Company

        if (id != other.id) return false
        if (name != other.name) return false
        if (ceo != other.ceo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + (ceo?.hashCode() ?: 0)
        return result
    }
}

interface CompanyRepository : JpaRepository<Company, Long> {
    fun findByName(name: String): Company
}

interface CeoRepository : JpaRepository<Ceo, Long> {
    fun findByName(name: String): Ceo
}
