package com.generation.pokemontournament.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pokemon_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PokemonType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 20)
    private String name;
    
    @Column(length = 7)
    private String color;
}