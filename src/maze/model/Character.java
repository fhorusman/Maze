/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maze.model;

import maze.sprite.MovingSprite;

/**
 *
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class Character {
    private String name;
    
    private float baseHealth;
    private float baseMana;
    private float baseStrength;
    private float baseAgility;
    private float baseIntelligence;
    private int baseExpValue;
    
    private float addHealth;
    private float addMana;
    private float addStrength;
    private float addAgility;
    private float addIntelligence;
    private int addExpValue;
    
    private float currMaxHealth;
    private float currHealth;
    private float currMaxMana;
    private float currMana;
    private float currStrength;
    private float currAgility;
    private float currIntelligence;
    private int currLevel = 1;
    private int currExpValue;
    private int currNextLevelExp;
    private int currExp;
    
    private MovingSprite sprite;

    /**
     * Initialize Character with the current status
     * @param name
     * @param level
     * @param health
     * @param mana
     * @param strength
     * @param agility
     * @param intelligence
     * @param exp
     * @param sprite 
     */
    public Character(String name, int level, float health, float mana, float strength, float agility, float intelligence, int exp, MovingSprite sprite) {
        this.name = name;
        this.currLevel = level;
        this.currHealth = health;
        this.currMaxHealth = health;
        this.currMana = mana;
        this.currMaxMana = mana;
        this.currStrength = strength;
        this.currAgility = agility;
        this.currIntelligence = intelligence;
        this.currExpValue = exp;
        this.sprite = sprite;
        currExp = 0;
    }
    
    /**
     * Set the status gain for each level
     * @param health
     * @param mana
     * @param strength
     * @param agility
     * @param intelligence
     * @param exp 
     */
    public void setPotentials(float health, float mana, float strength, float agility, float intelligence, int exp) {
        this.addHealth = health;
        this.addMana = mana;
        this.addStrength = strength;
        this.addAgility = agility;
        this.addIntelligence = intelligence;
        this.addExpValue = exp;
    }
    
    /**
     * Set the character status on level 1
     * @param health
     * @param mana
     * @param strength
     * @param agility
     * @param intelligence
     * @param exp 
     */
    public void setBaseValue(float health, float mana, float strength, float agility, float intelligence, int exp) {
        this.baseHealth = health;
        this.baseMana = mana;
        this.baseStrength = strength;
        this.baseAgility = agility;
        this.baseIntelligence = intelligence;
        this.baseExpValue = exp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public float getCurrMaxHealth() {
        return currMaxHealth;
    }

    public float getCurrHealth() {
        return currHealth;
    }

    public float getCurrMaxMana() {
        return currMaxMana;
    }

    public float getCurrMana() {
        return currMana;
    }

    public float getCurrStrength() {
        return currStrength;
    }

    public float getCurrAgility() {
        return currAgility;
    }

    public float getCurrIntelligence() {
        return currIntelligence;
    }

    public int getCurrLevel() {
        return currLevel;
    }

    public int getCurrExpValue() {
        return currExpValue;
    }

    public MovingSprite getSprite() {
        return sprite;
    }

    public void setCurrMaxHealth(float currMaxHealth) {
        this.currMaxHealth = currMaxHealth;
    }

    public void setCurrHealth(float currHealth) {
        this.currHealth = currHealth;
    }

    public void setCurrMaxMana(float currMaxMana) {
        this.currMaxMana = currMaxMana;
    }

    public void setCurrMana(float currMana) {
        this.currMana = currMana;
    }

    public void setCurrStrength(float currStrength) {
        this.currStrength = currStrength;
    }

    public void setCurrAgility(float currAgility) {
        this.currAgility = currAgility;
    }

    public void setCurrIntelligence(float currIntelligence) {
        this.currIntelligence = currIntelligence;
    }

    public void setCurrLevel(int currLevel) {
        this.currLevel = currLevel;
    }

    public void setCurrExpValue(int currExpValue) {
        this.currExpValue = currExpValue;
    }

    public void setSprite(MovingSprite sprite) {
        this.sprite = sprite;
    }

    public int getCurrNextLevelExp() {
        return currNextLevelExp;
    }

    public void setCurrNextLevelExp(int currNextLevelExp) {
        this.currNextLevelExp = currNextLevelExp;
    }

    public int getCurrExp() {
        return currExp;
    }

    public void setCurrExp(int currExp) {
        this.currExp = currExp;
    }
}
