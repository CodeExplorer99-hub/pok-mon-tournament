package com.generation.pokemontournament.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.generation.pokemontournament.dto.TurnResult;
import com.generation.pokemontournament.model.Move;
import com.generation.pokemontournament.model.Pokemon;
import com.generation.pokemontournament.repository.MoveRepository;
import com.generation.pokemontournament.repository.PokemonRepository;
import com.generation.pokemontournament.repository.PokemonTypeRepository;

@Service
public class PokemonService {

    private final PokemonRepository pokemonRepository;
    private final PokemonTypeRepository pokemonTypeRepository;
    private final MoveRepository moveRepository;

    public PokemonService(PokemonRepository pokemonRepository,
            PokemonTypeRepository pokemonTypeRepository,
            MoveRepository moveRepository) {
        this.pokemonRepository = pokemonRepository;
        this.pokemonTypeRepository = pokemonTypeRepository;
        this.moveRepository = moveRepository;
    }

    // ===== TABELLA TIPI =====

    private static final Map<String, List<String>> SUPER_EFFECTIVE    = new HashMap<>();
    private static final Map<String, List<String>> NOT_VERY_EFFECTIVE = new HashMap<>();
    private static final Map<String, List<String>> NO_EFFECT          = new HashMap<>();

    // Tipi speciali (usano Sp.Attack / Sp.Defense)
    private static final Set<String> SPECIAL_TYPES = new HashSet<>(Arrays.asList(
            "Fire", "Water", "Grass", "Electric", "Ice", "Psychic", "Dragon", "Dark", "Fairy"
    ));

