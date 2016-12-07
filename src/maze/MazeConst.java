/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import maze.model.Character;
import util.NLog;

/**
 *
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class MazeConst {
    public static final String KTag = "MazeConst";
    public static final int ALL_DIRECTION = 15; // 1111
    public static final int N = 1; // 0001
    public static final int S = 2; // 0010
    public static final int E = 4; // 0100
    public static final int W = 8; // 1000
    public static final int U = 16;
    public static final int D = 32;
    
    public static final int SEEN = 1;
    public static final int UNPASSABLE = 2;
    
    public static final int TREASURE = 4;
    public static final int TREASURE_OPENED = 8;
    
    private static Map<Integer, Integer> OPPOSITE;
    private static List<Integer> DIRECTIONS;
    
    public static final int TILE_HEIGHT = 32;
    public static final int TILE_WIDTH = 32;
    
    public static final int MOVEMENT_ANIMATION_DURATION = 200;
    
    public static Map<Integer, Integer> getOPPOSITE() {
        if(OPPOSITE == null) {
            OPPOSITE = new HashMap();
            OPPOSITE.put(N, S);
            OPPOSITE.put(S, N);
            OPPOSITE.put(E, W);
            OPPOSITE.put(W, E);
            OPPOSITE.put(U, D);
            OPPOSITE.put(D, U);
        }
        return OPPOSITE;
    }
    
    public static int OPPOSITE_OF(int direction) {
        switch(direction) {
            case N: return S;
            case S: return N;
            case E: return W;
            case W: return E;
            case U: return D;
            case D: return U;
            default: return 0;
        }
    }
    
    public static List<Integer> getDIRECTIONS() {
        if(DIRECTIONS == null) {
            DIRECTIONS = new ArrayList<>();
            DIRECTIONS.add(N);
            DIRECTIONS.add(S);
            DIRECTIONS.add(E);
            DIRECTIONS.add(W);
        }
        return DIRECTIONS;
    }
    
    public static String toString(int direction) {
        switch(direction) {
            case N: return "North";
            case S: return "South";
            case E: return "East";
            case W: return "West";
            case U: return "Up";
            case D: return "Down";
            default: return "Unknown";
        }
    }
    
    /**
     * Get the Manhattan Distance between coordinate
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return 
     */
    public static int getMD(int x1, int y1, int x2, int y2) {
        int xDis = Math.abs(x1 - x2);
        int yDis = Math.abs(y1 - y2);
        return xDis + yDis;
    }
    
    public static int getDirection(int dx, int dy) {
        if(dx == 0 && dy == -1) {
            return N;
        } else if(dx == 0 && dy == 1) {
            return S;
        } else if(dx == 1 && dy == 0) {
            return E;
        } else if(dx == -1 && dy == 0) {
            return W;
        }
        return -1;
    }
    
    public static boolean isWithinRange(int sourceX, int sourceY, int targetX, int targetY, int range, Floor f) {
        if(getMD(sourceX, sourceY, targetX, targetY) <= range) {
            int directionTowardPC = getDirectionUsingVector(sourceX, sourceY, targetX, targetY);
            return (f.getGrid()[sourceY][sourceX] & directionTowardPC) == directionTowardPC;
        }
        return false;
    }
    
    public static int getDirectionUsingVector(int sourceX, int sourceY, int targetX, int targetY) {
        int dx = targetX - sourceX;
        int dy = targetY - sourceY;
        double distance = Math.sqrt((dx*dx) + (dy * dy));
        dx = Math.toIntExact(Math.round(dx / distance));
        dy = Math.toIntExact(Math.round(dy / distance));
        int directionTowardPC = MazeConst.getDirection(dx, dy);
        return directionTowardPC;
    }
    
    public static boolean checkDirectionHasTarget(Character owner, Character target, Floor f) {
        int nextX = owner.getTargetX(), nextY = owner.getTargetY();
        int x = owner.getX(), y = owner.getY();
        // Check if target is within range
//        if(isWithinRange(nextX, nextY, target.getTargetX(), target.getTargetY(), owner.getFighter().getVisionRange(), f)) {
//            return true;
//        }
        
        do {
            if ((target.getX() == nextX && target.getY() == nextY) || 
                    (target.getTargetX() == nextX && target.getTargetY() == nextY)) {
                NLog.log(KTag, "checkDirectionHasTarget", owner.getName() + "@(" + x + "," + nextY + ") -> (" + nextX + "," + nextY + ") toward " + 
                        toString(owner.getSpriteDirection()) + " has seen " + target.getName());
                owner.setPcSeen(true);
                return true;
            }
            if((f.getGrid()[nextY][nextX] & owner.getSpriteDirection()) != 0) {
                nextX += f.getGenerator().getDX().get(owner.getSpriteDirection());
                nextY += f.getGenerator().getDY().get(owner.getSpriteDirection());
            } else {
                break;
            }
        } while(true);
        return false;
    }

    public static boolean isRectIntersect(float x1, float x2, float y1, float y2, float otherX1, float otherX2, float otherY1, float otherY2) {
        return x1 <= otherX2 && x2 >= otherX1 &&
                y1 <= otherY2 && y2 >= otherY1;
    }
}
