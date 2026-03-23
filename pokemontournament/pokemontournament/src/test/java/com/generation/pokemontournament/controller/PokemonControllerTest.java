package com.generation.pokemontournament.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PokemonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllPokemon() throws Exception {
        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetPokemonById() throws Exception {
         mockMvc.perform(get("/api/pokemon/1"))
    	.andExpect(status().isOk())
    	.andExpect(jsonPath("$.id").exists());
    }
    
    @Test
    public void testGetPokemonByIdNotFound() throws Exception {   
        mockMvc.perform(get("/api/pokemon/99999"))
        .andExpect(status().isNotFound());
    }

    @Test
    public void testBattle() throws Exception {
    	mockMvc.perform(post("/api/battle")
    		    .contentType("application/json")
    		    .content("{\"pokemon1Id\": 1, \"pokemon2Id\": 2}"))
    		    .andExpect(status().isOk())
    		    .andExpect(jsonPath("$.id").exists());
	    }
    
    @Test
    public void testBattleInvalidPokemon() throws Exception {
        // TODO: POST /api/battle con id inesistente, verificare errore
    	mockMvc.perform(post("/api/battle")
    		    .contentType("application/json")
    		    .content("{\"pokemon1Id\": 9999, \"pokemon2Id\": 9999}"))
    		    .andExpect(status().isNotFound())
    		    .andExpect(jsonPath("$.id").doesNotExist());
             }
    
}