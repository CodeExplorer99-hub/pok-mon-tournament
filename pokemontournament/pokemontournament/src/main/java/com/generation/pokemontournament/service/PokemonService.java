package com.generation.pokemontournament.service;

import java.util.Arrays;
import com.generation.pokemontournament.model.Move;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

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

	
	
	private static final  Map<String, List<String>> SUPER_EFFECTIVE = new HashMap<>();
	
	private static final  Map<String, List<String>> NOT_VERY_EFFECTIVE = new HashMap<>();
	
	private static final  Map<String, List<String>> NO_EFFECT = new HashMap<>();
	
	
	
	static {
	    // ===== SUPER EFFICACE (2x) =====
	    SUPER_EFFECTIVE.put("Fire", Arrays.asList("Grass", "Ice", "Bug", "Steel"));
	    SUPER_EFFECTIVE.put("Water", Arrays.asList("Fire", "Ground", "Rock"));
	    SUPER_EFFECTIVE.put("Electric", Arrays.asList("Water", "Flying"));
	    SUPER_EFFECTIVE.put("Grass", Arrays.asList("Water", "Ground", "Rock"));
	    SUPER_EFFECTIVE.put("Ice", Arrays.asList("Grass", "Ground", "Flying", "Dragon"));
	    SUPER_EFFECTIVE.put("Fighting", Arrays.asList("Normal", "Ice", "Rock", "Dark", "Steel"));
	    SUPER_EFFECTIVE.put("Poison", Arrays.asList("Grass", "Fairy"));
	    SUPER_EFFECTIVE.put("Ground", Arrays.asList("Fire", "Electric", "Poison", "Rock", "Steel"));
	    SUPER_EFFECTIVE.put("Flying", Arrays.asList("Grass", "Fighting", "Bug"));
	    SUPER_EFFECTIVE.put("Psychic", Arrays.asList("Fighting", "Poison"));
	    SUPER_EFFECTIVE.put("Bug", Arrays.asList("Grass", "Psychic", "Dark"));
	    SUPER_EFFECTIVE.put("Rock", Arrays.asList("Fire", "Ice", "Flying", "Bug"));
	    SUPER_EFFECTIVE.put("Ghost", Arrays.asList("Psychic", "Ghost"));
	    SUPER_EFFECTIVE.put("Dragon", Arrays.asList("Dragon"));
	    SUPER_EFFECTIVE.put("Dark", Arrays.asList("Psychic", "Ghost"));
	    SUPER_EFFECTIVE.put("Steel", Arrays.asList("Ice", "Rock", "Fairy"));
	    SUPER_EFFECTIVE.put("Fairy", Arrays.asList("Fighting", "Dragon", "Dark"));
	    
	    // ===== NON MOLTO EFFICACE (0.5x) =====
	    NOT_VERY_EFFECTIVE.put("Fire", Arrays.asList("Fire", "Water", "Rock", "Dragon"));
	    NOT_VERY_EFFECTIVE.put("Water", Arrays.asList("Water", "Grass", "Dragon"));
	    NOT_VERY_EFFECTIVE.put("Electric", Arrays.asList("Electric", "Grass", "Dragon"));
	    NOT_VERY_EFFECTIVE.put("Grass", Arrays.asList("Fire", "Grass", "Poison", "Flying", "Bug", "Dragon", "Steel"));
	    NOT_VERY_EFFECTIVE.put("Ice", Arrays.asList("Fire", "Water", "Ice", "Steel"));
	    NOT_VERY_EFFECTIVE.put("Fighting", Arrays.asList("Poison", "Flying", "Psychic", "Bug", "Fairy"));
	    NOT_VERY_EFFECTIVE.put("Poison", Arrays.asList("Poison", "Ground", "Rock", "Ghost"));
	    NOT_VERY_EFFECTIVE.put("Ground", Arrays.asList("Grass", "Bug"));
	    NOT_VERY_EFFECTIVE.put("Flying", Arrays.asList("Electric", "Rock", "Steel"));
	    NOT_VERY_EFFECTIVE.put("Psychic", Arrays.asList("Psychic", "Steel"));
	    NOT_VERY_EFFECTIVE.put("Bug", Arrays.asList("Fire", "Fighting", "Poison", "Flying", "Ghost", "Steel", "Fairy"));
	    NOT_VERY_EFFECTIVE.put("Rock", Arrays.asList("Fighting", "Ground", "Steel"));
	    NOT_VERY_EFFECTIVE.put("Ghost", Arrays.asList("Dark"));
	    NOT_VERY_EFFECTIVE.put("Dragon", Arrays.asList("Steel"));
	    NOT_VERY_EFFECTIVE.put("Dark", Arrays.asList("Fighting", "Dark", "Fairy"));
	    NOT_VERY_EFFECTIVE.put("Steel", Arrays.asList("Fire", "Water", "Electric", "Steel"));
	    NOT_VERY_EFFECTIVE.put("Fairy", Arrays.asList("Fire", "Poison", "Steel"));
	    
	    // ===== IMMUNE / NESSUN EFFETTO (0x) =====
	    NO_EFFECT.put("Normal", Arrays.asList("Ghost"));
	    NO_EFFECT.put("Electric", Arrays.asList("Ground"));
	    NO_EFFECT.put("Fighting", Arrays.asList("Ghost"));
	    NO_EFFECT.put("Poison", Arrays.asList("Steel"));
	    NO_EFFECT.put("Ground", Arrays.asList("Flying"));
	    NO_EFFECT.put("Psychic", Arrays.asList("Dark"));
	    NO_EFFECT.put("Ghost", Arrays.asList("Normal"));
	    NO_EFFECT.put("Dragon", Arrays.asList("Fairy"));
	}
	
	
	
	public List<Pokemon> getAllPokemon()
	{
		
		 return pokemonRepository.findAll();
	}
	
	public Optional<Pokemon> getPokemonById(Long id) {
	    return pokemonRepository.findById(id);
	}	
	
	public double getTypeEffectiveness(String attackType, String defenseType) {
	    // STEP 1: Controlla se è immune (0x)
	    if (NO_EFFECT.containsKey(attackType) && 
	        NO_EFFECT.get(attackType).contains(defenseType)) {
	        return 0.0;
	    }
	    
	    // STEP 2: Controlla se è super efficace (2x)
	    if (SUPER_EFFECTIVE.containsKey(attackType) && 
	    	SUPER_EFFECTIVE.get(attackType).contains(defenseType)) {
	        return 2.0;
	    }
	    
	    // STEP 3: Controlla se è non molto efficace (0.5x)
	    if (NOT_VERY_EFFECTIVE.containsKey(attackType) && 
	        NOT_VERY_EFFECTIVE.get(attackType).contains(defenseType)) {
	        return 0.5;
	    }
	    
	    // STEP 4: Altrimenti è normale (1x)
	    return 1.0;
	}
	
	public int calculateDamage(Pokemon attacker, Pokemon defender) {
	    // STEP 1: Recupera le statistiche con i getter
	    int attackStat = attacker.getBaseAttack();
	    int defenseStat = defender.getBaseDefense();
	    int attackerSpeed = attacker.getBaseSpeed();
	    int defenderSpeed = defender.getBaseSpeed();
	    
	    // STEP 2: Calcola danno base
	    int baseDamage = attackStat - defenseStat;
	    
	    // STEP 3: Se difesa >= attacco, danno minimo
	    if (baseDamage <= 0) {
	        baseDamage = 5; // danno minimo garantito
	    }
	    
	    // STEP 4: Calcola efficacia dei tipi (considerando type1 E type2)
	    String attackerType = attacker.getType1().getName();
	    
	    // Efficacia contro il primo tipo del defender
	    double effectiveness1 = getTypeEffectiveness(attackerType, defender.getType1().getName());
	    
	    // Efficacia contro il secondo tipo (se esiste)
	    double effectiveness2 = 1.0; // default normale
	    if (defender.getType2() != null) {
	        effectiveness2 = getTypeEffectiveness(attackerType, defender.getType2().getName());
	    }
	    
	    // Moltiplicatore totale = efficacia1 * efficacia2
	    double totalEffectiveness = effectiveness1 * effectiveness2;
	    
	    // STEP 5: Applica moltiplicatore di tipo
	    double damageWithType = baseDamage * totalEffectiveness;
	    
	    // STEP 6: Bonus velocità (chi è più veloce fa +20% danno)
	    if (attackerSpeed > defenderSpeed) {
	        damageWithType = damageWithType * 1.2;
	    } else if (attackerSpeed < defenderSpeed) {
	        damageWithType = damageWithType * 0.9; // malus se più lento
	    }
	    
	    // STEP 7: Converti a intero e ritorna
	    int finalDamage = (int) Math.round(damageWithType);
	    
	    // Assicurati che il danno sia almeno 1
	    return Math.max(1, finalDamage);
	}
	
	public Pokemon simulateBattle(Pokemon pokemon1, Pokemon pokemon2) {
	    // STEP 1: Copia gli HP per non modificare i Pokemon originali
	    int hp1 = pokemon1.getBaseHp();
	    int hp2 = pokemon2.getBaseHp();
	    
	    // STEP 2: Determina chi attacca per primo (chi è più veloce)
	    boolean pokemon1First = pokemon1.getBaseSpeed() >= pokemon2.getBaseSpeed();
	    
	    // STEP 3: Loop di battaglia fino a che uno va KO
	    while (hp1 > 0 && hp2 > 0) {
	        
	        if (pokemon1First) {
	            // Pokemon 1 attacca Pokemon 2
	            int damage = calculateDamage(pokemon1, pokemon2);
	            hp2 -= damage;
	            System.out.println(pokemon1.getName() + " attacca " + pokemon2.getName() + 
	                             " per " + damage + " danni! HP rimanenti: " + hp2);
	            
	            // Se Pokemon 2 è KO, Pokemon 1 vince
	            if (hp2 <= 0) {
	                System.out.println(pokemon2.getName() + " è KO! " + pokemon1.getName() + " vince!");
	                return pokemon1;
	            }
	            
	            // Pokemon 2 contrattacca
	            damage = calculateDamage(pokemon2, pokemon1);
	            hp1 -= damage;
	            System.out.println(pokemon2.getName() + " contrattacca " + pokemon1.getName() + 
	                             " per " + damage + " danni! HP rimanenti: " + hp1);
	            
	            // Se Pokemon 1 è KO, Pokemon 2 vince
	            if (hp1 <= 0) {
	                System.out.println(pokemon1.getName() + " è KO! " + pokemon2.getName() + " vince!");
	                return pokemon2;
	            }
	            
	        } else {
	            // Stessa logica ma Pokemon 2 attacca per primo
	            int damage = calculateDamage(pokemon2, pokemon1);
	            hp1 -= damage;
	            System.out.println(pokemon2.getName() + " attacca " + pokemon1.getName() + 
	                             " per " + damage + " danni! HP rimanenti: " + hp1);
	            
	            if (hp1 <= 0) {
	                System.out.println(pokemon1.getName() + " è KO! " + pokemon2.getName() + " vince!");
	                return pokemon2;
	            }
	            
	            damage = calculateDamage(pokemon1, pokemon2);
	            hp2 -= damage;
	            System.out.println(pokemon1.getName() + " contrattacca " + pokemon2.getName() + 
	                             " per " + damage + " danni! HP rimanenti: " + hp2);
	            
	            if (hp2 <= 0) {
	                System.out.println(pokemon2.getName() + " è KO! " + pokemon1.getName() + " vince!");
	                return pokemon1;
	            }
	        }
	    }
	    
	    // Fallback (non dovrebbe mai arrivare qui)
	    return pokemon1.getBaseHp() > pokemon2.getBaseHp() ? pokemon1 : pokemon2;
	}
	
	
	
	
	/**
	 * Calcola se lo STAB (Same Type Attack Bonus) si applica.
	 * Lo STAB vale 1.5 se il tipo della mossa coincide con type1 O type2 del Pokémon attaccante.
	 * Altrimenti vale 1.0 (nessun bonus).
	 *
	 * @param attacker il Pokémon che attacca
	 * @param move la mossa usata
	 * @return 1.5 se STAB si applica, 1.0 altrimenti
	 */
	public double getStab(Pokemon attacker, Move move) {
	    String moveType = move.getType().getName();
	    
	    if(attacker.getType1().getName().equalsIgnoreCase(moveType)) {
	    	return 1.5;
	    }
	    // TODO: controlla se moveType è uguale al nome di type1 dell'attaccante
	    // se sì ritorna 1.5
	    
	    // TODO: controlla se type2 esiste E se moveType è uguale al nome di type2
	    // se sì ritorna 1.5
	    
	    // nessun bonus STAB
	    return 1.0;
	}

	/**
	 * Calcola il danno inflitto da una mossa specifica.
	 * Formula: (attacco / difesa) * potenzaMossa * efficaciaTipo * STAB
	 *
	 * @param attacker il Pokémon che attacca
	 * @param defender il Pokémon che difende
	 * @param move la mossa usata dall'attaccante
	 * @return il danno finale come intero, minimo 1
	 */
	public int calculateDamageWithMove(Pokemon attacker, Pokemon defender, Move move) {
	    
	    // TODO: se la potenza della mossa è 0 (es. Amnesia) ritorna 0 subito
		if(move.getPower()==0)
			return 0;
	    
		
	    int attackStat = attacker.getBaseAttack();
	    int defenseStat = defender.getBaseDefense();
	    
	    // TODO: calcola baseDamage usando attackStat, defenseStat e move.getPower()
	    // suggerimento: (attackStat / defenseStat) * potenza  — usa double per non perdere decimali
	    
	    
	   double baseDamage= ((double) attackStat / defenseStat)*move.getPower();
	    
	    // TODO: prendi il nome del tipo della mossa
	    String moveType = move.getType().getName();
	    
	    // TODO: calcola effectiveness1 usando getTypeEffectiveness con moveType e il tipo1 del defender
	    
	    
	    double effectiveness1 = getTypeEffectiveness(moveType, defender.getType1().getName());
	    
	    
	    // TODO: se il defender ha type2, calcola effectiveness2, altrimenti lasciala a 1.0
	    double effectiveness2 = 1.0;
	    if(defender.getType2()!=null) {
	    	 effectiveness2 = getTypeEffectiveness(moveType, defender.getType2().getName());
	    		}
	    // TODO: calcola totalEffectiveness moltiplicando effectiveness1 * effectiveness2
	   double  totalEffectiveness = effectiveness1 * effectiveness2;
	    
	    // TODO: chiama getStab per ottenere il moltiplicatore STAB
	   double stab = getStab(attacker, move);
	    
	    // TODO: calcola finalDamage = baseDamage * totalEffectiveness * stab
	    double finalDamage = baseDamage * totalEffectiveness * stab;
	    
	    // ritorna il danno arrotondato, minimo 1
	    return Math.max(1, (int) Math.round(finalDamage));
	}
	
	public Optional<Move> getMoveById(Long id) {
	    return moveRepository.findById(id);
	}
	
	
	
	
	
	
}
