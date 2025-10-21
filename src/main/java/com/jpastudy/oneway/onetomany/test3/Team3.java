package com.jpastudy.oneway.onetomany.test3;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class Team3 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "team_id", nullable = false, updatable = false)
    private List<Player3> player3s = new ArrayList<>();

    public Team3(final String name) {
        this.name = name;
    }

    public void add(final Player3 player3) {
        player3s.add(player3);
    }

    public void remove(final Player3 player3) {
        player3s.remove(player3);
    }
}
