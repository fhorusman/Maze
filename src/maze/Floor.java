/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maze;

import java.util.List;
import maze.sprite.MovingSprite;

/**
 *
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class Floor {
    private int[][] grid;
    private int[][] visitedGrid;
    private int[][] eventGrid;
    private int floorNumber;
    private long seed;
    private List<Character> characterSprites;

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
}
