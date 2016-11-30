/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maze;

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
    private int nextLevelExp;
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
    private int currLevel;
    private int currExpValue;
    
    private MovingSprite sprite;

    public Character(String name, float health, float mana, float strength, float agility, float intelligence, int exp, MovingSprite sprite) {
        this.name = name;
        this.baseHealth = health;
        this.currHealth = health;
        this.baseMana = mana;
        this.currMana = mana;
        this.baseStrength = strength;
        this.baseAgility = agility;
        this.baseIntelligence = intelligence;
        this.baseExpValue = exp;
        levelUp();
        this.sprite = sprite;
    }
    
    public void setPotentials(float health, float mana, float strength, float agility, float intelligence, int exp) {
        this.addHealth = health;
        this.addMana = mana;
        this.addStrength = strength;
        this.addAgility = agility;
        this.addIntelligence = intelligence;
        this.addExpValue = exp;
    }
    
    public void levelUp() {
        setCurrLevel(getCurrLevel() + 1);
        calculateCurrStatus();
    }

    private void calculateCurrStatus() {
        this.currMaxHealth = getBaseHealth() + (getCurrLevel() * getAddHealth());
        this.currMaxMana = getBaseMana() + (getCurrLevel() * getAddMana());
        this.currStrength = getBaseStrength() + (getCurrLevel() * getAddStrength());
        this.currAgility = getBaseAgility() + (getCurrLevel() * getAddAgility());
        this.currIntelligence = getBaseIntelligence() + (getCurrLevel() * getAddIntelligence());
        this.currExpValue = getBaseExpValue() + (getCurrLevel() * getAddExpValue());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getHealth() {
        return getBaseHealth();
    }

    public void setHealth(float health) {
        this.baseHealth = health;
    }

    public float getMana() {
        return getBaseMana();
    }

    public void setMana(float mana) {
        this.baseMana = mana;
    }

    public float getStrength() {
        return getBaseStrength();
    }

    public void setStrength(float strength) {
        this.baseStrength = strength;
    }

    public float getAgility() {
        return getBaseAgility();
    }

    public void setAgility(float agility) {
        this.baseAgility = agility;
    }

    public float getIntelligence() {
        return getBaseIntelligence();
    }

    public void setIntelligence(float intelligence) {
        this.baseIntelligence = intelligence;
    }

    public MovingSprite getSprite() {
        return sprite;
    }

    public void setSprite(MovingSprite sprite) {
        this.sprite = sprite;
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

    public int getNextLevelExp() {
        return nextLevelExp;
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

    public void setCurrHealth(float currHealth) {
        this.currHealth = currHealth;
    }

    public float getCurrMaxMana() {
        return currMaxMana;
    }

    public float getCurrMana() {
        return currMana;
    }

    public void setCurrMana(float currMana) {
        this.currMana = currMana;
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

    public void setCurrLevel(int currLevel) {
        this.currLevel = currLevel;
        calculateCurrStatus();
    }

    public int getCurrExpValue() {
        return currExpValue;
    }
    
    public void setNextLevelExp(int nextLevelExp) {
        this.nextLevelExp = nextLevelExp;
    }
}
