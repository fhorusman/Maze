/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maze.model;

/**
 *
 * @author FHORUSMAN
 */
public class AttackEvent {
    private Character attacker;
    private Character defender;
    private Spell spell;
    private boolean useSpell;
    
    public AttackEvent(Character attacker, Character defender) {
        this.attacker = attacker;
        this.defender = defender;
        this.useSpell = false;
    }
    
    public AttackEvent(Character attacker, Character defender, Spell spell) {
        this.attacker = attacker;
        this.defender = defender;
        this.spell = spell;
        this.useSpell = true;
    }

    public Character getAttacker() {
        return attacker;
    }

    public void setAttacker(Character attacker) {
        this.attacker = attacker;
    }

    public Character getDefender() {
        return defender;
    }

    public void setDefender(Character defender) {
        this.defender = defender;
    }

    public Spell getSpell() {
        return spell;
    }

    public void setSpell(Spell spell) {
        this.spell = spell;
    }

    public boolean isUseSpell() {
        return useSpell;
    }

    public void setUseSpell(boolean useSpell) {
        this.useSpell = useSpell;
    }
}
