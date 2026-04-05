package com.generation.pokemontournament.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generation.pokemontournament.dto.TurnResult;
import com.generation.pokemontournament.model.Pokemon;
import com.generation.pokemontournament.service.PokemonService;

/**
 * Controller REST per la gestione dei Pokemon e delle battaglie.
 */
@RestController
@RequestMapping("/api")
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    /**
     * Restituisce la lista di tutti i Pokemon.
     * GET /api/pokemon
     */
    @GetMapping("/pokemon")
    public List<Pokemon> getAllPokemon() {
        return pokemonService.getAllPokemon();
    }

    /**
     * Restituisce un singolo Pokemon per ID.
     * GET /api/pokemon/{id}
     */
    @GetMapping("/pokemon/{id}")
    public ResponseEntity<Pokemon> getPokemonById(@PathVariable Long id) {
        return pokemonService.getPokemonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Simula una battaglia completa tra due Pokemon e restituisce il vincitore.
     * POST /api/battle
     * Body: {"pokemon1Id": 1, "pokemon2Id": 2}
     */
    @PostMapping("/battle")
    public ResponseEntity<Pokemon> battle(@RequestBody Map<String, Long> request) {
        Long id1 = request.get("pokemon1Id");
        Long id2 = request.get("pokemon2Id");

        var pokemon1 = pokemonService.getPokemonById(id1);
        var pokemon2 = pokemonService.getPokemonById(id2);

        if (pokemon1.isEmpty() || pokemon2.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Pokemon winner = pokemonService.simulateBattle(pokemon1.get(), pokemon2.get());
        return ResponseEntity.ok(winner);
    }

    /**
     * Esegue un singolo attacco in un turno di battaglia.
     * POST /api/battle/turn
     * Body: {"attackerId": 1, "defenderId": 2, "moveId": 5}
     *
     * Risposta: {
     *   "damage":        int,
     *   "missed":        boolean,
     *   "effectiveness": "normal" | "super" | "notVery" | "immune",
     *   "category":      "physical" | "special" | "status",
     *   "moveName":      String,
     *   "attackerName":  String,
     *   "defenderName":  String
     * }
     */
    @PostMapping("/battle/turn")
    public ResponseEntity<Map<String, Object>> battleTurn(@RequestBody Map<String, Long> request) {

        Long attackerId = request.get("attackerId");
        Long defenderId = request.get("defenderId");
        Long moveId     = request.get("moveId");

        var attacker = pokemonService.getPokemonById(attackerId);
        var defender = pokemonService.getPokemonById(defenderId);
        var move     = pokemonService.getMoveById(moveId);

        if (attacker.isEmpty() || defender.isEmpty() || move.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TurnResult result = pokemonService.executeTurnResult(
                attacker.get(), defender.get(), move.get()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("damage",        result.getDamage());
        response.put("missed",        result.isMissed());
        response.put("effectiveness", result.getEffectiveness());
        response.put("category",      result.getCategory());
        response.put("moveName",      move.get().getName());
        response.put("attackerName",  attacker.get().getName());
        response.put("defenderName",  defender.get().getName());

        return ResponseEntity.ok(response);
    }
}
