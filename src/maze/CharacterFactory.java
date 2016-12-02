/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maze;

import maze.model.Monster;
import maze.model.MonsterType;
import maze.sprite.MovingSprite;
import util.NLog;

/**
 *
 * @author MG-PC072
 */
public class CharacterFactory {
    private static final String KTag = "CharacterFactory";
    
    public static Monster CreateMonsterWithLevel(String name, int level, MovingSprite sprite, MonsterType type) {
        float health = type.getBaseHealth() + (level * type.getAddHealth());
        float mana = type.getBaseMana() + (level * type.getAddMana());
        float strength = type.getBaseStrength() + (level * type.getAddStrength());
        float agility = type.getBaseAgility() + (level * type.getAddAgility());
        float intelligence = type.getBaseIntelligence() + (level * type.getAddIntelligence());
        int currExpValue = type.getBaseExpValue() + (level * type.getAddExpValue());
        Monster mon = new Monster(name, level, health, mana, strength, agility, intelligence, level, sprite);
        mon.setBaseValue(type.getBaseHealth(), type.getBaseMana(), type.getBaseStrength(), 
                type.getBaseAgility(), type.getBaseIntelligence(), type.getBaseExpValue());
        mon.setPotentials(type.getAddHealth(), type.getAddMana(), type.getAddStrength(), 
                type.getAddAgility(), type.getAddIntelligence(), type.getAddExpValue());
        int nextLevelExp = Math.round((strength + agility + intelligence + currExpValue) * level);
        mon.setCurrNextLevelExp(nextLevelExp);
        NLog.log(KTag, "CreateMonsterWithLevel", "Generated " + name + " with level " + level + " @("+ sprite.getX() + "," + sprite.getY() + ")" );
        return mon;
    }
}
