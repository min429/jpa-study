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
    @JoinColumn(nullable = true, updatable = true)
    var library: Library? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, updatable = true)
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
        val targets = books.toList()
        targets.forEach { it.buyer = this }
        this.books.addAll(targets)
    }

    fun sellBooks(vararg books: Book) {
        val targets = books.toList()
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
    @JoinColumn(nullable = true, updatable = true)
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
        val targets = books.toList()
        targets.forEach { it.library = this }
        this.books.addAll(targets)
    }

    fun hireManagers(vararg managers: Manager) {
        val targets = managers.toList()
        targets.forEach { it.library = this }
        this.managers.addAll(targets)
    }

    fun removeBooks(vararg books: Book) {
        val targets = books.toList()
        targets.forEach { it.library = null }
        this.books.removeAll(targets)
    }

    fun fireManagers(vararg managers: Manager) {
        val targets = managers.toList()
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

    fun findByBooksName(booksName: String): MutableList<Library>
}

/**
 * Student(N) : School(1)
 */
@Entity
class Student(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, updatable = true)
    var school: School? = null
)

@Entity
class School(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @OneToMany(
        mappedBy = "school",
        cascade = [CascadeType.ALL], orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var students: MutableList<Student> = mutableListOf(),
) {
    fun enrollStudents(vararg students: Student) {
        val targets = students.toList()
        targets.forEach { it.school = this }
        this.students.addAll(targets)
    }
}

interface StudentRepository : JpaRepository<Student, Long> {}
interface SchoolRepository : JpaRepository<School, Long> {
    @Query("select sc from School sc join fetch sc.students st where st.name = :studentsName")
    fun findByStudentsNameNow(studentsName: String): MutableList<School>
}
