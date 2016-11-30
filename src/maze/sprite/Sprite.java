/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maze.sprite;

import maze.MazeConst;

/**
 *
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class Sprite {
    private Tile currentTile;

    private Tile generalTile;
    private Tile faceNorth, faceSouth, faceEast, faceWest;
    protected int spriteHeight = MazeConst.TILE_HEIGHT;
    protected int spriteWidth = MazeConst.TILE_WIDTH;

    private boolean automaticTurnEnabled = true;

    public Sprite(Tile tile) {
        this.generalTile = tile;
        this.currentTile = tile;
    }

    public Tile getFacingDirectionTile(int DIRECTION) {
        if ((DIRECTION & MazeConst.N) != 0) { // facing North
            if (faceNorth == null) {
                if (automaticTurnEnabled) {
                    generalTile.setRotation(0);
                    currentTile = generalTile;
                }
            } else {
                currentTile = faceNorth;
            }
        } else if ((DIRECTION & MazeConst.S) != 0) { // facing South
            if (faceSouth == null) {
                if (automaticTurnEnabled) {
                    generalTile.setRotation(180);
                    currentTile = generalTile;
                }
            } else {
                currentTile = faceSouth;
            }
        } else if ((DIRECTION & MazeConst.E) != 0) { // facing East
            if (faceEast == null) {
                if (automaticTurnEnabled) {
                    generalTile.setRotation(90);
                    currentTile = generalTile;
                }
            } else {
                currentTile = faceEast;
            }
        } else if ((DIRECTION & MazeConst.W) != 0) { // facing West
            if (faceWest == null) {
                if (automaticTurnEnabled) {
                    generalTile.setRotation(-90);
                    currentTile = generalTile;
                }
            } else {
                currentTile = faceWest;
            }
        }

        return currentTile;
    }

    public void setFaceNorth(Tile faceNorth) {
        this.faceNorth = faceNorth;
    }

    public void setFaceSouth(Tile faceSouth) {
        this.faceSouth = faceSouth;
    }

    public void setFaceEast(Tile faceEast) {
        this.faceEast = faceEast;
    }

    public void setFaceWest(Tile faceWest) {
        this.faceWest = faceWest;
    }

    public void setGeneralTile(Tile tile) {
        this.generalTile = tile;
    }

    public Tile getCurrentTile() {
        return currentTile;
    }

    public boolean isAutomaticTurnEnabled() {
        return automaticTurnEnabled;
    }

    public void setAutomaticTurnEnabled(boolean automaticTurnEnabled) {
        this.automaticTurnEnabled = automaticTurnEnabled;
    }

    public void update(int delta) {
    }

    /**
     * @return the spriteHeight
     */
    public int getSpriteHeight() {
        return spriteHeight;
    }

    /**
     * @param spriteHeight the spriteHeight to set
     */
    public void setSpriteHeight(int spriteHeight) {
        this.spriteHeight = spriteHeight;
    }

    /**
     * @return the spriteWidth
     */
    public int getSpriteWidth() {
        return spriteWidth;
    }

    /**
     * @param spriteWidth the spriteWidth to set
     */
    public void setSpriteWidth(int spriteWidth) {
        this.spriteWidth = spriteWidth;
    }
}
