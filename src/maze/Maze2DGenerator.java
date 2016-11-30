/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Create a 2D Maze that has no upstairs or downstairs.
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class Maze2DGenerator {
    private Random rand;
    
    // Constants
    private Map<Integer, Integer> DX = new HashMap();
    private Map<Integer, Integer> DY = new HashMap();
    
    public Maze2DGenerator() {
        rand = new Random();
        setHashMap();
    }
    
    public Map<Integer, Integer> getDX() {
        return DX;
    }
    
    public Map<Integer, Integer> getDY() {
        return DY;
    }
    
    private void setHashMap() {
        DX.put(MazeConst.N, 0);
        DX.put(MazeConst.S, 0);
        DX.put(MazeConst.E, 1);
        DX.put(MazeConst.W, -1);
        
        DY.put(MazeConst.N, -1);
        DY.put(MazeConst.S, 1);
        DY.put(MazeConst.E, 0);
        DY.put(MazeConst.W, 0);
    }
    int counter = 0;
    public void carvePassages(int cx, int cy, int[][] grid) {
        List<Integer> thisDirection = new ArrayList<>();
        thisDirection.addAll(MazeConst.getDIRECTIONS());
        
        Collections.shuffle(thisDirection, rand);
        for(Integer direct : thisDirection) {
            int newX = cx + DX.get(direct), newY = cy + DY.get(direct);
            if(newY >= 0 && newY < grid.length && newX >= 0 && newX < grid[newY].length && grid[newY][newX] == 0) {
                grid[cy][cx] |= direct;
                grid[newY][newX] |= MazeConst.getOPPOSITE().get(direct);
                carvePassages(newX, newY, grid);
            }
        }
    }
    
    public void printGrid(int[][] grid, int width, int height) {
        String wall = " ";
        for(int i = 0; i < width * 2 - 1; i++) {
            wall+= "_";
        }
        System.out.println(wall);
        String binaryText = "";
        for(int y = 0; y < height; y++) {
            
            System.out.print("|");
            for(int x = 0; x < width; x++) {
//                /*
                System.out.print((grid[y][x] & MazeConst.S) != 0 ? " " : "_");
                if((grid[y][x] & MazeConst.E) != 0) {
                    if(x + 1 >= width) System.out.print("X");
                    else System.out.print(((grid[y][x] | grid[y][x+1]) & MazeConst.S) != 0 ? " " : "_");
                } else {
                    System.out.print("|");
                }
//              */
                binaryText += String.format("%4s", Integer.toBinaryString(grid[y][x])).replace(" ", "0") + " ";
            }
            binaryText += "\n";
            System.out.println("");
        }
        System.out.println("");
        System.out.println(binaryText);
    }
    
    public int[][] run(int width, int height, int startX, int startY, long seed) {
        int grid[][] = new int[height][width];
        rand.setSeed(seed);
        
        carvePassages(startX, startY, grid);
        
        printGrid(grid, width, height);
        System.out.println("seed:" + seed);
        return grid;
    }
    
    public static void main(String[] args) {
        Maze2DGenerator gen = new Maze2DGenerator();
        gen.run(10,10,0,0,1385728600213L);
    }
}