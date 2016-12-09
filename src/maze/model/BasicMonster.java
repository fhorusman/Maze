/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maze.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import maze.Floor;
import maze.MazeConst;
import static maze.MazeConst.toString;
import util.NLog;

/**
 *
 * @author MG-PC072
 */
public class BasicMonster implements AI {
    private static final String KTag = "BasicMonster";
    @Override
    public Action takeTurn(Character owner, Character target, Floor f) {
        int msX = owner.getX(), msY = owner.getY();
        Action action = Action.WAIT;
        if(msX == owner.getTargetX() && msY == owner.getTargetY()) {
            if(MazeConst.isWithinRange(msX, msY, target.getTargetX(), target.getTargetY(), owner.getFighter().getAttackRange(), f)) {
                AttackEvent event = new AttackEvent(owner, target);
                int directionTowardPC = MazeConst.getDirectionUsingVector(msX, msY, target.getTargetX(), target.getTargetY());
                owner.turnSprite(directionTowardPC);
                owner.setPcSeen(true);
                NLog.log(KTag, "takeTurn", target.getName() + " is within " + owner.getName() + "'s attack range-> direction: " + MazeConst.toString(directionTowardPC));
                f.getAttackingMonster().add(event);
                action = Action.NORMAL_ATTACK;
                
            } else if(owner.hasSeenPc()) {
                owner.moveToward(target.getTargetX(), target.getTargetY(), f);
                action = Action.MOVE;
            }
            else {
                if(moveRandomly(owner, f))
                    action = Action.MOVE;
            }
        }
        MazeConst.checkDirectionHasTarget(owner, target, f);
        return action;
    }

    private boolean moveRandomly(Character owner, Floor f) {
//        System.out.println("moveRandomly");
        int targetX = owner.getTargetX();
        int targetY = owner.getTargetY();
        List<Integer> thisDirection = new ArrayList<>();
        thisDirection.addAll(MazeConst.getDIRECTIONS());
        Collections.shuffle(thisDirection);
        int way = 0;
        for (Integer direct : thisDirection) {
            // check if direction is passable
            if ((f.getGrid()[owner.getY()][owner.getX()] & direct) != 0) {
                targetX = owner.getTargetX() + f.getGenerator().getDX().get(direct);
                targetY = owner.getTargetY() + f.getGenerator().getDY().get(direct);
                // checking if the next target had the same coordinate as previous coordinate
                if (owner.getPreviousX() == targetX && owner.getPreviousY() == targetY) {
                    // check if the previous coordinate is occupied or not
                    if((f.getVisitedGrid()[targetY][targetX] & MazeConst.UNPASSABLE) == 0) {
                        way++;
                    }
                } 
                // checking if the next target is passable
                else if ((f.getVisitedGrid()[targetY][targetX] & MazeConst.UNPASSABLE) == 0) {
                    // make the current coordinate passable for other character
                    f.getVisitedGrid()[owner.getY()][owner.getX()] ^= MazeConst.UNPASSABLE;
                    // move the monster to another coordinate.
                    owner.moveSprite(targetX, targetY);
                    owner.turnSprite(direct);
                    // make the target coordinate as unpassable for other character.
                    f.getVisitedGrid()[targetY][targetX] ^= MazeConst.UNPASSABLE;
                    return true;
                }
            }
        }
        // if there's only one way that possible and previously it was not choosen
        // because it was the previous way
        // (because if it was possible and it wasn't the previous coordinate,
        // this expression would never been called in the first place)
        if(way == 1) {
//            System.out.println(owner.getName() + " way == 1");
            targetX = owner.getPreviousX();
            targetY = owner.getPreviousY();
            // make the current coordinate passable for other character
            f.getVisitedGrid()[owner.getY()][owner.getX()] ^= MazeConst.UNPASSABLE;
            // move the monster to another coordinate.
            owner.moveSprite(targetX, targetY);
            owner.turnSprite(owner.getPreviousDirection());
            // make the target coordinate as unpassable for other character.
            f.getVisitedGrid()[targetY][targetX] ^= MazeConst.UNPASSABLE;
            return true;
        } else {
//            NLog.log(KTag, "moveRandomly", owner.getName() + "@(" + owner.getTargetX() + "," + owner.getTargetY() + ") could not move!");
                // else means that there's no other direction that possible to move,
            // so this monster should wait for other monster to move first
        }
        return false;
    }
}
