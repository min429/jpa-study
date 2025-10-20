package com.jpa.study.twoway.manytoone.test2;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Comment2 {

    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    @Setter(AccessLevel.PRIVATE)
    private Long id; // JPA가 id가 null인지 아닌지 판단하는 게 중요

    @Column(columnDefinition = "TEXT") // ~ 약 63000자
    private String body;

    // mappedBy를 사용하지 않은 쪽이 연관관계의 주인 // DB 반영은 관계의 주인쪽에서 해야함
    @ManyToOne(fetch = FetchType.LAZY) // default: FetchType.EAGER
    @JoinColumn(name = "post2_id", nullable = false)
    private Post2 post;
}
