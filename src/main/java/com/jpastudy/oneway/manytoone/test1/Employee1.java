package com.jpastudy.oneway.manytoone.test1;

import org.hibernate.annotations.SoftDelete;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
@SoftDelete
public class Employee1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long number;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department1_id", nullable = false)
    private Department1 department;

    public Employee1(final String name, final Long number, final Department1 department) {
        this.name = name;
        this.number = number;
        this.department = department;
    }
}
