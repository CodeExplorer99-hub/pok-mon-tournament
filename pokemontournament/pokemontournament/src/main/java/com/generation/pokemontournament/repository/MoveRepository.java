package com.generation.pokemontournament.repository;

import com.generation.pokemontournament.model.Move;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveRepository extends JpaRepository<Move, Long> {
}