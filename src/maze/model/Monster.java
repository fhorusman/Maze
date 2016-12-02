/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maze.model;

import maze.sprite.MovingSprite;

/**
 *
 * @author MG-PC072
 */
public class Monster extends Character {
    private boolean pcSeen;
    public Monster(String name, MovingSprite sprite, MonsterType type) {
        super(name, 1, type.getBaseHealth(), type.getBaseMana(), type.getBaseStrength(), 
                type.getBaseAgility(), type.getBaseIntelligence(), type.getBaseExpValue(), sprite);
    }
    
    public Monster(String name, int level, float health, float mana, float strength, float agility, float intelligence, int exp, MovingSprite sprite) {
        super(name, level, health, mana, strength, agility, intelligence, exp, sprite);
    }
    
    public boolean hasSeenPc() {
        return pcSeen;
    }
    
    public void setPcSeen(boolean pcSeen) {
        this.pcSeen = pcSeen;
    }
}
