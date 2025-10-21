package com.jpastudy


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

    var name: String,

    @OneToMany(mappedBy = "buyer")
    var books: MutableList<Book> = mutableListOf()
) {
    fun buyBooks(books: List<Book>) {
        this.books.addAll(books)
        books.forEach { it.buyer = this }
    }
}

@Entity
class Manager(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

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

    var name: String,

    @OneToMany(mappedBy = "library")
    var books: MutableList<Book> = mutableListOf(),

    @OneToMany(mappedBy = "library")
    var managers: MutableList<Manager> = mutableListOf()
) {
    fun addBook(book: Book) {
        this.books.add(book)
        book.library = this
    }

    fun addBooks(books: List<Book>) {
        this.books.addAll(books)
        books.forEach { it.library = this }
    }

    fun hire(manager: Manager) {
        this.managers.add(manager)
        manager.library = this
    }
}

interface BookRepository : JpaRepository<Book, Long> {
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
