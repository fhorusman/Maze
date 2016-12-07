/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maze;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author MG-PC072
 */
public class MazeGenerator {
    protected Random rand;
    
    // Constants
    protected Map<Integer, Integer> DX = new HashMap();
    protected Map<Integer, Integer> DY = new HashMap();
    
    public Map<Integer, Integer> getDX() {
        return DX;
    }
    
    public Map<Integer, Integer> getDY() {
        return DY;
    }
}
