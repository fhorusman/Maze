/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import maze.MazeDrawer;
import org.lwjgl.LWJGLException;

/**
 *
 * @author FHORUSMAN
 */
public class Game {
    public static void main(String[] args) throws LWJGLException {
        MazeDrawer drawer = new MazeDrawer();
        drawer.start();
    }
}
