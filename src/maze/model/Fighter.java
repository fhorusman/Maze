/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maze.model;

import maze.Floor;
import util.NLog;

/**
 *
 * @author MG-PC072
 */
public class Fighter {
    public static final String KTag = "Fighter";
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
    private int attackRange;
    private int visionRange;

    /**
     * Initialize Fighter with status
     * @param level
     * @param health
     * @param mana
     * @param strength
     * @param agility
     * @param intelligence
     * @param exp
     */
    public Fighter(int level, float health, float mana, float strength, float agility, float intelligence, int exp) {
        this.currLevel = level;
        this.currHealth = health;
        this.currMaxHealth = health;
        this.currMana = mana;
        this.currMaxMana = mana;
        this.currStrength = strength;
        this.currAgility = agility;
        this.currIntelligence = intelligence;
        this.currExpValue = exp;
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
    public void setBaseValue(float health, float mana, float strength, float agility, float intelligence, int exp, int attackRange, int visionRange) {
        this.baseHealth = health;
        this.baseMana = mana;
        this.baseStrength = strength;
        this.baseAgility = agility;
        this.baseIntelligence = intelligence;
        this.baseExpValue = exp;
        this.attackRange = attackRange;
        this.visionRange = visionRange;
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

    public int getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(int attackRange) {
        this.attackRange = attackRange;
    }

    public int getVisionRange() {
        return visionRange;
    }

    public void setVisionRange(int visionRange) {
        this.visionRange = visionRange;
    }
    
    public void attack(Character owner, Character target, Floor f) {
        float potentialDamage = owner.getFighter().getCurrStrength();
        float potentialResist = target.getFighter().getCurrStrength()/ 2;
        float damage = potentialDamage - potentialResist;
        if(damage > 0) {
            target.getFighter().takeDamage(target, damage);
            NLog.log(KTag, "attack", owner.getName() + " dealt " + damage + 
                    " damage to " + target.getName() + "(" + target.getFighter().getCurrHealth() + 
                    "/" + target.getFighter().getCurrMaxHealth() + ")!");
        } else {
            NLog.log(KTag, "attack", owner.getName() + " attacked " + 
                    target.getName() + "(" + target.getFighter().getCurrHealth() + 
                    "/" + target.getFighter().getCurrMaxHealth() + ") but it has no effect!");
        }
    }
    
    public void attack(Character owner, Character target, Spell spell, Floor f) {
        float potentialDamage = owner.getFighter().getCurrIntelligence() + spell.getAttackPower();
        float potentialResist = target.getFighter().getCurrIntelligence()/ 2;
        float damage = potentialDamage - potentialResist;
        if(damage > 0) {
            target.getFighter().takeDamage(target, damage);
            NLog.log(KTag, "attack", owner.getName() + " cast " + spell.getName() + " dealt " + damage + 
                    " damage to " + target.getName() + "(" + target.getFighter().getCurrHealth() + 
                    "/" + target.getFighter().getCurrMaxHealth() + ")!");
        } else {
            NLog.log(KTag, "attack", owner.getName() + " cast " + spell.getName()  + " toward " + 
                    target.getName() + "(" + target.getFighter().getCurrHealth() + 
                    "/" + target.getFighter().getCurrMaxHealth() + ") but it has no effect!");
        }
    }
    
    public void takeDamage(Character owner, float damage) {
        if(damage > 0) {
            owner.getFighter().setCurrHealth(owner.getFighter().getCurrHealth() - damage);
        }
    }
}
