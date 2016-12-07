/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maze;

import maze.model.Character;
import java.util.List;
import maze.model.AttackEvent;

/**
 *
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class Floor {
    private int[][] grid;
    private int[][] visitedGrid;
    private int[][] eventGrid;
    private int floorNumber;
    private int width;
    private int height;
    private long seed;
    private List<Character> characterSprites;
    private List<AttackEvent> attackingMonster;
    private List<AttackEvent> attackingCharacter;
    private boolean canGoUp;
    private boolean canGoDown;
    private MazeGenerator generator;

    public List<Character> getCharacterSprites() {
        return characterSprites;
    }

    public void setCharacterSprites(List<Character> characterSprites) {
        this.characterSprites = characterSprites;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public int[][] getGrid() {
        return grid;
    }

    public void setGrid(int[][] grid) {
        this.grid = grid;
    }

    public int[][] getVisitedGrid() {
        return visitedGrid;
    }

    public void setVisitedGrid(int[][] visitedGrid) {
        this.visitedGrid = visitedGrid;
    }

    public int[][] getEventGrid() {
        return eventGrid;
    }

    public void setEventGrid(int[][] eventGrid) {
        this.eventGrid = eventGrid;
    }

    public boolean isCanGoUp() {
        return canGoUp;
    }

    public void setCanGoUp(boolean canGoUp) {
        this.canGoUp = canGoUp;
    }

    public boolean isCanGoDown() {
        return canGoDown;
    }

    public void setCanGoDown(boolean canGoDown) {
        this.canGoDown = canGoDown;
    }
    
    public void seenObservableGridFromCoords(int x, int y, int fromDirection) {
        int count = 0;
        getVisitedGrid()[y][x] |= MazeConst.SEEN;
        while((getGrid()[y][x-count] & MazeConst.W) == MazeConst.W) {
            count++;
            getVisitedGrid()[y][x-count] |= MazeConst.SEEN;
        }
        count = 0;
        while(x + count < grid[y].length && (getGrid()[y][x + count] & MazeConst.E) == MazeConst.E) {
            count++;
            getVisitedGrid()[y][x + count] |= MazeConst.SEEN;
        }
        count = 0;
        while(y + count < grid.length && (getGrid()[y + count][x] & MazeConst.S) == MazeConst.S) {
            count++;
            getVisitedGrid()[y + count][x] |= MazeConst.SEEN;
        }
        count = 0;
        while(y - count >= 0 && (getGrid()[y - count][x] & MazeConst.N) == MazeConst.N) {
            count++;
            getVisitedGrid()[y-count][x] |= MazeConst.SEEN;
        }
    }

    public MazeGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(MazeGenerator generator) {
        this.generator = generator;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
    public Character getCharAt(int x, int y) {
        for(Character chara : getCharacterSprites()) {
            if(chara.getX() == x && chara.getY() == y) {
                return chara;
            }
        }
        return null;
    }

    public List<AttackEvent> getAttackingMonster() {
        return attackingMonster;
    }

    public void setAttackingMonster(List<AttackEvent> attackingMonster) {
        this.attackingMonster = attackingMonster;
    }

    public List<AttackEvent> getAttackingCharacter() {
        return attackingCharacter;
    }

    public void setAttackingCharacter(List<AttackEvent> attackingCharacter) {
        this.attackingCharacter = attackingCharacter;
    }
}
