package com.generation.pokemontournament.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.generation.pokemontournament.model.Pokemon;

import java.util.List;
import java.util.Optional;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Long> {
    
    // Trova un Pokemon per numero Pokedex
    Optional<Pokemon> findByPokedexNumber(Integer pokedexNumber);
    
    // Trova un Pokemon per nome
    Optional<Pokemon> findByName(String name);
    
    // Trova Pokemon per tipo (type1)
    List<Pokemon> findByType1Id(Long typeId);
    
    // Trova Pokemon ordinati per statistiche totali
    List<Pokemon> findAllByOrderByBaseHpDesc();
}