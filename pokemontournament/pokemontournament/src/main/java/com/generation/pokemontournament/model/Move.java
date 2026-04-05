package com.generation.pokemontournament.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "move")
@Data
@NoArgsConstructor       //djdcbdbcbcdbcbdjc
@AllArgsConstructor
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id", nullable = false)
    private PokemonType type;

    @Column(nullable = false)
    private Integer power;

    @Column(nullable = false)
    private Integer accuracy;
}