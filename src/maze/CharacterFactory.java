/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maze;

import maze.model.AI;
import maze.model.BasicMonster;
import maze.model.Fighter;
import maze.model.MonsterType;
import maze.model.Character;
import maze.sprite.Tile;
import util.NLog;

/**
 *
 * @author MG-PC072
 */
public class CharacterFactory {
    private static final String KTag = "CharacterFactory";
    
    public static Character CreateBlueSlimeWithLevel(Tile tile, String name, int level, int x, int y) {
        MonsterType type = MonsterType.BLUE_SLIME;
        float health = type.getBaseHealth() + (level * type.getAddHealth());
        float mana = type.getBaseMana() + (level * type.getAddMana());
        float strength = type.getBaseStrength() + (level * type.getAddStrength());
        float agility = type.getBaseAgility() + (level * type.getAddAgility());
        float intelligence = type.getBaseIntelligence() + (level * type.getAddIntelligence());
        int currExpValue = type.getBaseExpValue() + (level * type.getAddExpValue());
        int attackRange = 1;
        int visionRange = 2;
        Fighter mon = new Fighter(level, health, mana, strength, agility, intelligence, currExpValue);
        mon.setBaseValue(type.getBaseHealth(), type.getBaseMana(), type.getBaseStrength(), 
                type.getBaseAgility(), type.getBaseIntelligence(), type.getBaseExpValue(), attackRange, visionRange);
        mon.setPotentials(type.getAddHealth(), type.getAddMana(), type.getAddStrength(), 
                type.getAddAgility(), type.getAddIntelligence(), type.getAddExpValue());
        int nextLevelExp = Math.round((strength + agility + intelligence + currExpValue) * level);
        mon.setCurrNextLevelExp(nextLevelExp);
        AI slimeAI = new BasicMonster();
        
        Character slime = new Character(tile, x, y, name, mon);
        slime.setSpriteHeight(16);
        slime.setSpriteWidth(16);
        slime.setMovementAnimationTime(MazeConst.MOVEMENT_ANIMATION_DURATION);
        slime.setFaceEast(tile);
        slime.setFaceNorth(tile);
        slime.setFaceSouth(tile);
        slime.setFaceWest(tile);
        slime.setAutomaticTurnEnabled(false);
        slime.setAi(slimeAI);
        
        NLog.log(KTag, "CreateBlueSlimeWithLevel", "Generated " + name + " with level " + level + " @("+ x + "," + y + ")" );
        return slime;
    }
}
