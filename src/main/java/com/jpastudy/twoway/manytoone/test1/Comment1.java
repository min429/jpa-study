package com.jpastudy.twoway.manytoone.test1;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment1 {

    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    @Setter(AccessLevel.PRIVATE)
    private Long id; // JPA가 id가 null인지 아닌지 판단하는 게 중요

    @Column(columnDefinition = "TEXT") // ~ 약 63000자
    private String body;

    // mappedBy를 사용하지 않은 쪽이 연관관계의 주인 // DB 반영은 관계의 주인쪽에서 해야함
    @ManyToOne(fetch = FetchType.LAZY) // default: FetchType.EAGER
    @JoinColumn(name = "post1_id", nullable = false)
    private Post1 post;
}
