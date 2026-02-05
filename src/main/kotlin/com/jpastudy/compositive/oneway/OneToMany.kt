package com.jpastudy.compositive.oneway

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Account(N) : Customer(1)
 */
@Entity
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String
)

@Entity
data class Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @OneToMany(
        cascade = [CascadeType.ALL], orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "customer_id", nullable = false) // OnUpdate는 없음. updatable = false가 좋음
    @OnDelete(action = OnDeleteAction.CASCADE) // 일대다 단방향에서 OnDelete 설정 가능
    var accounts: MutableList<Account> = mutableListOf(),
) {
    fun addAccounts(vararg accounts: Account) {
        this.accounts.addAll(accounts)
    }

    fun removeAccounts(vararg accounts: Account) {
        this.accounts.removeAll(accounts.toList())
    }
}

interface AccountRepository : JpaRepository<Account, Long> {}
interface CustomerRepository : JpaRepository<Customer, Long> {}
