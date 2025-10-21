package com.jpastudy.oneway.onetomany.test2;

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
public class Team2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "team_id", nullable = false, updatable = false)
    private List<Player2> player2s = new ArrayList<>();

    public Team2(final String name) {
        this.name = name;
    }

    public void add(final Player2 player2) {
        player2s.add(player2);
    }

    public void remove(final Player2 player2) {
        player2s.remove(player2);
    }
}
