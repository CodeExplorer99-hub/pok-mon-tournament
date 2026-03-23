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

import com.generation.pokemontournament.model.Pokemon;
import com.generation.pokemontournament.service.PokemonService;

/**
 * Controller REST per la gestione dei Pokemon e delle battaglie.
 * Espone le API che il frontend chiamerà.
 */
@RestController
@RequestMapping("/api")
public class PokemonController {

    /**
     * Spring crea e inietta automaticamente il Service.
     * Non dobbiamo fare new PokemonService() manualmente.
     */
    @Autowired
    private PokemonService pokemonService;

    /**
     * Restituisce la lista di tutti i Pokemon nel database.
     * GET http://localhost:8080/api/pokemon
     */
    @GetMapping("/pokemon")
    public List<Pokemon> getAllPokemon() {
        return pokemonService.getAllPokemon();
    }

    /**
     * Restituisce un singolo Pokemon cercandolo per ID.
     * GET http://localhost:8080/api/pokemon/1
     * @param id - preso direttamente dall'URL
     */
    @GetMapping("/pokemon/{id}")
    public ResponseEntity<Pokemon> getPokemonById(@PathVariable Long id) {
        return pokemonService.getPokemonById(id)
                .map(ResponseEntity::ok)                    // trovato → 200 OK
                .orElse(ResponseEntity.notFound().build()); // non trovato → 404
    }

    /**
     * Simula una battaglia tra due Pokemon e restituisce il vincitore.
     * POST http://localhost:8080/api/battle
     * Body JSON: {"pokemon1Id": 1, "pokemon2Id": 2}
     */
    @PostMapping("/battle")
    public ResponseEntity<Pokemon> battle(@RequestBody Map<String, Long> request) {
        
        // Estrae gli ID dal JSON ricevuto
        Long id1 = request.get("pokemon1Id");
        Long id2 = request.get("pokemon2Id");

        // Cerca i due Pokemon nel database (Optional = potrebbero non esistere)
        var pokemon1 = pokemonService.getPokemonById(id1);
        var pokemon2 = pokemonService.getPokemonById(id2);

        // Se uno dei due non esiste, risponde con 404
        if (pokemon1.isEmpty() || pokemon2.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Simula la battaglia e ottiene il vincitore
        Pokemon winner = pokemonService.simulateBattle(pokemon1.get(), pokemon2.get());
        
        // Restituisce il vincitore con 200 OK
        return ResponseEntity.ok(winner);
    }
    /**
     * Esegue un singolo turno di battaglia.
     * POST http://localhost:8080/api/battle/turn
     * Body JSON: {"attackerId": 1, "defenderId": 2, "moveId": 5}
     */
    @PostMapping("/battle/turn")
    public ResponseEntity<Map<String, Object>> battleTurn(@RequestBody Map<String, Long> request) {

        Long attackerId = request.get("attackerId");
        Long defenderId = request.get("defenderId");
        Long moveId = request.get("moveId");

        // Cerca attaccante, difensore e mossa nel database
        var attacker = pokemonService.getPokemonById(attackerId);
        var defender = pokemonService.getPokemonById(defenderId);
        var move = pokemonService.getMoveById(moveId);

        // Se uno dei tre non esiste risponde con 404
        if (attacker.isEmpty() || defender.isEmpty() || move.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Calcola il danno
        int damage = pokemonService.calculateDamageWithMove(
            attacker.get(), defender.get(), move.get()
        );

        // Costruisce la risposta con danno e info sulla mossa
        Map<String, Object> response = new HashMap<>();
        response.put("damage", damage);
        response.put("moveName", move.get().getName());
        response.put("attackerName", attacker.get().getName());
        response.put("defenderName", defender.get().getName());

        return ResponseEntity.ok(response);
    }
}