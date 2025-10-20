package org.example.jpastudy

import jakarta.persistence.*

@Entity
class Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String,

    @OneToOne(fetch = FetchType.LAZY)
    var company: Company?
)

@Entity
class Company(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String,

    @OneToOne(mappedBy = "company", cascade = [CascadeType.ALL])
    var customer: Customer?
) {
    fun addCustomer(customer: Customer) {
        this.customer = customer
    }
}