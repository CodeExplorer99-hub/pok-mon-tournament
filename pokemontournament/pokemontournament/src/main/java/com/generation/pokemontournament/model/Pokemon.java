package com.generation.pokemontournament.model;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pokemon")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pokemon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pokedex_number", unique = true, nullable = false)
    private Integer pokedexNumber;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type1_id", nullable = false)
    private PokemonType type1;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type2_id")
    private PokemonType type2;
    
    @Column(name = "base_hp", nullable = false)
    private Integer baseHp;
    
    @Column(name = "base_attack", nullable = false)
    private Integer baseAttack;
    
    @Column(name = "base_defense", nullable = false)
    private Integer baseDefense;
    
    @Column(name = "base_speed", nullable = false)
    private Integer baseSpeed;
    
    @Column(name = "base_sp_attack", nullable = false)
    private Integer baseSpAttack;
    
    @Column(name = "base_sp_defense", nullable = false)
    private Integer baseSpDefense;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "pokemon_moves",
        joinColumns = @JoinColumn(name = "pokemon_id"),
        inverseJoinColumns = @JoinColumn(name = "move_id")
    )
    private List<Move> moves;
    
    // Metodo helper per calcolare il totale delle statistiche
    public Integer getTotalStats() {
        return baseHp + baseAttack + baseDefense + baseSpeed + baseSpAttack + baseSpDefense;
    }
    
}