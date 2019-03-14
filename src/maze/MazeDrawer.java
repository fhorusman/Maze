/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maze;

import maze.model.Character;
import maze.model.Spell;
import maze.sprite.MovingSprite;
import maze.sprite.Tile;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import maze.model.Action;
import maze.model.AttackEvent;
import maze.model.Fighter;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GLContext;
import util.NLog;

/**
 * This is the main class of the Maze Game.
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class MazeDrawer {
    private final String KTag = "MazeDrawer";
    private final int WIDTH = 1600;
    private final int HEIGHT = 960;
    private int FPS;
    private final int KEY_INPUT_PHASE = 1;
    private final int PC_MOVE_ACTION_PHASE = 2;
    private final int PC_SPECIAL_ACTION_PHASE = 3;
    private final int NPC_DETERMINE_ACTION_PHASE = 4;
    private final int NPC_MOVE_ACTION_PHASE = 5;
    private final int NPC_SPECIAL_ACTION_PHASE = 6;
    private int currentPhase = KEY_INPUT_PHASE;

    private long lastFrame;
    private long lastFPS;
    
    private Maze2DGenerator generator;
    private Floor cf;
    private List<Floor> floorCollections;
    
    private Map<Integer, Tile> tiles; // floor tiles
    private Map<Integer, Float> rotations; // floor tiles directions
    private final int mazeWidth = 50;       // max=20 with current screen width
    private final int mazeHeight = 30;      // max 15 with current screen height
//    private int x, y, targetX, targetY;
//    private float movementX, movementY;
    private int elapsedTime = MazeConst.MOVEMENT_ANIMATION_DURATION;
    
    private boolean isPaused = false;
    private boolean fog = true;

    public void start() throws LWJGLException {
        //1. Initialize Display
        try {
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.create();
        } catch (LWJGLException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        
        //2. Initialize OpenGL and start drawing
        initGL();
        initSprite();

        getDelta();
        lastFPS = getTime();

        while (!Display.isCloseRequested()) {
            int delta = getDelta();

            update(delta);
            //Render OpenGL

            if (Display.isActive() || Display.isVisible()) {
                renderGL();
            } else {
                // Only bother rendering if the window is dirty
                if (Display.isDirty()) {
                    renderGL();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            Display.update();
            Display.sync(60);
        }

        Display.destroy();
    }
    
    public Floor createMazeFloor(int mazeWidth, int mazeHeight) {
        Floor newFloor = new Floor();
        int x = pcSprite.getX(), y = pcSprite.getY();
        long seed = System.currentTimeMillis();
        // Create a maze using the generator
        int[][] grid = generator.run(mazeWidth, mazeHeight, x, y, seed);
        int[][] visitedGrid = new int[mazeHeight][mazeWidth];
        int[][] eventGrid = new int[mazeHeight][mazeWidth];
        
        if(cf == null) {
            newFloor.setFloorNumber(0);
        } else {
            newFloor.setFloorNumber(cf.getFloorNumber() + 1);
            grid[y][x] |= MazeConst.D;
        }
        
        // Count and save the coordinates of dead-end tile exist in maze.
        List<int[]> oneWayTiles = new ArrayList<>();
        for(int j = 0; j < grid.length; j++) {
            for(int i = 0; i < grid[j].length; i++) {
                if(!(y == j && x == i) && 
                        ((grid[j][i] | MazeConst.N) == MazeConst.N || 
                        (grid[j][i] | MazeConst.S) == MazeConst.S || 
                        (grid[j][i] | MazeConst.E) == MazeConst.E || 
                        (grid[j][i] | MazeConst.W) == MazeConst.W)) {
                    oneWayTiles.add(new int[]{j,i});
                }
            }
        }
        
        // Shuffle the dead-end tiles for putting the maze treasures randomly.
        Collections.shuffle(oneWayTiles, new Random(seed));
        int[] floorTile = oneWayTiles.get(0);
        int[] treasureTile = oneWayTiles.get(1);
        
        // The first tile should be stair up.
        grid[floorTile[0]][floorTile[1]] |= MazeConst.U;
        // The second tile is the treasure chest.
        eventGrid[treasureTile[0]][treasureTile[1]] |= MazeConst.TREASURE;
        
        // Made the other dead-end tiles as the spawn location of the monsters
        List<Character> characters = new ArrayList<>();
        System.out.println("oneWayTiles.size(): " + oneWayTiles.size());
        for(int i = 2; i < oneWayTiles.size(); i++) {
            int[] slimeTileCoord = oneWayTiles.get(i);
            int slimeX = slimeTileCoord[1], slimeY = slimeTileCoord[0];
            Character slimeChar = CharacterFactory.CreateBlueSlimeWithLevel(slime, "Blue Slime " + i, floorCollections.size() + 1, slimeX, slimeY);
            visitedGrid[slimeY][slimeX] |= MazeConst.UNPASSABLE;
            characters.add(slimeChar);
//            break;
        }
        
        List<AttackEvent> attackingMonster = new ArrayList<>();
        List<AttackEvent> attackingCharacter = new ArrayList<>();
        
        newFloor.setAttackingMonster(attackingMonster);
        newFloor.setAttackingCharacter(attackingCharacter);
        newFloor.setSeed(seed);
        newFloor.setGrid(grid);
        newFloor.setVisitedGrid(visitedGrid);
        newFloor.setEventGrid(eventGrid);
        newFloor.setCharacterSprites(characters);
        newFloor.setGenerator(generator);
        newFloor.setWidth(mazeWidth);
        newFloor.setHeight(mazeHeight);
        return newFloor;
    }
    
    private void proceedToNextFloor() {
        int x = pcSprite.getX(), y = pcSprite.getY();
        if(cf == null) {
            cf = createMazeFloor(mazeWidth, mazeHeight);
            cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
            seeDirectionalTiles(x, y);
            floorCollections.add(cf);
        } else if((cf.getGrid()[y][x] & MazeConst.U) == MazeConst.U) {
            if(cf.getFloorNumber() + 1 >= floorCollections.size()){
                if(cf.isCanGoUp()) {
                    cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;

                    cf = createMazeFloor(mazeWidth, mazeHeight);
                    cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
                    seeDirectionalTiles(x, y);
                    floorCollections.add(cf);
                } else {
                    return;
                }
            } else {
                cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
                cf = floorCollections.get(cf.getFloorNumber() + 1);
                cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
            }
        } else if((cf.getGrid()[y][x] & MazeConst.D) == MazeConst.D) {
            cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
            Floor nextFloor = floorCollections.get(cf.getFloorNumber() - 1);
            cf = nextFloor;
            cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
        }
        elapsedTime = 0;
    }
    
    private void seeDirectionalTiles(int targetX, int targetY) {
        // Make the target tiles seen
        cf.getVisitedGrid()[targetY][targetX] |= MazeConst.SEEN;
        // Look at north
        if((cf.getGrid()[targetY][targetX] & MazeConst.N) == MazeConst.N) {
            int dx = 0, dy = -1;
            // move specified direction tiles
            int frontX = targetX + dx, frontY = targetY + dy;
            while(frontX >= 0 && frontY >=0 && frontX < cf.getWidth() && frontY < cf.getHeight()) {
                // Skip if the specified target tiles has been passed before.
                if((cf.getVisitedGrid()[frontY][frontX] & MazeConst.SEEN) == MazeConst.SEEN) break;
                // Make the specified direction tiles seen.
                cf.getVisitedGrid()[frontY][frontX] |= MazeConst.SEEN;
                // Skip if there's no path to next direction tiles.
                if((cf.getGrid()[frontY][frontX] & MazeConst.N) != MazeConst.N) break;
                // move specified direction tiles
                frontX += dx; frontY += dy;
            }
        }
        // Look at east
        if((cf.getGrid()[targetY][targetX] & MazeConst.E) == MazeConst.E) {
            int dx = 1, dy = 0;
            int frontX = targetX + dx, frontY = targetY + dy;
            while(frontX >= 0 && frontY >=0 && frontX < cf.getWidth() && frontY < cf.getHeight()) {
                if((cf.getVisitedGrid()[frontY][frontX] & MazeConst.SEEN) == MazeConst.SEEN) break;
                cf.getVisitedGrid()[frontY][frontX] |= MazeConst.SEEN;
                if((cf.getGrid()[frontY][frontX] & MazeConst.E) != MazeConst.E) break;
                frontX += dx; frontY += dy;
            }
        }
        // Look at south
        if((cf.getGrid()[targetY][targetX] & MazeConst.S) == MazeConst.S) {
            int dx = 0, dy = 1;
            int frontX = targetX + dx, frontY = targetY + dy;
            while(frontX >= 0 && frontY >=0 && frontX < cf.getWidth() && frontY < cf.getHeight()) {
                if((cf.getVisitedGrid()[frontY][frontX] & MazeConst.SEEN) == MazeConst.SEEN) break;
                cf.getVisitedGrid()[frontY][frontX] |= MazeConst.SEEN;
                if((cf.getGrid()[frontY][frontX] & MazeConst.S) != MazeConst.S) break;
                frontX += dx; frontY += dy;
            }
        }
        // Look at west
        if((cf.getGrid()[targetY][targetX] & MazeConst.W) == MazeConst.W) {
            int dx = -1, dy = 0;
            int frontX = targetX + dx, frontY = targetY + dy;
            while(frontX >= 0 && frontY >=0 && frontX < cf.getWidth() && frontY < cf.getHeight()) {
                if((cf.getVisitedGrid()[frontY][frontX] & MazeConst.SEEN) == MazeConst.SEEN) break;
                cf.getVisitedGrid()[frontY][frontX] |= MazeConst.SEEN;
                if((cf.getGrid()[frontY][frontX] & MazeConst.W) != MazeConst.W) break;
                frontX += dx; frontY += dy;
            }
        }
    }
    
    private final int spellCooldown = 128;
    private int currentCooldown = 128;
    private final int hotkeyCooldown = 300;
    private int currentHotKeyCooldown = 300;
    
    public void update(int delta) {
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            isPaused = !isPaused;
        }
        if(isPaused) return;
        
        pcSprite.update(delta);
        for(Character monsterChara : cf.getCharacterSprites()) {
            monsterChara.update(delta);
        }
        
        int x = pcSprite.getX(), y = pcSprite.getY();
        int targetX = pcSprite.getTargetX(), targetY = pcSprite.getTargetY();
        int direct = pcSprite.getSpriteDirection();
        currentCooldown += delta;
        
        if(currentPhase == PC_SPECIAL_ACTION_PHASE) {
//            System.out.println("PC_SPECIAL_ACTION_PHASE");
            // check if there's casted spell
            if(castedSpell.size() > 0) {
                List<Spell> removedSpell = new ArrayList<>();
//                System.out.println("castedSpell.size()" + castedSpell.size());
                for(Spell spell : castedSpell) {
                    MovingSprite spellSprite = spell.getSprite();
                    spellSprite.update(delta);
                    int spellX = spellSprite.getMovementXTile(), spellY = spellSprite.getMovementYTile();
                    boolean isSpriteInTile = spellSprite.isSpriteInTile(spellX, spellY);
                    int dx = generator.getDX().get(MazeConst.OPPOSITE_OF(spellSprite.getSpriteDirection()));
                    int dy = generator.getDY().get(MazeConst.OPPOSITE_OF(spellSprite.getSpriteDirection()));
                    int previousX = spellX + dx;
                    int previousY = spellY + dy;
//                        System.out.println("previousX: " + previousX + ", previousY: " + previousY);
                    if((spellSprite.getSpriteDirection() & MazeConst.E) == MazeConst.E) {
                        previousX += 1;
                    } else if((spellSprite.getSpriteDirection() & MazeConst.S) == MazeConst.S) {
                        previousY += 1;
                    }
//                    System.out.println("spellX: " + spellX + ", spellY:" + spellY);
                    if(spellSprite.getCenteredMovementY() < 0 || 
                            spellSprite.getCenteredMovementY() >= mazeHeight * MazeConst.TILE_HEIGHT || 
                            spellSprite.getCenteredMovementX() < 0 || 
                            spellSprite.getCenteredMovementX() >= mazeWidth * MazeConst.TILE_WIDTH) {
//                        System.out.println("Went to the void");
                        removedSpell.add(spell);
                    } else if(spellSprite.getPreviousX() == spellX && 
                            spellSprite.getPreviousY() == spellY && 
                            isSpriteInTile) {
                        // check if spell has changed tile
//                        System.out.println("Spells still in the first tile");
                    } else if((cf.getVisitedGrid()[spellY][spellX] & MazeConst.UNPASSABLE) != 0 &&
                            (spellY != pcSprite.getY() || spellX != pcSprite.getX())) {
                        // if spell met with occupied grid, damage the creature and remove spell
                        
                        if((cf.getGrid()[previousY][previousX] & spellSprite.getSpriteDirection()) == 0
                                && !isSpriteInTile) {
                            // check if there's wall between the previous tile and the current tile
                            System.out.println(spell.getName() + " hit the wall then extinguished!");
                            removedSpell.add(spell);
                        } else {
                            List<Character> removedChara = new ArrayList<>();
                            Character chara = cf.getCharAt(spellX, spellY);
                            if(chara != null && chara.getX() == spellX && chara.getY() == spellY) {
                            // check if the spell made contact with monster
                            // create a square box of monster and box of spell
                                float leftXpx = chara.getCenteredMovementX(), rightXpx = chara.getCenteredMovementX() + chara.getSpriteWidth();
                                float upYpx = chara.getCenteredMovementY(), downYpx = chara.getCenteredMovementY() + chara.getSpriteHeight();
                                float sLeftXpx = spellSprite.getCenteredMovementX(), sRightXpx = spellSprite.getCenteredMovementX() + spellSprite.getSpriteWidth();
                                float sUpYpx = spellSprite.getCenteredMovementY(), sDownYpx = spellSprite.getCenteredMovementY() + spellSprite.getSpriteHeight();
                                if(MazeConst.isRectIntersect(leftXpx, rightXpx, upYpx, downYpx, sLeftXpx, sRightXpx, sUpYpx, sDownYpx)) {
//                                    System.out.println("Made Contact");
                                    pcSprite.getFighter().attack(pcSprite, chara, spell, cf);
                                    chara.turnSprite(MazeConst.OPPOSITE_OF(spellSprite.getSpriteDirection()));
                                    chara.setPcSeen(true);
                                    if(chara.getFighter().getCurrHealth() <= 0f) {
                                        combatWinEvent(spell.getOwner(), chara);
                                        cf.getVisitedGrid()[spellY][spellX] ^= MazeConst.UNPASSABLE;
                                        removedChara.add(chara);
                                    }
                                    removedSpell.add(spell);
                                } else {
//                                    System.out.println("Not Made Contact");
                                }
                            }
                            cf.getCharacterSprites().removeAll(removedChara);
                        }
                    } else {
//                        System.out.println("Spell Direction: " + MazeConst.toString(spellSprite.getSpriteDirection()));
//                        System.out.println("Opposite Direction: " + MazeConst.toString(MazeConst.OPPOSITE_OF(spellSprite.getSpriteDirection())));
                        
                        
                        if((cf.getGrid()[previousY][previousX] & spellSprite.getSpriteDirection()) == 0
                                && !isSpriteInTile) {
                            // check if there's wall between the previous tile and the current tile
                            System.out.println(spell.getName() + " hit the wall then extinguished!");
                            removedSpell.add(spell);
                        }
                    }
                }
                castedSpell.removeAll(removedSpell);
                if(castedSpell.isEmpty()) {
                    currentPhase = NPC_DETERMINE_ACTION_PHASE;
                }
            } else {
//                System.out.println("attackingCharacter.size(): " + cf.getAttackingCharacter().size());
                if(cf.getAttackingCharacter().size() > 0) {
                    List<AttackEvent> removedAttacker = new ArrayList<>();
                    for(AttackEvent charAttack: cf.getAttackingCharacter()) {
                        charAttack.getAttacker().getFighter().attack(charAttack.getAttacker(), charAttack.getDefender(), cf);
                        charAttack.getDefender().turnSprite(MazeConst.OPPOSITE_OF(charAttack.getAttacker().getSpriteDirection()));
                        charAttack.getDefender().setPcSeen(true);
                        if(charAttack.getDefender().getFighter().getCurrHealth() <= 0f) {
                            combatWinEvent(charAttack.getAttacker(), charAttack.getDefender());
                            cf.getVisitedGrid()[charAttack.getDefender().getY()][charAttack.getDefender().getX()] ^= MazeConst.UNPASSABLE;
                            cf.getCharacterSprites().remove(charAttack.getDefender());
                        }
                        removedAttacker.add(charAttack);
                    }
                    cf.getAttackingCharacter().removeAll(removedAttacker);
                    currentPhase = NPC_DETERMINE_ACTION_PHASE;
                } else {
                    currentPhase = NPC_DETERMINE_ACTION_PHASE;
                }
            }
        }
        
        if(currentPhase == NPC_SPECIAL_ACTION_PHASE) {
//            System.out.println("NPC_DETERMINE_ACTION_PHASE");
            // check if there's attacking monster
            if(cf.getAttackingMonster().size() > 0) {
                List<AttackEvent> removedAttacker = new ArrayList<>();
//                System.out.println("getAttackingMonster.size(): " + cf.getAttackingMonster().size());
                for(AttackEvent monsterAttack: cf.getAttackingMonster()) {
                    monsterAttack.getAttacker().getFighter().attack(monsterAttack.getAttacker(), monsterAttack.getDefender(), cf);
                    if(monsterAttack.getDefender().getFighter().getCurrHealth() <= 0f) {
                        combatWinEvent(monsterAttack.getAttacker(), monsterAttack.getDefender());
                        if(monsterAttack.getDefender() == pcSprite) {
                            System.out.println("GAME OVER!!");
                        }
                    }
                    removedAttacker.add(monsterAttack);
                }
                cf.getAttackingMonster().removeAll(removedAttacker);
                currentPhase = KEY_INPUT_PHASE;
            } else {
                currentPhase = KEY_INPUT_PHASE;
            }
        }
        
        currentHotKeyCooldown += delta;
        if(currentHotKeyCooldown >= hotkeyCooldown) {
            if(Keyboard.isKeyDown(Keyboard.KEY_M)) {
                fog = !fog;
                currentHotKeyCooldown = 0;
            }
        }
        
        elapsedTime += delta;
        if(elapsedTime >= MazeConst.MOVEMENT_ANIMATION_DURATION) {
            if(currentPhase == KEY_INPUT_PHASE) {
//                System.out.println("KEY_INPUT_PHASE");
                boolean onTheStair = false;
                // Check if pcSprite is currently on stair
                if((cf.getGrid()[y][x] & MazeConst.U) == MazeConst.U || 
                        (cf.getGrid()[y][x] & MazeConst.D) == MazeConst.D) {
                    onTheStair = true;
                }
                
                // Check if there's a treasure on the tile
                if((cf.getEventGrid()[y][x] & MazeConst.TREASURE) == MazeConst.TREASURE) {
                    cf.getEventGrid()[y][x] ^= MazeConst.TREASURE;
                    cf.getEventGrid()[y][x] |= MazeConst.TREASURE_OPENED;
                    cf.setCanGoUp(true);
                }

                if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    } else if((cf.getGrid()[y][x] & MazeConst.W) == MazeConst.W) {
                        targetX -= 1;
                        currentPhase = PC_MOVE_ACTION_PHASE;
                    } else if(onTheStair && (cf.getGrid()[y][x] & MazeConst.E) == MazeConst.E) {
                        proceedToNextFloor();
                    }
                    pcSprite.turnSprite(MazeConst.W);
                } else if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    } else if((cf.getGrid()[y][x] & MazeConst.E) == MazeConst.E) {
                        targetX += 1;
                        currentPhase = PC_MOVE_ACTION_PHASE;
                    } else if(onTheStair && (cf.getGrid()[y][x] & MazeConst.W) == MazeConst.W) {
                        proceedToNextFloor();
                    }
                    pcSprite.turnSprite(MazeConst.E);
                } else if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    } else if((cf.getGrid()[y][x] & MazeConst.S) == MazeConst.S) {
                        targetY += 1;
                        currentPhase = PC_MOVE_ACTION_PHASE;
                    } else if(onTheStair && (cf.getGrid()[y][x] & MazeConst.N) == MazeConst.N) {
                        proceedToNextFloor();
                    }
                    pcSprite.turnSprite(MazeConst.S);
                } else if(Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    } else if((cf.getGrid()[y][x] & MazeConst.N) == MazeConst.N) {
                        targetY -= 1;
                        currentPhase = PC_MOVE_ACTION_PHASE;
                    } else if(onTheStair && (cf.getGrid()[y][x] & MazeConst.S) == MazeConst.S) {
                        proceedToNextFloor();
                    }
                    pcSprite.turnSprite(MazeConst.N);
                } else if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
                    pcSprite.turnSprite(MazeConst.N);
                } else if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
                    pcSprite.turnSprite(MazeConst.S);
                } else if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
                    pcSprite.turnSprite(MazeConst.E);
                } else if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
                    pcSprite.turnSprite(MazeConst.W);
                } else if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                    if(currentCooldown >= spellCooldown) {
                    // if shift is held, unleash special attack
                        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                            currentCooldown = 0;
                            if(pcSprite.getFighter().getCurrMana() <= 0) {
                                NLog.log(KTag, "attack", "Not enough mana! Mana: " + pcSprite.getFighter().getCurrMana() + "/" + pcSprite.getFighter().getCurrMaxMana());
                            } else {
                                MovingSprite fireballSprite = new MovingSprite(fireball, pcSprite.getX(), pcSprite.getY());
                                fireballSprite.setSpriteHeight(12);
                                fireballSprite.setSpriteWidth(8);
                                fireballSprite.turnSprite(direct);
                                fireballSprite.setMovementAnimationTime(spellCooldown);
                                switch(direct) {
                                    case MazeConst.N:
                                        fireballSprite.moveSprite(fireballSprite.getX(), -1);
                                        break;
                                    case MazeConst.S:
                                        fireballSprite.moveSprite(fireballSprite.getX(), mazeHeight);
                                        break;
                                    case MazeConst.E:
                                        fireballSprite.moveSprite(mazeWidth, fireballSprite.getY());
                                        break;
                                    case MazeConst.W:
                                        fireballSprite.moveSprite(-1, fireballSprite.getY());
                                        break;
                                }

                                Spell fireballSpell = new Spell("Fireball", 5, 1, fireballSprite, pcSprite);
                                castedSpell.add(fireballSpell);
                                pcSprite.getFighter().setCurrMana(pcSprite.getFighter().getCurrMana() - fireballSpell.getManaCost());
                                
                                NLog.log(KTag, "attack", "Casted " + fireballSpell.getName() + " from (" + 
                                        pcSprite.getX() + "," + pcSprite.getY() + ") toward " + 
                                        MazeConst.toString(pcSprite.getSpriteDirection()) + 
                                        ". Mana: " + pcSprite.getFighter().getCurrMana() + "/" + pcSprite.getFighter().getCurrMaxMana());
                                
                                currentPhase = PC_SPECIAL_ACTION_PHASE;
                            }
                        } else {
                            // Do normal attack
                            NLog.log(KTag, "attack", "Melee attack from (" + pcSprite.getX() + "," + pcSprite.getY() + ") toward " + MazeConst.toString(pcSprite.getSpriteDirection()));

                            int pcX = pcSprite.getX();
                            int pcY = pcSprite.getY();
                            // check if direction is passable
                            if ((cf.getGrid()[pcY][pcX] & direct) != 0) {
                                pcX += generator.getDX().get(direct);
                                pcY += generator.getDY().get(direct);
                                // check if any monster is in the position
                                for(Character monsterChara : cf.getCharacterSprites()) {
                                    int msX = monsterChara.getX(), msY = monsterChara.getY();

                                    if(msX == pcX && msY == pcY) {
                                        cf.getAttackingCharacter().add(new AttackEvent(pcSprite, monsterChara));
                                        break;
                                    }
                                }
                            }
                            currentCooldown = 0;
                            currentPhase = PC_SPECIAL_ACTION_PHASE;
                        }
                    }
                }
            }
            if(currentPhase == PC_MOVE_ACTION_PHASE) {
//                System.out.println("PC_MOVE_ACTION_PHASE");
                if(((pcSprite.getTargetX() != targetX || pcSprite.getTargetY() != targetY) && 
                        (cf.getVisitedGrid()[targetY][targetX] & MazeConst.UNPASSABLE) != 0)) {
                    currentPhase = KEY_INPUT_PHASE;
                } else {
                    cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
                    cf.getVisitedGrid()[targetY][targetX] ^= MazeConst.UNPASSABLE;
                    pcSprite.moveSprite(targetX, targetY);
                    seeDirectionalTiles(targetX, targetY);
                    elapsedTime = 0;
                    currentPhase = NPC_DETERMINE_ACTION_PHASE;
                }
            }

            if(currentPhase == NPC_DETERMINE_ACTION_PHASE) {
//                System.out.println("NPC_DETERMINE_ACTION_PHASE");
                CHARA: for(Character monsterChara : cf.getCharacterSprites()) {
                    Action action = monsterChara.getAi().takeTurn(monsterChara, pcSprite, cf);
//                    System.out.println(monsterChara.getName() + " action = " + action);
                }
                currentPhase = NPC_SPECIAL_ACTION_PHASE;
            }
        }
        updateFPS();
    }

    public void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            Display.setTitle("FPS: " + FPS);
            FPS = 0; //reset the FPS counter
            lastFPS += 1000; //add one second
        }
        FPS++;
    }

    public int getDelta() {
        long time = getTime();
        int delta = (int) (time - lastFrame);
        lastFrame = time;
        return delta;
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    Tile oneWay, twoWay, cornerWay, threeWay, fourWay, unvisitedTile, stairUp, stairDown;
    Tile pcFaceLeft, pcFaceRight, slime, fireball;
    Tile treasure, treasureOpened, healingPotion, manaPotion;
    int tileId;
    
    private Character pcSprite;
    private List<Spell> castedSpell;
    
    public void initSprite() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("image/Game2.png");
        try {
            floorCollections = new ArrayList<>();
            generator = Maze2DGenerator.getInstance();

            BufferedImage test = ImageIO.read(url);
            Graphics2D g2d = test.createGraphics();
            g2d.setColor(new Color(1.0f, 1.0f, 1.0f, 1f));
            tileId = loadTexture(test);

            oneWay = new Tile(0, 0, 32, 32, test.getWidth(), test.getHeight(), tileId); // Opened way facing north
            twoWay = new Tile(0, 32, 32, 32, test.getWidth(), test.getHeight(), tileId); // Facing north and south
            cornerWay = new Tile(32, 32, 32, 32, test.getWidth(), test.getHeight(), tileId); // Facing east and south
            threeWay = new Tile(32, 0, 32, 32, test.getWidth(), test.getHeight(), tileId); // Facing north, south, and east
            fourWay = new Tile(64, 0, 32, 32, test.getWidth(), test.getHeight(), tileId); // Facing all directions;
            unvisitedTile = new Tile(96,0,32,32,test.getWidth(), test.getHeight(), tileId); // Unvisited tile image;
            
            stairUp = new Tile(0,64,32,32,test.getWidth(), test.getHeight(), tileId); // Stair Up facing north
            stairDown = new Tile(32,64,32,32,test.getWidth(), test.getHeight(), tileId); // Stair down facing north
            treasure = new Tile(0,96,32,32,test.getWidth(), test.getHeight(), tileId); // Locked chest facing north
            treasureOpened = new Tile(32,96,32,32,test.getWidth(), test.getHeight(), tileId); // Open chest facing north
            healingPotion = new Tile(64,96,32,32,test.getWidth(), test.getHeight(), tileId); // Locked chest facing north
            manaPotion = new Tile(96,96,32,32,test.getWidth(), test.getHeight(), tileId); // Open chest facing north
            
            pcFaceLeft = new Tile(71, 35, 18, 26, test.getWidth(), test.getHeight(), tileId); // The character to be moved facing left
            pcFaceRight = new Tile(103, 35, 18, 26, test.getWidth(), test.getHeight(), tileId); // The character to be moved facing right;
//            fireball = new Tile(64, 64, 32, 32, test.getWidth(), test.getHeight(), tileId); // Fireball facing north
            fireball = new Tile(76, 76, 8, 12, test.getWidth(), test.getHeight(), tileId); // Fireball facing north
            slime = new Tile(104, 72, 16, 16, test.getWidth(), test.getHeight(), tileId); // Monster facing south
            
            tiles = new HashMap();
            rotations = new HashMap();
            
            tiles.put(1, oneWay);       rotations.put(1, 0f);
            tiles.put(2, oneWay);       rotations.put(2, 180f);
            tiles.put(3, twoWay);       rotations.put(3, 0f);
            tiles.put(4, oneWay);       rotations.put(4, 90f);
            tiles.put(5, cornerWay);    rotations.put(5, -90f);
            tiles.put(6, cornerWay);    rotations.put(6, 0f);
            tiles.put(7, threeWay);     rotations.put(7, 0f);
            tiles.put(8, oneWay);       rotations.put(8, -90f);
            tiles.put(9, cornerWay);    rotations.put(9, 180f);
            tiles.put(10, cornerWay);   rotations.put(10, 90f);
            tiles.put(11, threeWay);    rotations.put(11, 180f);
            tiles.put(12, twoWay);      rotations.put(12, 90f);
            tiles.put(13, threeWay);    rotations.put(13, -90f);
            tiles.put(14, threeWay);    rotations.put(14, 90f);
            tiles.put(15, fourWay);     rotations.put(15, 0f);
            tiles.put(17, stairUp);     rotations.put(17, 0f);
            tiles.put(18, stairUp);     rotations.put(18, 180f);
            tiles.put(20, stairUp);     rotations.put(20, 90f);
            tiles.put(24, stairUp);     rotations.put(24, -90f);
            tiles.put(33, stairDown);     rotations.put(33, 0f);
            tiles.put(34, stairDown);     rotations.put(34, 180f);
            tiles.put(36, stairDown);     rotations.put(36, 90f);
            tiles.put(40, stairDown);     rotations.put(40, -90f);
            
            Fighter pcFighter = new Fighter(1, 50f, 10f, 1f, 1f, 1f, 0);
            pcFighter.setPotentials(4, 3, 0.5f, 0.5f, 2, 1);
            pcFighter.setCurrLevel(1);
            
            pcSprite = new Character(pcFaceLeft, 0, 0, "Gandalph", pcFighter);
            pcSprite.setFaceWest(pcFaceLeft);
            pcSprite.setFaceEast(pcFaceRight);
            pcSprite.setSpriteHeight(26);
            pcSprite.setSpriteWidth(18);
            pcSprite.setMovementAnimationTime(MazeConst.MOVEMENT_ANIMATION_DURATION);
            pcSprite.setAutomaticTurnEnabled(false);
            pcSprite.turnSprite(MazeConst.E);
            
            castedSpell = new ArrayList<>();
            
            proceedToNextFloor();
        } catch (IOException ex) {
            Logger.getLogger(MazeDrawer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initGL() {
            int maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
            System.out.println("maxSize = " + maxSize);
            boolean npotSupported = GLContext.getCapabilities().GL_ARB_texture_non_power_of_two;
            System.out.println("npotSupported = " + npotSupported);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
//            glOrtho(0, WIDTH, 0, HEIGHT, 1, -1);
            glOrtho(0, WIDTH, HEIGHT, 0, -1, 1); 
            glMatrixMode(GL_MODELVIEW);
            glViewport(0, 0, Display.getWidth(), Display.getHeight());
            glDisable(GL_DEPTH_TEST);
//            glDisable(GL_LIGHTING);
            glEnable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glClearColor(0f, 0f, 0f, 0f);
    }
    
    public void renderGL() {
        if(isPaused) return;
        //Clear the screen and depth buffer
//        glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
//        glClear(GL_COLOR_BUFFER_BIT);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        
        glBindTexture(GL_TEXTURE_2D, tileId);
        for(int y = 0; y < cf.getGrid().length; y++) {
            for(int x = 0; x < cf.getGrid()[y].length; x++) {
                if((cf.getVisitedGrid()[y][x] & MazeConst.SEEN) != 0 || !fog) {
                    drawTile(tiles.get(cf.getGrid()[y][x]), x * 32, y * 32, 0, 32, 32, rotations.get(cf.getGrid()[y][x]));
                
                    if((cf.getEventGrid()[y][x] & MazeConst.TREASURE) != 0) {
                        drawTile(treasure, x * 32, y * 32, 0, 32, 32);
                    } else if((cf.getEventGrid()[y][x] & MazeConst.TREASURE_OPENED) != 0) {
                        drawTile(treasureOpened, x * 32, y * 32, 0, 32, 32);
                    }
                }
        //        
                
                 
//                if((cf.getVisitedGrid()[y][x] & MazeConst.UNPASSABLE) != 0) {
//                    drawTile(manaPotion, x * 32, y * 32, 0, 32, 32, 0);
//                }
//                if((cf.getVisitedGrid()[y][x] & MazeConst.SEEN) == 0) {
//                    drawTile(unvisitedTile, x * 32, y * 32, 0, 32, 32,0);
//                }
            }
        }
        for(Character monsterChara : cf.getCharacterSprites()) {
                    if(
//                            (monsterChara.isSpriteInTile(monsterChara.getX(), monsterChara.getY()) &&
//                            (cf.getVisitedGrid()[monsterChara.getY()][monsterChara.getX()] & MazeConst.SEEN) == MazeConst.SEEN)
//                            ||
//                            (monsterChara.isSpriteInTile(monsterChara.getTargetX(), monsterChara.getTargetY()) &&
//                            (cf.getVisitedGrid()[monsterChara.getTargetY()][monsterChara.getTargetX()] & MazeConst.SEEN) == MazeConst.SEEN)) 
                        
                            ((cf.getVisitedGrid()[monsterChara.getY()][monsterChara.getX()] & MazeConst.SEEN) == MazeConst.SEEN ||
                            (cf.getVisitedGrid()[monsterChara.getTargetY()][monsterChara.getTargetX()] & MazeConst.SEEN) == MazeConst.SEEN)
                            || !fog
                    ) {
                drawTile(monsterChara.getFacingDirectionTile(-1), 
                        monsterChara.getCenteredMovementX(), monsterChara.getCenteredMovementY(), 
                        0, monsterChara.getSpriteWidth(), monsterChara.getSpriteHeight());
                    }
//
        }

        for(Spell casted : castedSpell) {
            MovingSprite spellSprite = casted.getSprite();
            drawTile(spellSprite.getFacingDirectionTile(spellSprite.getSpriteDirection()), spellSprite.getCenteredMovementX(), spellSprite.getCenteredMovementY(), 0, spellSprite.getSpriteWidth(), spellSprite.getSpriteHeight());
        }
        
//        for(int y = 0; y < cf.getGrid().length; y++) {
//            for(int x = 0; x < cf.getGrid()[y].length; x++) {
//                if((cf.getVisitedGrid()[y][x] & MazeConst.SEEN) == 0) {
//                    drawTile(unvisitedTile, x * 32, y * 32, 0, 32, 32,0);
//                }
//            }
//        }
        
        drawTile(pcSprite.getFacingDirectionTile(-1), pcSprite.getCenteredMovementX(), pcSprite.getCenteredMovementY(), 0, pcSprite.getSpriteWidth(), pcSprite.getSpriteHeight());
    }
    
    private static void drawTile(Tile tile, float posX, float posY, float posZ, int tileWidth, int tileHeight, float rotation) {
        glPushMatrix();
        glTranslatef(posX, posY, posZ);
        glTranslatef(tileWidth / 2, tileHeight / 2, posZ);
        glRotatef(rotation, 0f, 0f, 1f);
        glScalef(1f, 1f, 0f);
        glTranslatef(-tileWidth / 2, -tileHeight / 2, posZ);
        glBegin(GL_QUADS);
        {
            glTexCoord2f(tile.getU(), tile.getV()); // Up left Coordinat
            glVertex2f(0, 0);

            glTexCoord2f(tile.getU2(), tile.getV()); // Up right Coordinat
            glVertex2f(tileWidth, 0);

            glTexCoord2f(tile.getU2(), tile.getV2()); // Down right coordinat
            glVertex2f(tileWidth, tileHeight);

            glTexCoord2f(tile.getU(), tile.getV2()); // Down left coordinat
            glVertex2f(0, tileHeight);
        }
        glEnd();
        glPopMatrix();
    }
    
    private static void drawTile(Tile tile, float posX, float posY, float posZ, int tileWidth, int tileHeight) {
        glPushMatrix();
        glTranslatef(posX, posY, posZ);
        glTranslatef(tileWidth / 2, tileHeight / 2, posZ);
        glRotatef(tile.getRotation(), 0f, 0f, 1f);
        glScalef(1f, 1f, 0f);
        glTranslatef(-tileWidth / 2, -tileHeight / 2, posZ);
        glBegin(GL_QUADS);
        {
            glTexCoord2f(tile.getU(), tile.getV()); // Up left Coordinat
            glVertex2f(0, 0);

            glTexCoord2f(tile.getU2(), tile.getV()); // Up right Coordinat
            glVertex2f(tileWidth, 0);

            glTexCoord2f(tile.getU2(), tile.getV2()); // Down right coordinat
            glVertex2f(tileWidth, tileHeight);

            glTexCoord2f(tile.getU(), tile.getV2()); // Down left coordinat
            glVertex2f(0, tileHeight);
        }
        glEnd();
        glPopMatrix();
    }

    /**
     * Set the display mode to be used
     *
     * @param width The width of the display required
     * @param height The height of the display required
     * @param fullscreen True if we want fullscreen mode
     */
    public void setDisplayMode(int width, int height, boolean fullscreen) {

        // return if requested DisplayMode is already set
        if ((Display.getDisplayMode().getWidth() == width)
                && (Display.getDisplayMode().getHeight() == height)
                && (Display.isFullscreen() == fullscreen)) {
            return;
        }

        try {
            DisplayMode targetDisplayMode = null;

            if (fullscreen) {
                DisplayMode[] modes = Display.getAvailableDisplayModes();
                int freq = 0;
                for (DisplayMode current : modes) {
                    if ((current.getWidth() == width) && (current.getHeight() == height)) {
                        if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
                            if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
                                targetDisplayMode = current;
                                freq = targetDisplayMode.getFrequency();
                            }
                        }
                        
                        // if we've found a match for bpp and frequence against the 
                        // original display mode then it's probably best to go for this one
                        // since it's most likely compatible with the monitor
                        if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel())
                                && (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
                            targetDisplayMode = current;
                            break;
                        }
                    }
                }
            } else {
                targetDisplayMode = new DisplayMode(width, height);
            }

            if (targetDisplayMode == null) {
                System.out.println("Failed to find value mode: " + width + "x" + height + " fs=" + fullscreen);
                return;
            }

            Display.setDisplayMode(targetDisplayMode);
            Display.setFullscreen(fullscreen);

        } catch (LWJGLException e) {
            System.out.println("Unable to setup mode " + width + "x" + height + " fullscreen=" + fullscreen + e);
        }
    }

    private static final int BYTES_PER_PIXEL = 4;
    public static int loadTexture(BufferedImage image) {

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL); //4 for RGBA, 3 for RGB

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

        // You now have a ByteBuffer filled with the color data of each pixel.
        // Now just create a texture ID and bind it. Then you can load it using 
        // whatever OpenGL method you want, for example:
        int textureID = glGenTextures(); //Generate texture ID
        glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID

        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        //Return the texture ID so we can bind it later again
        return textureID;
    }

    private void combatWinEvent(Character attacker, Character defender) {
        // calculate exp
        NLog.log(KTag, "combatWinEvent", attacker.getName() + " win the battle ");
        attacker.getFighter().setCurrExp(attacker.getFighter().getCurrExp() + defender.getFighter().getCurrExpValue());
        NLog.log(KTag, "combatWinEvent", attacker.getName() + " gained " + defender.getFighter().getCurrExpValue() + 
                " exp. Curr: " + attacker.getFighter().getCurrExp() + "/" + attacker.getFighter().getCurrNextLevelExp());
        
        while(attacker.getFighter().getCurrExp() >= attacker.getFighter().getCurrNextLevelExp()) {
            levelUp(attacker);
        }
        
        // get loot
    }
    
    public void levelUp(Character character) {
        character.getFighter().setCurrLevel(character.getFighter().getCurrLevel() + 1);
        float health = character.getFighter().getCurrMaxHealth() + character.getFighter().getAddHealth();
        float mana = character.getFighter().getCurrMaxMana() + character.getFighter().getAddMana();
        float strength = character.getFighter().getCurrStrength() + character.getFighter().getAddStrength();
        float agility = character.getFighter().getCurrAgility() + character.getFighter().getAddAgility();
        float intelligence = character.getFighter().getCurrIntelligence() + character.getFighter().getAddIntelligence();
        int currExpValue = character.getFighter().getCurrExpValue() + character.getFighter().getAddExpValue();
        int nextLevelExp = Math.round((character.getFighter().getCurrAgility() + 
                character.getFighter().getCurrIntelligence() + character.getFighter().getCurrStrength() + 
                character.getFighter().getCurrExpValue()) * character.getFighter().getCurrLevel());
        
        NLog.log(KTag, "levelUp", 
                "###################################");
        NLog.log(KTag, "levelUp", 
                character.getName() + " grew to " + character.getFighter().getCurrLevel() + "!!!");
        NLog.log(KTag, "levelUp", 
                "Health: " + character.getFighter().getCurrMaxHealth() + " -> " + health + " \n" +
                "Mana:   " + character.getFighter().getCurrMaxMana() + " -> " + mana + " \n" +
                "Str:    " + character.getFighter().getCurrStrength() + " -> " + strength + " \n" +
                "Agi:    " + character.getFighter().getCurrAgility() + " -> " + agility + " \n" +
                "Int:    " + character.getFighter().getCurrIntelligence()+ " -> " + intelligence + " \n" +
                "Next:   " + character.getFighter().getCurrExp() + "/" + nextLevelExp + "\n" +
                "Health and Mana are fully restored.");
        NLog.log(KTag, "levelUp", 
                "###################################");
        character.getFighter().setCurrMaxHealth(health);
        character.getFighter().setCurrHealth(health);
        character.getFighter().setCurrMaxMana(mana);
        character.getFighter().setCurrMana(mana);
        character.getFighter().setCurrStrength(strength);
        character.getFighter().setCurrAgility(agility);
        character.getFighter().setCurrIntelligence(intelligence);
        character.getFighter().setCurrExpValue(currExpValue);
        character.getFighter().setCurrNextLevelExp(nextLevelExp);
    }
}