    static {
        // ===== SUPER EFFICACE (2x) =====
        SUPER_EFFECTIVE.put("Fire",     Arrays.asList("Grass", "Ice", "Bug", "Steel"));
        SUPER_EFFECTIVE.put("Water",    Arrays.asList("Fire", "Ground", "Rock"));
        SUPER_EFFECTIVE.put("Electric", Arrays.asList("Water", "Flying"));
        SUPER_EFFECTIVE.put("Grass",    Arrays.asList("Water", "Ground", "Rock"));
        SUPER_EFFECTIVE.put("Ice",      Arrays.asList("Grass", "Ground", "Flying", "Dragon"));
        SUPER_EFFECTIVE.put("Fighting", Arrays.asList("Normal", "Ice", "Rock", "Dark", "Steel"));
        SUPER_EFFECTIVE.put("Poison",   Arrays.asList("Grass", "Fairy"));
        SUPER_EFFECTIVE.put("Ground",   Arrays.asList("Fire", "Electric", "Poison", "Rock", "Steel"));
        SUPER_EFFECTIVE.put("Flying",   Arrays.asList("Grass", "Fighting", "Bug"));
        SUPER_EFFECTIVE.put("Psychic",  Arrays.asList("Fighting", "Poison"));
        SUPER_EFFECTIVE.put("Bug",      Arrays.asList("Grass", "Psychic", "Dark"));
        SUPER_EFFECTIVE.put("Rock",     Arrays.asList("Fire", "Ice", "Flying", "Bug"));
        SUPER_EFFECTIVE.put("Ghost",    Arrays.asList("Psychic", "Ghost"));
        SUPER_EFFECTIVE.put("Dragon",   Arrays.asList("Dragon"));
        SUPER_EFFECTIVE.put("Dark",     Arrays.asList("Psychic", "Ghost"));
        SUPER_EFFECTIVE.put("Steel",    Arrays.asList("Ice", "Rock", "Fairy"));
        SUPER_EFFECTIVE.put("Fairy",    Arrays.asList("Fighting", "Dragon", "Dark"));

        // ===== NON MOLTO EFFICACE (0.5x) =====
        NOT_VERY_EFFECTIVE.put("Fire",     Arrays.asList("Fire", "Water", "Rock", "Dragon"));
        NOT_VERY_EFFECTIVE.put("Water",    Arrays.asList("Water", "Grass", "Dragon"));
        NOT_VERY_EFFECTIVE.put("Electric", Arrays.asList("Electric", "Grass", "Dragon"));
        NOT_VERY_EFFECTIVE.put("Grass",    Arrays.asList("Fire", "Grass", "Poison", "Flying", "Bug", "Dragon", "Steel"));
        NOT_VERY_EFFECTIVE.put("Ice",      Arrays.asList("Fire", "Water", "Ice", "Steel"));
        NOT_VERY_EFFECTIVE.put("Fighting", Arrays.asList("Poison", "Flying", "Psychic", "Bug", "Fairy"));
        NOT_VERY_EFFECTIVE.put("Poison",   Arrays.asList("Poison", "Ground", "Rock", "Ghost"));
        NOT_VERY_EFFECTIVE.put("Ground",   Arrays.asList("Grass", "Bug"));
        NOT_VERY_EFFECTIVE.put("Flying",   Arrays.asList("Electric", "Rock", "Steel"));
        NOT_VERY_EFFECTIVE.put("Psychic",  Arrays.asList("Psychic", "Steel"));
        NOT_VERY_EFFECTIVE.put("Bug",      Arrays.asList("Fire", "Fighting", "Poison", "Flying", "Ghost", "Steel", "Fairy"));
        NOT_VERY_EFFECTIVE.put("Rock",     Arrays.asList("Fighting", "Ground", "Steel"));
        NOT_VERY_EFFECTIVE.put("Ghost",    Arrays.asList("Dark"));
        NOT_VERY_EFFECTIVE.put("Dragon",   Arrays.asList("Steel"));
        NOT_VERY_EFFECTIVE.put("Dark",     Arrays.asList("Fighting", "Dark", "Fairy"));
        NOT_VERY_EFFECTIVE.put("Steel",    Arrays.asList("Fire", "Water", "Electric", "Steel"));
        NOT_VERY_EFFECTIVE.put("Fairy",    Arrays.asList("Fire", "Poison", "Steel"));

        // ===== IMMUNE / NESSUN EFFETTO (0x) =====
        NO_EFFECT.put("Normal",   Arrays.asList("Ghost"));
        NO_EFFECT.put("Electric", Arrays.asList("Ground"));
        NO_EFFECT.put("Fighting", Arrays.asList("Ghost"));
        NO_EFFECT.put("Poison",   Arrays.asList("Steel"));
        NO_EFFECT.put("Ground",   Arrays.asList("Flying"));
        NO_EFFECT.put("Psychic",  Arrays.asList("Dark"));
        NO_EFFECT.put("Ghost",    Arrays.asList("Normal"));
        NO_EFFECT.put("Dragon",   Arrays.asList("Fairy"));
    }

    // ===== REPOSITORY =====

    public List<Pokemon> getAllPokemon() {
        return pokemonRepository.findAll();
    }

    public Optional<Pokemon> getPokemonById(Long id) {
        return pokemonRepository.findById(id);
    }

    public Optional<Move> getMoveById(Long id) {
        return moveRepository.findById(id);
    }

    // ===== TIPO: EFFICACIA =====

    public double getTypeEffectiveness(String attackType, String defenseType) {
        if (NO_EFFECT.containsKey(attackType) &&
                NO_EFFECT.get(attackType).contains(defenseType)) {
            return 0.0;
        }
        if (SUPER_EFFECTIVE.containsKey(attackType) &&
                SUPER_EFFECTIVE.get(attackType).contains(defenseType)) {
            return 2.0;
        }
        if (NOT_VERY_EFFECTIVE.containsKey(attackType) &&
                NOT_VERY_EFFECTIVE.get(attackType).contains(defenseType)) {
            return 0.5;
        }
        return 1.0;
    }

    // ===== STAB (Same-Type Attack Bonus) =====

