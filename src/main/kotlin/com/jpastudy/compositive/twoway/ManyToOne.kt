package com.jpastudy.compositive.twoway


import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Library(1) : Book(N)
 * Library(1) : Manager(N)
 * Buyer(1) : Book(N)
 */
@Entity
data class Book(
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
data class Buyer(
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
data class Manager(
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
data class Library(
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
    @Query("select b from Book b join fetch b.library l where l.name = :libraryName")
    fun findAllByLibraryName(libraryName: String): MutableList<Book>
}

interface ManagerRepository : JpaRepository<Manager, Long> {
}

interface BuyerRepository : JpaRepository<Buyer, Long> {
}

interface LibraryRepository : JpaRepository<Library, Long> {
    fun findAllByBooksName(booksName: String): MutableList<Library>
}

/**
 * Student(N) : School(1)
 */
@Entity
data class Student(
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
data class School(
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
    @Query("select s from School s join fetch s.students st where st.name = :studentsName")
    fun findAllWithStudentsByStudentsName(studentsName: String): MutableList<School>

    //    fun findAllWithStudentsByName(name: String): MutableList<School>
    @Query("select s from School s join fetch s.students where s.name = :name")
    fun findAllWithStudentsByName(name: String): MutableList<School>
}
