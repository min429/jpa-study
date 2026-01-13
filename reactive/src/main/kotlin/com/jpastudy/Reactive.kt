package com.jpastudy.reactive

import jakarta.persistence.*
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Guest(N) : House(1)
 */
@Entity
data class Guest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, updatable = true)
    var house: House? = null
)

@Entity
data class House(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @OneToMany(
        mappedBy = "house",
        cascade = [CascadeType.ALL], orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var guests: MutableList<Guest> = mutableListOf(),
) {
    fun enrollGuests(vararg guests: Guest) {
        guests.forEach { it.house = this }
        this.guests.addAll(guests)
    }

    fun kickGuests(vararg guests: Guest) {
        guests.forEach { it.house = null }
        this.guests.removeAll(guests.toList())
    }
}

interface GuestRepository : CoroutineCrudRepository<Guest, Long> {}
interface HouseRepository : CoroutineCrudRepository<House, Long> {}

@Service
class ReactiveService {

    @Autowired
    lateinit var gr: GuestRepository

    @Autowired
    lateinit var hr: HouseRepository

    @Transactional(readOnly = true)
    suspend fun getGuests(): List<Guest> {
        val house = hr.findAll().toList().first()
        return house.guests
    }
}
