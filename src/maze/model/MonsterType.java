/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maze.model;

/**
 *
 * @author MG-PC072
 */
public enum MonsterType {
    BLUE_SLIME(1, 10, 1, 4, 2, 2,
            4, 0.1f, 1, 0.5f, 0.5f, 1);
    
    private final float baseHealth;
    private final float baseMana;
    private final float baseStrength;
    private final float baseAgility;
    private final float baseIntelligence;
    private final int baseExpValue;
    
    private final float addHealth;
    private final float addMana;
    private final float addStrength;
    private final float addAgility;
    private final float addIntelligence;
    private final int addExpValue;
    
    MonsterType(float baseHealth, float baseMana, float baseStr, float baseAgi, float baseInt, int baseExpValue,
            float addHealth, float addMana, float addStr, float addAgi, float addInt, int addExpValue) {
        this.baseHealth = baseHealth;
        this.baseMana = baseMana;
        this.baseStrength = baseStr;
        this.baseAgility = baseAgi;
        this.baseIntelligence = baseInt;
        this.baseExpValue = baseExpValue;
        this.addHealth = addHealth;
        this.addMana = addMana;
        this.addStrength = addStr;
        this.addAgility = addAgi;
        this.addIntelligence = addInt;
        this.addExpValue = addExpValue;
    }

    public static MonsterType getBLUE_SLIME() {
        return BLUE_SLIME;
    }

    public float getBaseHealth() {
        return baseHealth;
    }

    public float getBaseMana() {
        return baseMana;
    }

    public float getBaseStrength() {
        return baseStrength;
    }

    public float getBaseAgility() {
        return baseAgility;
    }

    public float getBaseIntelligence() {
        return baseIntelligence;
    }

    public int getBaseExpValue() {
        return baseExpValue;
    }

    public float getAddHealth() {
        return addHealth;
    }

    public float getAddMana() {
        return addMana;
    }

    public float getAddStrength() {
        return addStrength;
    }

    public float getAddAgility() {
        return addAgility;
    }

    public float getAddIntelligence() {
        return addIntelligence;
    }

    public int getAddExpValue() {
        return addExpValue;
    }
}
