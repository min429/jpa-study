package com.jpastudy.oneway.onetomany.test1;

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
public class Team1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "team_id")
    private List<Player1> player1s = new ArrayList<>();

    public Team1(final String name) {
        this.name = name;
    }

    public void add(final Player1 player1) {
        player1s.add(player1);
    }

    public void remove(final Player1 player1) {
        player1s.remove(player1);
    }
}
