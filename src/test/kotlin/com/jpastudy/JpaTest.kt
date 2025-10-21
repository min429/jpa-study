package com.jpastudy

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
//        println("savedCeo class: ${savedCmp.ceo?.javaClass}") // real object (Ceo를 먼저 조회해야 Company를 알 수 있어서 Ceo는 무조건 즉시로딩됨)
    }

    @Test
    @DisplayName("library.books 조회 가능")
    fun test2() {
        val books = listOf(Book(name = "book1"), Book(name = "book2"))
        val library = Library(name = "library")
        library.addBooks(books)

        // TODO: 영속성 전이 없애고 save 둘다 호출하는거 좀 별로인듯
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
        library.addBook(book)
        library.hire(manager)

        lbr.save(library)

        println(lbr.findBooksByLibraryName("library"))
    }
}