    /**
     * Ritorna 1.5 se il tipo della mossa coincide con type1 O type2 del Pokemon attaccante.
     */
    public double getStab(Pokemon attacker, Move move) {
        String moveType = move.getType().getName();
        if (attacker.getType1().getName().equalsIgnoreCase(moveType)) {
            return 1.5;
        }
        if (attacker.getType2() != null &&
                attacker.getType2().getName().equalsIgnoreCase(moveType)) {
            return 1.5;
        }
        return 1.0;
    }

    // ===== CATEGORIA MOSSA: SPECIALE o FISICO =====

    /**
     * I tipi Fire, Water, Grass, Electric, Ice, Psychic, Dragon, Dark, Fairy
     * usano Attacco Speciale / Difesa Speciale.
     * Tutti gli altri (Normal, Fighting, Flying, Poison, Ground, Rock, Bug, Ghost, Steel)
     * usano Attacco Fisico / Difesa Fisica.
     */
    private boolean isSpecialMove(String moveType) {
        return SPECIAL_TYPES.contains(moveType);
    }

    // ===== ESEGUI TURNO (metodo principale, usato dal controller) =====

    /**
     * Esegue un singolo attacco con una mossa specifica.
     * Applica: categoria fisica/speciale, accuratezza, tipo, STAB, bonus velocità.
     *
     * @return TurnResult con danno, se ha mancato, etichetta efficacia e categoria
     */
    public TurnResult executeTurnResult(Pokemon attacker, Pokemon defender, Move move) {

        // Mossa di stato (potenza 0): nessun danno
        if (move.getPower() == 0) {
            return new TurnResult(0, false, "normal", "status");
        }

        // Controllo accuratezza: accuracy <= 0 oppure >= 100 significa sempre colpisce
        int acc = move.getAccuracy();
        if (acc > 0 && acc < 100) {
            int roll = (int) (Math.random() * 100) + 1; // 1-100
            if (roll > acc) {
                return new TurnResult(0, true, "normal", isSpecialMove(move.getType().getName()) ? "special" : "physical");
            }
        }

        // Scegli le statistiche in base alla categoria della mossa
        String moveType = move.getType().getName();
        boolean isSpecial = isSpecialMove(moveType);
        String category = isSpecial ? "special" : "physical";

        int attackStat  = isSpecial ? attacker.getBaseSpAttack()  : attacker.getBaseAttack();
        int defenseStat = isSpecial ? defender.getBaseSpDefense() : defender.getBaseDefense();

        // Danno base
        double baseDamage = ((double) attackStat / Math.max(1, defenseStat)) * move.getPower();

        // Efficacia tipo
        double eff1 = getTypeEffectiveness(moveType, defender.getType1().getName());
        double eff2 = (defender.getType2() != null)
                ? getTypeEffectiveness(moveType, defender.getType2().getName())
                : 1.0;
        double totalEffectiveness = eff1 * eff2;

        // STAB
        double stab = getStab(attacker, move);

        // Bonus velocità: chi è più veloce infligge il 10% in più
        double speedModifier = (attacker.getBaseSpeed() > defender.getBaseSpeed()) ? 1.1 : 1.0;

        // Danno finale
        int finalDamage;
        if (totalEffectiveness == 0.0) {
            finalDamage = 0;
        } else {
            finalDamage = Math.max(1, (int) Math.round(
                    baseDamage * totalEffectiveness * stab * speedModifier
            ));
        }

        // Etichetta efficacia
        String effectivenessLabel;
        if      (totalEffectiveness == 0.0) effectivenessLabel = "immune";
        else if (totalEffectiveness > 1.0)  effectivenessLabel = "super";
        else if (totalEffectiveness < 1.0)  effectivenessLabel = "notVery";
        else                                effectivenessLabel = "normal";

        return new TurnResult(finalDamage, false, effectivenessLabel, category);
    }

    // ===== DANNO CON MOSSA (compatibilità con calculateDamageWithMove) =====

