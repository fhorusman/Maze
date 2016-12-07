/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maze.model;

import maze.Floor;

/**
 *
 * @author MG-PC072
 */
public interface AI {
    public Action takeTurn(Character owner, Character target, Floor f);
}
