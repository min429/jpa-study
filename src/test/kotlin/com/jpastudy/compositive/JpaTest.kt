package com.jpastudy.compositive

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityManager
import jakarta.persistence.Id
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@SpringBootTest
@Transactional
class JpaTest {

    @Autowired
    lateinit var em: EntityManager

    @Autowired
    lateinit var cmpr: CompanyRepository

    @Autowired
    lateinit var ceor: CeoRepository

    @Autowired
    lateinit var br: BookRepository

    @Autowired
    lateinit var lbr: LibraryRepository

    @Autowired
    lateinit var byr: BuyerRepository

    @Autowired
    lateinit var t6r: Test6Repository

    @Test
    @DisplayName("객체참조는 영속성 전이와 별도로 직접 설정해줘야 한다.")
    fun test1() {
        val company = Company(name = "company")
        val ceo = Ceo(name = "ceo")
        company.addCustomer(ceo) // 양쪽 객체 참조 설정

        em.persist(company) // repository.save()에서도 내부적으로 호출함
        em.flush()
        em.clear()

        val savedCeo = ceor.findByName("ceo")
        println("savedCmp class: ${savedCeo.company?.javaClass}") // proxy object

//        val savedCmp = cmpr.findByName("company")
//        println("savedCeo class: ${savedCmp.ceo?.javaClass}") // real object (JPA 스펙 상 OneToOne에서 연관관계 주인 엔티티(FK를 가진 엔티티)는 무조건 즉시로딩됨)
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

    @Test
    @DisplayName("merge vs persist")
    fun test6() {
        val test = Test6(1, "test")

        t6r.save(test) // id가 있으므로 merge 된다.
        test.name = "changed" // merge인 경우 컨텍스트에 반영되지 않는다.

        em.flush()
        em.clear()

        println("name: ${t6r.findById(1).get().name}")

        // merge(entity)는 entity를 shallow copy한 객체를 영속성 컨텍스트에 저장 및 return 하고,
        // persist(entity)는 entity를 영속성 컨텍스트에 저장 및 return 한다.
    }
}

@Entity
class Test6(
    @Id
    var id: Long? = null, // GenerationType 지정x

    @Column(unique = true)
    var name: String
)

interface Test6Repository : JpaRepository<Test6, Long>