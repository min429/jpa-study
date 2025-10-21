package com.jpastudy.lock;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Setter(value = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Board2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String body;

    @Setter(value = AccessLevel.PUBLIC)
    @Builder.Default
    private Long likes = 0L;

    @Version
    private Long version;
}
