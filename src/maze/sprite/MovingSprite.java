/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maze.sprite;

import java.util.Date;
import maze.MazeConst;

/**
 *
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class MovingSprite extends Sprite {
    private int x, y, targetX, targetY;
    private float movementX, movementY;
    
    private int movementAnimationTimePerTile;
    private int totalMovementAnimationTimeNeeded;
    private int elapsedTime;
    
    private int spriteDirection;
    private int previousX;
    private int previousY;

    public int getSpriteDirection() {
        return spriteDirection;
    }

    public int getMovementAnimationTime() {
        return movementAnimationTimePerTile;
    }

    public void setMovementAnimationTime(int movementAnimationTime) {
        this.movementAnimationTimePerTile = movementAnimationTime;
    }
    
    public MovingSprite(Tile generalTile, int x, int y) {
        super(generalTile);
        this.x = x; this.y = y;
        this.targetX = x; this.targetY = y;
        this.previousX = x; this.previousY = y;
        this.movementX = x * MazeConst.TILE_WIDTH;
        this.movementY = y * MazeConst.TILE_HEIGHT;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPreviousX() {
        return previousX;
    }

    public void setPreviousX(int previousX) {
        this.previousX = previousX;
    }

    public int getPreviousY() {
        return previousY;
    }

    public void setPreviousY(int previousY) {
        this.previousY = previousY;
    }

    public int getTargetX() {
        return targetX;
    }

    public void setTargetX(int targetX) {
        this.targetX = targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public void setTargetY(int targetY) {
        this.targetY = targetY;
    }

    public float getCenteredMovementX() {
        return movementX + (MazeConst.TILE_WIDTH - spriteWidth) / 2;
    }

    public float getTrueMovementX() {
        return movementX;
    }

    public void setMovementX(float movementX) {
        this.movementX = movementX;
    }

    public float getCenteredMovementY() {
        return movementY + (MazeConst.TILE_HEIGHT - spriteHeight) / 2;
    }

    public float getTrueMovementY() {
        return movementY;
    }

    public void setMovementY(float movementY) {
        this.movementY = movementY;
    }
    
    public void moveSprite(int targetX, int targetY) {
        previousX = x;
        previousY = y;
        this.targetX = targetX;
        this.targetY = targetY;
        elapsedTime = 0;
        int distance = Math.abs(targetX - x) + Math.abs(targetY - y);
        totalMovementAnimationTimeNeeded = movementAnimationTimePerTile * distance;
    }
    
    public void turnSprite(int DIRECTION) {
        if((spriteDirection & DIRECTION) == spriteDirection || 
                (spriteDirection & MazeConst.OPPOSITE_OF(DIRECTION)) == spriteDirection) {
            
        } else {
            int temp = spriteHeight;
            spriteHeight = spriteWidth;
            spriteWidth = temp;
        }
        spriteDirection = DIRECTION;
    }
    
    @Override
    public Tile getFacingDirectionTile(int DIRECTION) {
        if(DIRECTION == -1) return super.getFacingDirectionTile(spriteDirection);
        else return super.getFacingDirectionTile(DIRECTION);
    }
    
    @Override
    public void update(int delta) {
        elapsedTime += delta;
        if(elapsedTime >= totalMovementAnimationTimeNeeded) {
            x = targetX;
            y = targetY;
            movementX = x * MazeConst.TILE_WIDTH;
            movementY = y * MazeConst.TILE_HEIGHT;
        } else {
            float movement = (float) elapsedTime / movementAnimationTimePerTile * MazeConst.TILE_WIDTH;
            if(x != targetX) {
                if(x > targetX) movement = -movement;
                movementX = x * MazeConst.TILE_WIDTH + movement;
                /*
                System.out.println("==========================================");
                System.out.println("x         = " + x + "(" + (x * MazeConst.TILE_WIDTH) + "," + ((x+1) * MazeConst.TILE_WIDTH) + ")");
                System.out.println("targetX   = " + targetX);
                System.out.println("movement  = " + movement);
                System.out.println("movementX = " + getCenteredMovementX() + "(" + getCenteredMovementX() + "," + (getCenteredMovementX() + spriteWidth) + ")");
                System.out.println("==========================================");
//                */
            } 
            if(y != targetY) {
                if(y > targetY) movement = -movement;
                movementY = y * MazeConst.TILE_HEIGHT + movement;
                /*
                System.out.println("==========================================");
                System.out.println("y         = " + y + "(" + (y * MazeConst.TILE_HEIGHT) + "," + ((y+1) * MazeConst.TILE_HEIGHT) + ")");
                System.out.println("targetY   = " + targetY);
                System.out.println("movement  = " + movement);
                System.out.println("movementY = " + getCenteredMovementY() + "(" + getCenteredMovementY() + "," + (getCenteredMovementY() + spriteHeight) + ")");
                System.out.println("==========================================");
//                */
            }
        }
    }

    public int getMovementYTile() {
        int movementYInTile = (int) getCenteredMovementY() / MazeConst.TILE_HEIGHT;
        return movementYInTile;
//        return Math.round(getCenteredMovementY() / MazeConst.TILE_HEIGHT);
    }

    public int getMovementXTile() {
        int movementXInTile = (int) getCenteredMovementX() / MazeConst.TILE_WIDTH;
        return movementXInTile;
//        return Math.round(getCenteredMovementX() / MazeConst.TILE_WIDTH);
    }
    
    public boolean isSpriteInTile(int x, int y) {
        // build the square coordinate of pixel. As long as the sprite exist in
        // the tile, return true;
        int leftXpx = x * MazeConst.TILE_WIDTH, rightXpx = (x + 1) * MazeConst.TILE_WIDTH;
        int upYpx = y * MazeConst.TILE_HEIGHT, downYpx = (y + 1) * MazeConst.TILE_HEIGHT;
//        boolean isTileXOK = movementX + spriteWidth >= leftXpx && movementX + spriteWidth <= rightXpx;
//        boolean isTileYOK = movementY + spriteHeight >= upYpx && movementY + spriteHeight <= downYpx;
        boolean isTileXOK = getCenteredMovementX() >= leftXpx && getCenteredMovementX() + spriteWidth <= rightXpx;
        boolean isTileYOK = getCenteredMovementY() >= upYpx && getCenteredMovementY() + spriteHeight <= downYpx;
//        System.out.println("IsSpriteInTile:" + (isTileXOK && isTileYOK));
        return isTileXOK && isTileYOK;
    }
    
    public void printCoordinates() {
        System.out.println("============================================");
        System.out.println("previousX = " + previousX + ", previousY = " + previousY);
        System.out.println("currentX = " + x + ", currentY = " + y);
        System.out.println("targetX = " + targetX + ", targetY = " + targetY);
        System.out.println("movementX:" + movementX + ", movementY:" + movementY);
        System.out.println("============================================");
    }
}
