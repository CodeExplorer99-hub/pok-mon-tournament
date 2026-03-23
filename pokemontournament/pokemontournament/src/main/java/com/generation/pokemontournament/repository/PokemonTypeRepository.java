package com.generation.pokemontournament.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.generation.pokemontournament.model.PokemonType;

import java.util.Optional;

@Repository
public interface PokemonTypeRepository extends JpaRepository<PokemonType, Long> {
    
    // Trova un tipo per nome (es: "Fire", "Water")
    Optional<PokemonType> findByName(String name);
}