package com.generation.pokemontournament.dto;

public class TurnResult {

    private int damage;
    private boolean missed;
    private String effectiveness; // "normal", "super", "notVery", "immune"
    private String category;      // "physical", "special", "status"

    public TurnResult(int damage, boolean missed, String effectiveness, String category) {
        this.damage = damage;
        this.missed = missed;
        this.effectiveness = effectiveness;
        this.category = category;
    }

    public int getDamage()          { return damage; }
    public boolean isMissed()       { return missed; }
    public String getEffectiveness(){ return effectiveness; }
    public String getCategory()     { return category; }
}
