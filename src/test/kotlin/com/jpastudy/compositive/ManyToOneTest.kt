package com.jpastudy.compositive

import jakarta.persistence.EntityManager
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
    lateinit var t6r: TRepository

    @Test
    @DisplayName("library.books 조회 가능")
    fun test2() {
        val books = listOf(Book(name = "book1"), Book(name = "book2"))
        val library = Library(name = "library")
        library.addBooks(*books.toTypedArray())

        // TODO: 영속성 전이 없애고 save 둘다 호출하는거 좀 별로인듯 -> test5에서 해결
        lbr.save(library)
        br.saveAll(books)

        println(lbr.findBooksByLibraryName("library"))
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

        println(lbr.findBooksByLibraryName("library"))
    }

    @Test
    @DisplayName("findAll()은 즉시 DB를 조회하고 영속성 컨텍스트와 동기화한다.")
    fun test4() {
        val book = Book(name = "book")
        val library = Library(name = "library")
        library.addBooks(book)

        lbr.save(library) // @GeneratedValue를 쓰는 경우, persist/merge 시 books가 프록시객체(PersistentBag)으로 감싸짐
        println(library.books.javaClass) // 프록시 객체(PersistentBag) 출력

        em.clear()

        val savedLibrary = lbr.findAll().first()

        book.name = "changed"

        println(library === savedLibrary) // 참조가 같은 객체 (영속성 컨텍스트에서 @ID 필드를 기준으로 엔티티를 관리하고 DB 조회 시 동일한 객체로 매핑해줌)
        println(savedLibrary.books.first().name) // changed

        em.clear()
    }

    @Test
    @DisplayName("둘 이상의 부모엔티티 -> 영속성 전이(REMOVE), 고아객체 옵션 적용x")
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

        em.flush()
        em.clear()

        // FK의 on delete 설정은 RESTRICT 이지만, nullable 이기 때문에 null은 할당 가능하다.
    }
}
