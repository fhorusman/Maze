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
 * 3D Maze has more than one upstairs and downstairs in one floor, and only has
 * one route to go to the exit.
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class Maze3DGenerator {
    private Random rand;
    
    // Constants
    private final int N = 1;
    private final int S = 2;
    private final int E = 4;
    private final int W = 8;
    private final int U = 16;
    private final int D = 32;
    private final Map<Integer, Integer> DX = new HashMap();
    private final Map<Integer, Integer> DY = new HashMap();
    private final Map<Integer, Integer> DZ = new HashMap();
    private final Map<Integer, Integer> OPPOSITE = new HashMap();
    private final List<Integer> directions = new ArrayList<>();
    
    public void setHashMap() {
        DX.put(N, 0);
        DX.put(S, 0);
        DX.put(E, 1);
        DX.put(W, -1);
        DX.put(U, 0);
        DX.put(D, 0);
        
        DY.put(N, -1);
        DY.put(S, 1);
        DY.put(E, 0);
        DY.put(W, 0);
        DY.put(U, 0);
        DY.put(D, 0);
        
        DZ.put(N, 0);
        DZ.put(S, 0);
        DZ.put(E, 0);
        DZ.put(W, 0);
        DZ.put(U, 1);
        DZ.put(D, -1);
        
        OPPOSITE.put(N, S);
        OPPOSITE.put(S, N);
        OPPOSITE.put(E, W);
        OPPOSITE.put(W, E);
        OPPOSITE.put(U, D);
        OPPOSITE.put(D, U);
        
        directions.add(N);
        directions.add(S);
        directions.add(E);
        directions.add(W);
        directions.add(U);
        directions.add(D);
    }
    
    public void carvePassages(int cx, int cy, int cz, int[][][] grid) {
        List<Integer> thisDirection = new ArrayList<>();
        thisDirection.addAll(directions);
        
        Collections.shuffle(thisDirection, rand);
        for(Integer direct : thisDirection) {
            int newX = cx + DX.get(direct), newY = cy + DY.get(direct), newZ = cz + DZ.get(direct);
            if(newZ >= 0 && newZ < grid.length && newY >= 0 && newY < grid[newZ].length && newX >= 0 && newX < grid[newZ][newY].length && grid[newZ][newY][newX] == 0) {
                grid[cz][cy][cx] |= direct;
                grid[newZ][newY][newX] |= OPPOSITE.get(direct);
                carvePassages(newX, newY, newZ, grid);
            }
        }
    }
    
    public void printGrid(int[][] grid, int width, int height, int tall) {
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
                System.out.print((grid[y][x] & S) != 0 ? " " : "_");
                if((grid[y][x] & E) != 0) {
                    if(x + 1 >= width) System.out.print("|");
                    else System.out.print(((grid[y][x] | grid[y][x+1]) & S) != 0 ? " " : "_");
                } else {
                    System.out.print("|");
                }
//              */
                binaryText += String.format("%6s", Integer.toBinaryString(grid[y][x])).replace(" ", "0") + " ";
            }
            binaryText += "\n";
            System.out.println("");
        }
        System.out.println("");
        System.out.println(binaryText);
    }
    
    public int[][][] run(int width, int height, int tall, int startX, int startY, int startZ) {
        int grid[][][] = new int[tall][height][width];
//        long seed = (long) (Math.random() * );
        rand = new Random();
        setHashMap();
        carvePassages(startX, startY, startZ, grid);
        
        printGrid(grid[1], width, height, tall);
//        System.out.println("seed:" + seed);
        return grid;
    }
    
    public static void main(String[] args) {
        Maze3DGenerator test = new Maze3DGenerator();
        test.run(10, 10, 10, 0, 0, 0);
    }
}