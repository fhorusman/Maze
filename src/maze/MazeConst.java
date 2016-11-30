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

/**
 *
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class MazeConst {
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
    
    public static Map<Integer, Integer> getOPPOSITE() {
        if(OPPOSITE == null) {
            OPPOSITE = new HashMap();
            OPPOSITE.put(N, S);
            OPPOSITE.put(S, N);
            OPPOSITE.put(E, W);
            OPPOSITE.put(W, E);
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
            default: return -1;
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
}
