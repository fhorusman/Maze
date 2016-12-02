/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MG-PC072
 */
public class NLog {
    private static Logger log = Logger.getLogger("Maze");
    public static boolean isOn = true;
    public static void log(String tag, String methodName, String message) {
        if(isOn) {
//            log.logp(Level.INFO, tag, methodName, message);
            System.out.println(message);
        }
    }
}
