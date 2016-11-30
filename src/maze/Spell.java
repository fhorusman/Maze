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
public class Spell {
    private String name;
    private float attackPower;
    private MovingSprite sprite;

    public Spell(String name, float attackPower, MovingSprite sprite) {
        this.name = name;
        this.attackPower = attackPower;
        this.sprite = sprite;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAttackPower() {
        return attackPower;
    }

    public void setAttackPower(float attackPower) {
        this.attackPower = attackPower;
    }

    public MovingSprite getSprite() {
        return sprite;
    }

    public void setSprite(MovingSprite sprite) {
        this.sprite = sprite;
    }
}
