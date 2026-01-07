package com.jpastudy.compositive.twoway

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@SpringBootTest
@Transactional
class ManyToOneTest {

    @Autowired
    lateinit var em: EntityManager

    @Autowired
    lateinit var br: BookRepository

    @Autowired
    lateinit var lbr: LibraryRepository

    @Autowired
    lateinit var byr: BuyerRepository

    @Autowired
    lateinit var str: StudentRepository

    @Autowired
    lateinit var scr: SchoolRepository

    private fun flushAndClear() {
        em.flush()
        em.clear()
    }

    @Test
    @DisplayName("library.books 조회 가능")
    fun test2() {
        val books = listOf(Book(name = "book1"), Book(name = "book2"))
        val library = Library(name = "library")
        library.addBooks(*books.toTypedArray())

        // TODO: 영속성 전이 없애고 save 둘다 호출하는거 좀 별로인듯 -> test5에서 해결
        lbr.save(library)
        br.saveAll(books)

        println(br.findAllByLibraryName(library.name))
    }

    @Test
    @DisplayName("library 엔티티 그래프 조회")
    fun test3() {
        val book = Book(name = "book")
        val manager = Manager(name = "manager")
        val library = Library(name = "library")
        library.addBooks(book)
        library.hireManagers(manager)

        lbr.save(library)

        println(br.findAllByLibraryName(library.name))
    }

    @Test
    @DisplayName("findAll()은 즉시 DB를 조회하고 영속성 컨텍스트와 동기화한다.")
    fun test4() {
        val book = Book(name = "book")
        val library = Library(name = "library")
        library.addBooks(book)

        lbr.save(library) // @GeneratedValue를 쓰는 경우, persist/merge 시 books가 프록시객체(PersistentBag)으로 감싸짐
        println(library.books.javaClass) // 프록시 객체(PersistentBag) 출력

        flushAndClear()

        val savedLibrary = lbr.findAll().first()

        book.name = "changed"

        println(library === savedLibrary) // 참조가 같은 객체 (영속성 컨텍스트에서 @ID 필드를 기준으로 엔티티를 관리하고 DB 조회 시 동일한 객체로 매핑해줌)
        println(savedLibrary.books.first().name) // changed
    }

    @Test
    @DisplayName("둘 이상의 부모엔티티 -> 영속성 전이(REMOVE), 고아객체 옵션 적용x (나머지 영속성 전이 옵션은 사용해도 될듯)")
    fun test5() {
        val book = Book(name = "book")
        val library = Library(name = "library")
        val buyer = Buyer(name = "buyer")

        // 양쪽 참조를 다 설정해줘야 한다.
        library.addBooks(book)
        buyer.buyBooks(book)

        // 영속성 전이 PERSIST는 설정o
        em.persist(library) // library, book 영속화 및 insert 쿼리 발생 (book.buyer = null)
        em.persist(buyer) // book은 이미 영속화 되어있으므로 buyer에 대한 insert 쿼리만 발생

        // 영속성 컨텍스트와 DB가 일치하지 않지만 괜찮다. 어짜피 나중에 flush() 될 테니.

        // 양쪽 참조를 다 해제해줘야 한다.
        library.removeBooks(book)

        // 영속성 전이 REMOVE, orphanRemoval은 설정x
        em.remove(library)

        flushAndClear()

        // FK의 on delete 설정은 RESTRICT 이지만, nullable 이기 때문에 null은 할당 가능하다.
    }

    @Test
    @DisplayName("지연로딩으로 컬렉션 조건에 맞게 가져온 후 컬렉션에 접근하면 전체 컬렉션을 가져와버림")
    fun test6() {
        val books = listOf(Book(name = "book1"), Book(name = "book2"))
        val library = Library(name = "library")
        library.addBooks(*books.toTypedArray())
        lbr.save(library)
        br.saveAll(books)

        flushAndClear()

        // 일부 조회 (1개)
        val savedLibrary = lbr.findAllByBooksName(books.first().name).first()

        // 지연로딩 시 컬렉션 전체 다 가져옴 (2개)
        assertThat(savedLibrary.books.size).isEqualTo(2)

        // 컬렉션 요소 id 같은 것들을 따로 가지고 있지 않기 때문에 컬렉션에 접근한 순간 DB에서 전부 가져오게 됨
    }

