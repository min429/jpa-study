package com.jpastudy.twoway.manytoone.test3;

import org.hibernate.annotations.SoftDelete;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SoftDelete
public class Comment3 {

    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    @Setter(AccessLevel.PRIVATE)
    private Long id; // JPA가 id가 null인지 아닌지 판단하는 게 중요

    @Column(columnDefinition = "TEXT") // ~ 약 63000자
    private String body;

    // mappedBy를 사용하지 않은 쪽이 연관관계의 주인 // DB 반영은 관계의 주인쪽에서 해야함
    @ManyToOne(fetch = FetchType.LAZY) // default: FetchType.EAGER
    @JoinColumn(name = "post3_id", nullable = false)
    private Post3 post;
}
