/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maze.model;

import maze.Floor;
import maze.MazeConst;
import maze.sprite.MovingSprite;
import maze.sprite.Tile;

/**
 *
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class Character extends MovingSprite {
    private String name;
    private boolean pcSeen;
    
    private Character target;
    private Fighter fighter;
    private AI ai;

    /**
     * Initialize Character with the current status
     */
    public Character(Tile tile, int x, int y, String name, Fighter fighter) {
        super(tile, x, y);
        this.name = name;
        this.fighter = fighter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Character getTarget() {
        return target;
    }

    public void setTarget(Character target) {
        this.target = target;
    }

    public Fighter getFighter() {
        return fighter;
    }

    public void setFighter(Fighter fighter) {
        this.fighter = fighter;
    }

    public AI getAi() {
        return ai;
    }

    public void setAi(AI ai) {
        this.ai = ai;
    }
    
    public boolean hasSeenPc() {
        return pcSeen;
    }
    
    public void setPcSeen(boolean pcSeen) {
        this.pcSeen = pcSeen;
    }
    
    /**
     * Move character to the specified block. 
     * @param targetX
     * @param targetY
     * @param f
     * @return 
     */
    public boolean moveToward(int targetX, int targetY, Floor f) {
        int bestDirection = getSpriteDirection();
        int bestMD = f.getWidth() + f.getHeight();
        int bestX = 0, bestY = 0;
        for(Integer nextDirection : MazeConst.getDIRECTIONS()) {
            if((f.getGrid()[getY()][getX()] & nextDirection) != 0) {
                int nextX = getX() + f.getGenerator().getDX().get(nextDirection);
                int nextY = getY() + f.getGenerator().getDY().get(nextDirection);
                
                int nextMD = MazeConst.getMD(nextX, nextY, targetX, targetY);
                if(nextMD == 0 && (f.getVisitedGrid()[bestY][bestX] & MazeConst.UNPASSABLE) == 0) {
                    bestDirection = nextDirection;
                    bestMD = nextMD;
                    bestX = nextX;
                    bestY = nextY;
                    break;
                } else {
                    nextMD = findBestDirectionMD(nextX, nextY, getX(), getY(), targetX, targetY, f, 0);
//                    System.out.println("moveToward-> toward: " + MazeConst.toString(nextDirection) + ", nextMD: " + nextMD + ", nextX: " + nextX + 
//                            ", nextY: " + nextY + ", targetX: " + targetX + ", targetY: " + targetY);
                    if(nextMD < bestMD) {
                        bestDirection = nextDirection;
                        bestMD = nextMD;
                        bestX = nextX;
                        bestY = nextY;
                    }
                }
            }
        }
        if((f.getVisitedGrid()[bestY][bestX] & MazeConst.UNPASSABLE) == MazeConst.UNPASSABLE) {
//            System.out.println("UNPASSABLE");
        } else {
//            System.out.println("move toward " + bestX + "," + bestY);
            // make the current coordinate passable for other character
            f.getVisitedGrid()[getY()][getX()] ^= MazeConst.UNPASSABLE;
            // move the monster to another coordinate.
            moveSprite(bestX, bestY);
            turnSprite(bestDirection);
            // make the target coordinate as unpassable for other character.
            f.getVisitedGrid()[bestY][bestX] ^= MazeConst.UNPASSABLE;
            return true;
        }
        return false;
    }
    
    private int findBestDirectionMD(int currX, int currY, int prevX, int prevY, int targetX, int targetY, Floor f, int loop) {
//        System.out.println("findBestDirection " + currX + "," + currY);
        int[] nextDirections = new int[MazeConst.getDIRECTIONS().size()];
        int counter = 0, bestMD = f.getWidth() + f.getHeight();
        for(Integer nextDirection : MazeConst.getDIRECTIONS()) {
            if((f.getGrid()[currY][currX] & nextDirection) != 0) {
                int nextX = currX + f.getGenerator().getDX().get(nextDirection);
                int nextY = currY + f.getGenerator().getDY().get(nextDirection);
                
                if(nextX == prevX && nextY == prevY) {
                    continue;
                }

                int nextMD = MazeConst.getMD(nextX, nextY, targetX, targetY);
//                System.out.println(loop + "toward: " + MazeConst.toString(nextDirection) + ", nextMD: " + nextMD + ", nextX: " + nextX + 
//                        ", nextY: " + nextY + ", targetX: " + targetX + ", targetY: " + targetY);
                if(nextMD == 0) {
                    return nextMD;
                } else {
                    if(nextMD < bestMD) {
                        bestMD = nextMD;
                    }
                    nextDirections[counter++] = nextDirection;
                }
            }
        }
        
        for(int i = 0; i < counter; i++) {
            int nextX = currX + f.getGenerator().getDX().get(nextDirections[i]);
            int nextY = currY + f.getGenerator().getDY().get(nextDirections[i]);
            int nextMD = findBestDirectionMD(nextX, nextY, currX, currY, targetX, targetY, f, loop + 1);
            if(nextMD < bestMD) {
                bestMD = nextMD;
            }
        }
        return bestMD;
    }
}
