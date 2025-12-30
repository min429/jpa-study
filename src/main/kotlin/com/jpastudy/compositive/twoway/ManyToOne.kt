package com.jpastudy.compositive.twoway


import jakarta.persistence.*
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Library(1) : Book(N)
 * Library(1) : Manager(N)
 * Buyer(1) : Book(N)
 */
@Entity
class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    var library: Library? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    var buyer: Buyer? = null
)

@Entity
class Buyer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @OneToMany(
        mappedBy = "buyer",
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH],
        fetch = FetchType.LAZY
    )
    var books: MutableList<Book> = mutableListOf()
) {
    fun buyBooks(vararg books: Book) {
        val targets = books.toSet()
        targets.forEach { it.buyer = this }
        this.books.addAll(targets)
    }

    fun sellBooks(vararg books: Book) {
        val targets = books.toSet()
        targets.forEach { it.buyer = null }
        this.books.removeAll(targets)
    }
}

@Entity
class Manager(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    var library: Library? = null
)

@Entity
@Table(name = "`library`")
class Library(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @OneToMany(
        mappedBy = "library",
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH],
        fetch = FetchType.LAZY
    )
    var books: MutableList<Book> = mutableListOf(),

    @OneToMany(
        mappedBy = "library",
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH],
        fetch = FetchType.LAZY
    )
    var managers: MutableList<Manager> = mutableListOf()
) {
    fun addBooks(vararg books: Book) {
        val targets = books.toSet()
        targets.forEach { it.library = this }
        this.books.addAll(targets)
    }

    fun hireManagers(vararg managers: Manager) {
        val targets = managers.toSet()
        targets.forEach { it.library = this }
        this.managers.addAll(targets)
    }

    fun removeBooks(vararg books: Book) {
        val targets = books.toSet()
        targets.forEach { it.library = null }
        this.books.removeAll(targets)
    }

    fun fireManagers(vararg managers: Manager) {
        val targets = managers.toSet()
        targets.forEach { it.library = null }
        this.managers.removeAll(targets)
    }
}

interface BookRepository : JpaRepository<Book, Long> {
    fun findByName(name: String): Book
}

interface ManagerRepository : JpaRepository<Manager, Long> {
}

interface BuyerRepository : JpaRepository<Buyer, Long> {
}

interface LibraryRepository : JpaRepository<Library, Long> {

    @Query("select b from Library l join l.books b where l.name = :name")
    fun findBooksByLibraryName(name: String): List<Book>

    @EntityGraph(attributePaths = ["books", "managers"])
    fun findLibraryByName(name: String): Library
}
