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
        books.forEach { it.buyer = this }
        this.books.addAll(books)
    }

    fun sellBooks(vararg books: Book) {
        books.forEach { it.buyer = null }
        this.books.removeAll(books.toList())
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
        books.forEach { it.library = this }
        this.books.addAll(books)
    }

    fun hireManagers(vararg managers: Manager) {
        managers.forEach { it.library = this }
        this.managers.addAll(managers)
    }

    fun removeBooks(vararg books: Book) {
        books.forEach { it.library = null }
        this.books.removeAll(books.toList())
    }

    fun fireManagers(vararg managers: Manager) {
        managers.forEach { it.library = null }
        this.managers.removeAll(managers.toList())
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
        students.forEach { it.school = this }
        this.students.addAll(students)
    }

    fun kickStudents(vararg students: Student) {
        students.forEach { it.school = null }
        this.students.removeAll(students.toList())
    }
}

interface StudentRepository : JpaRepository<Student, Long> {}
interface SchoolRepository : JpaRepository<School, Long> {
    @Query("select s from School s join fetch s.students st where st.name = :studentsName")
    fun findAllWithStudentsByStudentsName(studentsName: String): MutableList<School>

    @Query("select s from School s join fetch s.students where s.name = :name")
    fun findAllWithStudentsByName(name: String): MutableList<School>
}

/**
 * Camera(N) : Event(M)
 * Camera(1) : EventCamera(N)
 * Event(1) : EventCamera(M)
 */
@Entity
data class Camera(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @OneToMany(
        mappedBy = "camera",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var eventCameras: MutableList<EventCamera> = mutableListOf()
) {
    fun addEventCameras(vararg eventCameras: EventCamera) {
        eventCameras.forEach { it.camera = this }
        this.eventCameras.addAll(eventCameras)
    }

    fun removeEventCameras(vararg eventCameras: EventCamera) {
        eventCameras.forEach { it.camera = null }
        this.eventCameras.removeAll(eventCameras.toList())
    }
}

@Entity
data class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @OneToMany(
        mappedBy = "event",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var eventCameras: MutableList<EventCamera> = mutableListOf(),
) {
    fun addEventCameras(vararg eventCameras: EventCamera) {
        eventCameras.forEach { it.event = this }
        this.eventCameras.addAll(eventCameras)
    }

    fun removeEventCameras(vararg eventCameras: EventCamera) {
        eventCameras.forEach { it.event = null }
        this.eventCameras.removeAll(eventCameras.toList())
    }
}

@Entity
data class EventCamera(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, updatable = true)
    var event: Event? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, updatable = true)
    var camera: Camera? = null
)

interface EventCameraRepository : JpaRepository<EventCamera, Long> {}
interface CameraRepository : JpaRepository<Camera, Long> {}
interface EventRepository : JpaRepository<Event, Long> {}