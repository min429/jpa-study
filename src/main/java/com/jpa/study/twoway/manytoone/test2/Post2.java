package com.jpa.study.twoway.manytoone.test2;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@BatchSize(size = 2) // posts를 배치사이즈만큼 가져올 때 필요
public class Post2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "TEXT") // ~ 약 63000자
    private String body;

    /**
     * <영속성 전이(Cascade)와 고아객체>
     * 1. 부모 엔티티와 자식 엔티티의 연관관계가 형성이 된 경우에 작동한다.
     * 2. 부모 엔티티에 자식 엔티티를 추가하는 것은 부모 엔티티에서 자식 엔티티를 관리한다는 것을 명시적으로 의미한다.
     * -> 영속성 전이를 통해 자식 엔티티를 영속성 컨텍스트에 추가한다. -> DB에 반영된다.
     * 3. 부모 엔티티에서 자식 엔티티를 삭제하는 것은 부모 엔티티에서 자식 엔티티를 관리하지 않는다는 것을 명시적으로 의미한다.
     * 해당 엔티티 자체를 삭제하겠다는 의미가 아니다. (다른 부모와 다시 연결하거나 부모 없이 쓸 수 있기 때문)
     * 4. 부모 엔티티에서 자식 엔티티를 삭제하면 그 자식 엔티티는 고아 객체라고 볼 수 있다.
     * 5. 고아 객체를 그대로 냅둘 건지, 삭제할 건지를 선택하려면 orphanRemoval을 설정한다.
     * 6. 결론: 부모 엔티티에서 자식 엔티티를 관리하고 싶은 경우, 자식 엔티티를 추가할 때는 CascadeType.Persist, 삭제할 때는 orphanRemoval = true를 설정한다.
     * 부모 엔티티 자체가 삭제된 경우에 자식 엔티티를 삭제하고 싶은 경우, CascadeType.Remove를 추가한다.
     */

    // cascade.ALL: 영속성 전이 -> Post를 영속화할 때 연관된 엔티티도 영속화한다.
    // orphanRemoval = true: 부모객체(Post)에서 자식객체를 제거(comments.remove(comment))하면 comment는 고아객체가 됨 -> 트랜잭션 종료될 때 제거됨
    // cascade.ALL, orphanRemoval = true -> 자식 엔티티의 생명주기를 부모 엔티티에 종속시킴
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Builder를 사용할 때 초기값(빈 리스트)을 지정하기 위해 사용
    @BatchSize(size = 2)
    private List<Comment2> comments = new ArrayList<>(); // 배치사이즈만큼의 posts가 가진 comments를 한번에 조회하기 위해 필요

    public void addComment(Comment2 comment) {
        // 둘다 있어야 연관관계가 형성돼서 영속성 전이가 발생함
        comments.add(comment);
        comment.setPost(this); // DB 반영은 연관관계의 주인 쪽에서만 가능하다.
    }

    public void removeComment(Comment2 comment) {
        // comments에서 comment를 지워도 comment 쪽에서는 아직 부모 엔티티 참조를 가지고 있음
        // 영속성 전이와는 상관없음
        comments.remove(comment);
    }
}