    /**
     * Mantiene compatibilità col codice esistente.
     * Usa executeTurnResult internamente.
     */
    public int calculateDamageWithMove(Pokemon attacker, Pokemon defender, Move move) {
        return executeTurnResult(attacker, defender, move).getDamage();
    }

    // ===== BATTAGLIA SIMULATA (endpoint /api/battle) =====

    /**
     * Simula una battaglia completa scegliendo mosse casuali ad ogni turno.
     * L'ordine di attacco dipende dalla velocità.
     */
    public Pokemon simulateBattle(Pokemon pokemon1, Pokemon pokemon2) {
        int hp1 = pokemon1.getBaseHp();
        int hp2 = pokemon2.getBaseHp();

        boolean pokemon1First = pokemon1.getBaseSpeed() >= pokemon2.getBaseSpeed();

        while (hp1 > 0 && hp2 > 0) {

            Pokemon first  = pokemon1First ? pokemon1 : pokemon2;
            Pokemon second = pokemon1First ? pokemon2 : pokemon1;
            int[] hpRef    = pokemon1First ? new int[]{hp1, hp2} : new int[]{hp2, hp1};

            // Primo attaccante
            Move move1 = getRandomMove(first);
            int dmg1 = (move1 != null)
                    ? executeTurnResult(first, second, move1).getDamage()
                    : calculateDamage(first, second);
            hpRef[1] -= dmg1;

            System.out.printf("%s usa %s su %s per %d danni! HP rimanenti: %d%n",
                    first.getName(), move1 != null ? move1.getName() : "Attacco",
                    second.getName(), dmg1, hpRef[1]);

            if (hpRef[1] <= 0) {
                System.out.println(second.getName() + " è KO! " + first.getName() + " vince!");
                return first;
            }

            // Secondo attaccante
            Move move2 = getRandomMove(second);
            int dmg2 = (move2 != null)
                    ? executeTurnResult(second, first, move2).getDamage()
                    : calculateDamage(second, first);
            hpRef[0] -= dmg2;

            System.out.printf("%s usa %s su %s per %d danni! HP rimanenti: %d%n",
                    second.getName(), move2 != null ? move2.getName() : "Attacco",
                    first.getName(), dmg2, hpRef[0]);

            if (hpRef[0] <= 0) {
                System.out.println(first.getName() + " è KO! " + second.getName() + " vince!");
                return second;
            }

            if (pokemon1First) { hp1 = hpRef[0]; hp2 = hpRef[1]; }
            else               { hp2 = hpRef[0]; hp1 = hpRef[1]; }
        }

        return hp1 > 0 ? pokemon1 : pokemon2;
    }

    /** Mossa casuale dal pool del Pokemon; null se non ha mosse. */
    private Move getRandomMove(Pokemon pokemon) {
        if (pokemon.getMoves() == null || pokemon.getMoves().isEmpty()) return null;
        int idx = (int) (Math.random() * pokemon.getMoves().size());
        return pokemon.getMoves().get(idx);
    }

    // ===== DANNO SENZA MOSSA (fallback per simulateBattle) =====

    private int calculateDamage(Pokemon attacker, Pokemon defender) {
        int baseDamage = attacker.getBaseAttack() - defender.getBaseDefense();
        if (baseDamage <= 0) baseDamage = 5;

        String attackerType = attacker.getType1().getName();
        double eff1 = getTypeEffectiveness(attackerType, defender.getType1().getName());
        double eff2 = (defender.getType2() != null)
                ? getTypeEffectiveness(attackerType, defender.getType2().getName())
                : 1.0;
        double totalEffectiveness = eff1 * eff2;

        double damage = baseDamage * totalEffectiveness;
        if (attacker.getBaseSpeed() > defender.getBaseSpeed()) damage *= 1.1;

        return Math.max(1, (int) Math.round(damage));
    }
}