    @Test
    @DisplayName("join fetch로 컬렉션 일부 조회 후 clear() 하면 조회한 일부 요소만 삭제함. 전체 요소 삭제 x")
    fun test7() {
        val students = listOf(Student(name = "student1"), Student(name = "student2"))
        val school = School(name = "school")
        school.enrollStudents(*students.toTypedArray())
        scr.save(school)

        flushAndClear()

        // 일부 조회 (1개)
        val savedLibrary = scr.findAllWithStudentsByStudentsName(students.first().name).first()

        // clear() 하면 가져온 만큼만 삭제
        savedLibrary.students.clear()

        flushAndClear()

        // 다시 조회해보면 삭제하지 않은 student2는 남아있음
        val savedBook = str.findAll().first()
        assertThat(savedBook.name).isEqualTo("student2")
    }

    @Test
    @DisplayName("N:M 지연로딩 시 N+M+1 쿼리가 발생한다.")
    fun test8() {
        val booksList = listOf(
            // library1
            listOf(
                Book(name = "book1"), // buyer1
                Book(name = "book2"), // buyer2
                Book(name = "book3"), // buyer3
            ),
            // library2
            listOf(
                Book(name = "book4"), // buyer4
                Book(name = "book5"), // buyer5
                Book(name = "book6"), // buyer6
            )
        )
        val libraries = listOf(Library(name = "library1"), Library(name = "library2"))
        for (i in 0 until booksList.size) {
            libraries[i].addBooks(*booksList[i].toTypedArray())
        }
        lbr.saveAll(libraries)

        val buyers = listOf(
            Buyer(name = "buyer1"),
            Buyer(name = "buyer2"),
            Buyer(name = "buyer3"),
            Buyer(name = "buyer4"),
            Buyer(name = "buyer5"),
            Buyer(name = "buyer6")
        )
        for (i in 0 until buyers.size) {
            val books = booksList.flatten()
            buyers[i].buyBooks(books[i])
        }
        byr.saveAll(buyers)

        flushAndClear()

        val savedLibraries = lbr.findAll()

        // 쿼리 2 + 6 + 1 = 9개
        // books에서는 각 요소의 bookId를 모르지만, buyer는 buyer의 id를 알고 있다.
//        savedLibraries.forEach { it.books.map { book -> book.buyer?.name } }
        savedLibraries.forEach { it.books.map { book -> book.buyer }.map { buyer -> buyer?.name } }
    }

    @Test
    @DisplayName("findAllWith~ 메서드는 미리 구현되어있지만, fetch join이 아니다. 따라서 직접 쿼리를 작성해야한다.")
    fun test9() {
        val students = listOf(Student(name = "student1"), Student(name = "student2"))
        val school = School(name = "school")
        school.enrollStudents(*students.toTypedArray())
        scr.save(school)

        flushAndClear()

        // 어짜피 지연로딩을 해도 join을 해야하기 때문에 그냥 웬만하면 fetch join으로 가져오도록 findAllWith~를 구현해서 쓰자
        val savedLibrary = scr.findAllWithStudentsByName(school.name).first()
        print(savedLibrary.students.first().name)
    }

    @Test
    @DisplayName("지연로딩으로 clear() 시 N+1 쿼리 발생. clear()를 쓰려면 fetch join을 쓰자.")
    fun test10() {
        val students = listOf(Student(name = "student"))
        val schools = listOf(School(name = "school1"), School(name = "school2"), School(name = "school3"))
        schools.forEach { it.enrollStudents(*students.toTypedArray()) }
        scr.saveAll(schools)

        flushAndClear()

        val savedSchools = scr.findAll()
        val savedStudents = str.findAll()
        savedSchools.forEach { it.kickStudents(*savedStudents.toTypedArray()) }

        flushAndClear()
    }
}
