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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import maze.model.AttackEvent;
import maze.model.Monster;
import maze.model.MonsterType;
import maze.sprite.Sprite;
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
    private final int WIDTH = 640;
    private final int HEIGHT = 480;
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
    private final int mazeWidth = 20;       // max=20 with current screen width
    private final int mazeHeight = 15;      // max 15 with current screen height
//    private int x, y, targetX, targetY;
//    private float movementX, movementY;
    private final int movementAnimationTime = 200;
    private int elapsedTime = movementAnimationTime;
    
    private boolean isPaused = false;

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
    
    public Floor createMazeFloor() {
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
                if(y != j && x != i && (grid[j][i] == 1 || grid[j][i] == 2 || grid[j][i] == 4 || grid[j][i] == 8)) {
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
        for(int i = 2; i < oneWayTiles.size(); i++) {
            int[] slimeTileCoord = oneWayTiles.get(i);
            int slimeX = slimeTileCoord[1], slimeY = slimeTileCoord[0];
            MovingSprite slimeSprite = new MovingSprite(slime, slimeX, slimeY);
            slimeSprite.setSpriteHeight(16);
            slimeSprite.setSpriteWidth(16);
            slimeSprite.setMovementAnimationTime(movementAnimationTime);
            slimeSprite.setFaceEast(slime);
            slimeSprite.setFaceNorth(slime);
            slimeSprite.setFaceSouth(slime);
            slimeSprite.setFaceWest(slime);
            visitedGrid[slimeY][slimeX] |= MazeConst.UNPASSABLE;
            Monster slimeChar = CharacterFactory.CreateMonsterWithLevel("Blue Slime " + i, floorCollections.size() + 1, slimeSprite, MonsterType.BLUE_SLIME);
            characters.add(slimeChar);
//            break;
        }
        
        newFloor.setSeed(seed);
        newFloor.setGrid(grid);
        newFloor.setVisitedGrid(visitedGrid);
        newFloor.setEventGrid(eventGrid);
        newFloor.setCharacterSprites(characters);
        return newFloor;
    }
    
    private void proceedToNextFloor() {
        int x = pcSprite.getX(), y = pcSprite.getY();
        if(cf == null) {
            cf = createMazeFloor();
            cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
            floorCollections.add(cf);
        } else if((cf.getGrid()[y][x] & MazeConst.U) != 0) {
            if(cf.getFloorNumber() + 1 >= floorCollections.size()){
                cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
                
                cf = createMazeFloor();
                cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
                floorCollections.add(cf);
            } else {
                cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
                cf = floorCollections.get(cf.getFloorNumber() + 1);
                cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
            }
        } else if((cf.getGrid()[y][x] & MazeConst.D) != 0) {
            cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
            Floor nextFloor = floorCollections.get(cf.getFloorNumber() - 1);
            cf = nextFloor;
            cf.getVisitedGrid()[y][x] ^= MazeConst.UNPASSABLE;
        }
        
        try {
            Thread.sleep(400);
        } catch (InterruptedException ex) {
            Logger.getLogger(MazeDrawer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private final int spellCooldown = 300;
    private boolean isForbidAction = false;
    private boolean waitUntilAnimationEnded = false;
    private int currentCooldown = 300;
    
    public void update(int delta) {
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            isPaused = !isPaused;
        }
        if(isPaused) return;
        
        pcSprite.update(delta);
        for(Character monsterChara : cf.getCharacterSprites()) {
            monsterChara.getSprite().update(delta);
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
                        System.out.println("Went to the void");
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
                            System.out.println("Hit the wall");
                            removedSpell.add(spell);
                        } else {
                            List<Character> removedChara = new ArrayList<>();
                            for(Character chara : cf.getCharacterSprites()) {
                                if(chara.getSprite().getX() == spellX && chara.getSprite().getY() == spellY) {
                                // check if the spell made contact with monster
                                // create a square box of monster and box of spell
                                    float leftXpx = chara.getSprite().getCenteredMovementX(), rightXpx = chara.getSprite().getCenteredMovementX() + chara.getSprite().getSpriteWidth();
                                    float upYpx = chara.getSprite().getCenteredMovementY(), downYpx = chara.getSprite().getCenteredMovementY() + chara.getSprite().getSpriteHeight();
                                    float sLeftXpx = spellSprite.getCenteredMovementX(), sRightXpx = spellSprite.getCenteredMovementX() + spellSprite.getSpriteWidth();
                                    float sUpYpx = spellSprite.getCenteredMovementY(), sDownYpx = spellSprite.getCenteredMovementY() + spellSprite.getSpriteHeight();
                                    boolean madeContact = false;
    //                                System.out.println("mon.top(" + leftXpx + "," + upYpx + ") - (" + rightXpx + "," + upYpx + ")");
    //                                System.out.println("mon.bottom(" + leftXpx + "," + downYpx + ") - (" + rightXpx + "," + downYpx + ")");
    //                                System.out.println("mon.top(" + sLeftXpx + "," + sUpYpx + ") - (" + sRightXpx + "," + sUpYpx + ")");
    //                                System.out.println("mon.bottom(" + sLeftXpx + "," + sDownYpx + ") - (" + sRightXpx + "," + sDownYpx + ")");

                                    // check for the left up corner
                                    if(sLeftXpx >= leftXpx && sLeftXpx <= rightXpx &&
                                            sUpYpx >= upYpx && sUpYpx <= downYpx) {
                                        madeContact = true;
                                    }
                                    // check for the right up corner
                                    else if(sRightXpx >= leftXpx && sRightXpx <= rightXpx &&
                                            sUpYpx >= upYpx && sUpYpx <= downYpx) {
                                        madeContact = true;
                                    }
                                    // check for the left down corner
                                    else if(sLeftXpx >= leftXpx && sLeftXpx <= rightXpx &&
                                            sDownYpx >= upYpx && sDownYpx <= downYpx) {
                                        madeContact = true;
                                    }
                                    // check for the right down corner
                                    else if(sRightXpx >= leftXpx && sRightXpx <= rightXpx &&
                                            sDownYpx >= upYpx && sDownYpx <= downYpx) {
                                        madeContact = true;
                                    }
                                    if(madeContact) {
                                        System.out.println("Made Contact");
                                        combatEvent(spell.getOwner(), chara, spell);
                                        if(chara.getCurrHealth() <= 0f) {
                                            combatWinEvent(spell.getOwner(), chara);
                                            cf.getVisitedGrid()[spellY][spellX] ^= MazeConst.UNPASSABLE;
                                            removedChara.add(chara);
                                            removedSpell.add(spell);
                                        }
                                    } else {
                                        System.out.println("Not Made Contact");
                                    }
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
                            System.out.println("Hit the wall");
                            removedSpell.add(spell);
                        }
                    }
                }
                castedSpell.removeAll(removedSpell);
                if(castedSpell.isEmpty()) {
                    currentPhase = NPC_DETERMINE_ACTION_PHASE;
                }
            } else {
                if(attackingCharacter.size() > 0) {
                    List<AttackEvent> removedAttacker = new ArrayList<>();
                    for(AttackEvent charAttack: attackingCharacter) {
                        combatEvent(charAttack.getAttacker(), charAttack.getDefender());
                        if(charAttack.getDefender().getCurrHealth() <= 0f) {
                            combatWinEvent(charAttack.getAttacker(), charAttack.getDefender());
                            cf.getVisitedGrid()[charAttack.getDefender().getSprite().getY()][charAttack.getDefender().getSprite().getX()] ^= MazeConst.UNPASSABLE;
                            cf.getCharacterSprites().remove(charAttack.getDefender());
                        }
                        removedAttacker.add(charAttack);
                    }
                    attackingCharacter.removeAll(removedAttacker);
                    currentPhase = NPC_DETERMINE_ACTION_PHASE;
                } else {
                    currentPhase = NPC_DETERMINE_ACTION_PHASE;
                }
            }
        }
        
        if(currentPhase == NPC_SPECIAL_ACTION_PHASE) {
//            System.out.println("NPC_DETERMINE_ACTION_PHASE");
            // check if there's attacking monster
            if(attackingMonster.size() > 0) {
                List<AttackEvent> removedAttacker = new ArrayList<>();
                for(AttackEvent monsterAttack: attackingMonster) {
                    combatEvent(monsterAttack.getAttacker(), monsterAttack.getDefender());
                    if(monsterAttack.getDefender().getCurrHealth() <= 0f) {
                        combatWinEvent(monsterAttack.getAttacker(), monsterAttack.getDefender());
                        if(monsterAttack.getDefender() == pc) {
                            System.out.println("GAME OVER!!");
                        }
                    }
                    removedAttacker.add(monsterAttack);
                }
                attackingMonster.removeAll(removedAttacker);
                currentPhase = KEY_INPUT_PHASE;
            } else {
                currentPhase = KEY_INPUT_PHASE;
            }
        }
        
        elapsedTime += delta;
        if(elapsedTime >= movementAnimationTime) {
            if(currentPhase == KEY_INPUT_PHASE) {
//                System.out.println("KEY_INPUT_PHASE");
                boolean onTheStair = false;
                // Check if pcSprite is currently on stair
                if((cf.getGrid()[y][x] & MazeConst.U) == MazeConst.U || 
                        (cf.getGrid()[y][x] & MazeConst.D) == MazeConst.D) {
                    onTheStair = true;
                }
                
                // Check if there's a treasure on the tile
                if((cf.getEventGrid()[y][x] & MazeConst.TREASURE) != 0) {
                    cf.getEventGrid()[y][x] ^= MazeConst.TREASURE;
                    cf.getEventGrid()[y][x] |= MazeConst.TREASURE_OPENED;
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
                            if(pc.getCurrMana() <= 0) {
                                NLog.log(KTag, "attack", "Not enough mana! Mana: " + pc.getCurrMana() + "/" + pc.getCurrMaxMana());
                            } else {
                                NLog.log(KTag, "attack", "Firing a spell from (" + pcSprite.getX() + "," + pcSprite.getY() + ") toward " + MazeConst.toString(pcSprite.getSpriteDirection()));
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

                                Spell fireballSpell = new Spell("Fireball", 5, fireballSprite, pc);
                                pc.setCurrMana(pc.getCurrMana() - 1);
                                castedSpell.add(fireballSpell);
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
                                    MovingSprite monsterSp = monsterChara.getSprite();
                                    int msX = monsterSp.getX(), msY = monsterSp.getY();

                                    if(msX == pcX && msY == pcY) {
                                        attackingCharacter.add(new AttackEvent(pc, monsterChara));
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
                    elapsedTime = 0;
                    currentPhase = NPC_DETERMINE_ACTION_PHASE;
                }
            }

            if(currentPhase == NPC_DETERMINE_ACTION_PHASE) {
//                System.out.println("NPC_DETERMINE_ACTION_PHASE");
                CHARA: for(Character monsterChara : cf.getCharacterSprites()) {
                    MovingSprite monsterSp = monsterChara.getSprite();
                    int msX = monsterSp.getX(), msY = monsterSp.getY();
                        
                    if(msX == monsterSp.getTargetX() && msY == monsterSp.getTargetY()) {
                        if(((Monster)monsterChara).hasSeenPc()) {
                            System.out.println(monsterChara.getName() + "@(" + msX + "," + msY + ") "
                                    + "has seen PC @(" + pcSprite.getTargetX() + "," + pcSprite.getTargetY() + ")");
                            // @TODO
                            // Create algorithm to search for the shortest path toward PC
                            // if pc is directly beside the monster, attack PC
                            // if not, go near PC with the shortest path
                            double directionTheta = Math.atan2(pcSprite.getTargetY() - msY, 
                                    pcSprite.getTargetX() - msX);
                            System.out.println("direction Theta:" + directionTheta);
                            if(moveRandomly(monsterSp, monsterChara)) continue CHARA;
                            
                        } else {
                            if(moveRandomly(monsterSp, monsterChara)) continue CHARA;
                        }
                    }
                }
                currentPhase = NPC_SPECIAL_ACTION_PHASE;
            }
        }
        updateFPS();
    }

    private boolean moveRandomly(MovingSprite monsterSp, Character monsterChara) {
        int msX;
        int msY;
        List<Integer> thisDirection = new ArrayList<>();
        thisDirection.addAll(MazeConst.getDIRECTIONS());
        Collections.shuffle(thisDirection);
        int way = 0;
        for (Integer direct : thisDirection) {
            msX = monsterSp.getX();
            msY = monsterSp.getY();
            // check if direction is passable
            if ((cf.getGrid()[msY][msX] & direct) != 0) {
                msX += generator.getDX().get(direct);
                msY += generator.getDY().get(direct);
                // checking if the next target had the same coordinate as previous coordinate
                if (monsterSp.getPreviousX() == msX && monsterSp.getPreviousY() == msY) {
                    if((cf.getVisitedGrid()[msY][msX] & MazeConst.UNPASSABLE) == 0) {
                        way++;
                    }
                // checking if the next target is passable
                } else if ((cf.getVisitedGrid()[msY][msX] & MazeConst.UNPASSABLE) == 0) {
                    // make the current coordinate passable for other character
                    cf.getVisitedGrid()[monsterSp.getY()][monsterSp.getX()] ^= MazeConst.UNPASSABLE;
                    // move the monster to another coordinate.
                    monsterSp.moveSprite(msX, msY);
                    // check the direction monster looked. If pc is there, change hasSeen as true
                    checkDirectionHasPc((Monster) monsterChara, msX, msY, direct);
                    monsterSp.turnSprite(direct);
                    // make the target coordinate as unpassable for other character.
                    cf.getVisitedGrid()[msY][msX] ^= MazeConst.UNPASSABLE;
                    // continue to loop the monster chara
                    return true;
                } else {
                    if(msX == pcSprite.getTargetX() && msY == pcSprite.getTargetY()) {
                        ((Monster) monsterChara).setPcSeen(true);
                        attackingMonster.add(new AttackEvent(monsterChara, pc));
                        return true;
                    }
                }
            }
        }
        // if there's only one way that possible and previously it was not choosen
        // because it was the previous way
        // (because if it was possible and it wasn't the previous coordinate,
        // this expression would never been called in the first place)
        //                        System.out.println("way = " + way);
        if(way == 1) {
            msX = monsterSp.getPreviousX();
            msY = monsterSp.getPreviousY();
            
            cf.getVisitedGrid()[monsterSp.getY()][monsterSp.getX()] ^= MazeConst.UNPASSABLE;
            monsterSp.moveSprite(msX, msY);
            cf.getVisitedGrid()[msY][msX] ^= MazeConst.UNPASSABLE;
            // else means that there's no other direction that possible to move,
            // so this monster should wait for other monster to move first
        }
        return false;
    }
    
    private boolean checkDirectionHasPc(Monster mon, int x, int y, int direction) {
//        if(mon.hasSeenPc()) return;
        
        int nextX = x, nextY = y;
        while((cf.getGrid()[nextY][nextX] & direction) != 0) {
            nextX += generator.getDX().get(direction);
            nextY += generator.getDY().get(direction);
//            System.out.println(mon.getName() + " toward " + direction + " (" + nextX + ", " + nextY + ")");
            if(nextX < 0 || nextX >= mazeWidth || nextY < 0 || nextY >= mazeHeight) {
                return false;
            }
            if ((pcSprite.getX() == nextX && pcSprite.getY() == nextY) || (pcSprite.getTargetX() == nextX && pcSprite.getTargetY() == nextY)) {
                NLog.log(KTag, "checkDirection", mon.getName() + " toward " + MazeConst.toString(direction) + " has seen PC@(" + nextX + "," + nextY + ") from (" + x + "," + y + ")");
                mon.setPcSeen(true);
                return true;
            } 
        }
        return false;
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
    
    private Character pc;
    private MovingSprite pcSprite;
    private List<Spell> castedSpell;
    private List<AttackEvent> attackingMonster;
    private List<AttackEvent> attackingCharacter;
    
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
            
            pcFaceLeft = new Tile(64, 32, 32, 32, test.getWidth(), test.getHeight(), tileId); // The character to be moved facing left
            pcFaceRight = new Tile(96, 32, 32, 32, test.getWidth(), test.getHeight(), tileId); // The character to be moved facing right;
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
            
            pcSprite = new MovingSprite(pcFaceLeft, 0, 0);
            pcSprite.setFaceWest(pcFaceLeft);
            pcSprite.setFaceEast(pcFaceRight);
            pcSprite.setMovementAnimationTime(movementAnimationTime);
            pcSprite.setAutomaticTurnEnabled(false);
            pcSprite.turnSprite(MazeConst.E);
            pc = new Character("Gandalph", 1, 50f, 10f, 1f, 1f, 1f, 0, pcSprite);
            pc.setPotentials(4, 3, 0.5f, 0.5f, 2, 1);
            pc.setCurrLevel(1);
            
            castedSpell = new ArrayList<>();
            attackingMonster = new ArrayList<>();
            attackingCharacter = new ArrayList<>();
            
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
//                if((cf.getVisitedGrid()[y][x] & MazeConst.SEEN) != 0) {
                    drawTile(tiles.get(cf.getGrid()[y][x]), x * 32, y * 32, 0, 32, 32, rotations.get(cf.getGrid()[y][x]));
                
                    if((cf.getEventGrid()[y][x] & MazeConst.TREASURE) != 0) {
                        drawTile(treasure, x * 32, y * 32, 0, 32, 32);
                    } else if((cf.getEventGrid()[y][x] & MazeConst.TREASURE_OPENED) != 0) {
                        drawTile(treasureOpened, x * 32, y * 32, 0, 32, 32);
                    }
//                }
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
            MovingSprite monsterSprite = monsterChara.getSprite();
//                    if((monsterSprite.isSpriteInTile(monsterSprite.getX(), monsterSprite.getY()) &&
//                            (cf.getVisitedGrid()[monsterSprite.getY()][monsterSprite.getX()] & MazeConst.SEEN) == MazeConst.SEEN)
//                            ||
//                            (monsterSprite.isSpriteInTile(monsterSprite.getTargetX(), monsterSprite.getTargetY()) &&
//                            (cf.getVisitedGrid()[monsterSprite.getTargetY()][monsterSprite.getTargetX()] & MazeConst.SEEN) == MazeConst.SEEN)) {
                drawTile(monsterSprite.getFacingDirectionTile(-1), 
                        monsterSprite.getCenteredMovementX(), monsterSprite.getCenteredMovementY(), 0, monsterSprite.getSpriteWidth(), monsterSprite.getSpriteHeight());
//                    }

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
    
    public static void main(String[] args) throws LWJGLException {
        MazeDrawer drawer = new MazeDrawer();
        drawer.start();
    }

    private void combatEvent(Character attacker, Character defender, Spell spell) {
        float potentialDamage = attacker.getCurrIntelligence() + spell.getAttackPower();
        float potentialResist = defender.getCurrIntelligence() / 2;
        float damage = potentialDamage - potentialResist;
        if(damage <= 1f) damage = 1f;
        defender.setCurrHealth(defender.getCurrHealth() - damage);
        NLog.log(KTag, "combatEvent", attacker.getName() + " use " + spell.getName() + " dealt " + damage + " damage. HP:" + defender.getCurrHealth() + "/" + defender.getCurrMaxHealth());
    }

    private void combatEvent(Character attacker, Character defender) {
        float potentialDamage = attacker.getCurrStrength();
        float potentialResist = defender.getCurrStrength()/ 2;
        float damage = potentialDamage - potentialResist;
        if(damage <= 1f) damage = 1f;
        defender.setCurrHealth(defender.getCurrHealth() - damage);
        NLog.log(KTag, "combatEvent", attacker.getName() + " dealt " + damage + " damage. HP:" + defender.getCurrHealth() + "/" + defender.getCurrMaxHealth());
    }

    private void combatWinEvent(Character attacker, Character defender) {
        // calculate exp
        NLog.log(KTag, "combatWinEvent", attacker.getName() + " win the battle ");
        attacker.setCurrExp(attacker.getCurrExp() + defender.getCurrExpValue());
        NLog.log(KTag, "combatWinEvent", "Receive " + defender.getCurrExpValue() + " exp. Curr: " + attacker.getCurrExp() + "/" + attacker.getCurrNextLevelExp());
        
        while(attacker.getCurrExp() >= attacker.getCurrNextLevelExp()) {
            levelUp(attacker);
        }
        
        // get loot
    }
    
    public void levelUp(Character character) {
        character.setCurrLevel(character.getCurrLevel() + 1);
        float health = character.getCurrMaxHealth() + character.getAddHealth();
        float mana = character.getCurrMaxMana() + character.getAddMana();
        float strength = character.getCurrStrength() + character.getAddStrength();
        float agility = character.getCurrAgility() + character.getAddAgility();
        float intelligence = character.getCurrIntelligence() + character.getAddIntelligence();
        int currExpValue = character.getCurrExpValue() + character.getAddExpValue();
        int nextLevelExp = Math.round((character.getCurrAgility() + 
                character.getCurrIntelligence() + character.getCurrStrength() + 
                character.getCurrExpValue()) * character.getCurrLevel());
        
        NLog.log(KTag, "levelUp", 
                "###################################");
        NLog.log(KTag, "levelUp", 
                "Level Up to " + character.getCurrLevel() + "!!!");
        NLog.log(KTag, "levelUp", 
                "Health: " + character.getCurrMaxHealth() + " -> " + health + " \n" +
                "Mana:   " + character.getCurrMaxMana() + " -> " + mana + " \n" +
                "Str:    " + character.getCurrStrength() + " -> " + strength + " \n" +
                "Agi:    " + character.getCurrAgility() + " -> " + agility + " \n" +
                "Int:    " + character.getCurrIntelligence()+ " -> " + intelligence + " \n" +
                "Next:   " + character.getCurrExp() + "/" + nextLevelExp + "\n" +
                "Health and Mana are fully restored.");
        NLog.log(KTag, "levelUp", 
                "###################################");
        character.setCurrMaxHealth(health);
        character.setCurrHealth(health);
        character.setCurrMaxMana(mana);
        character.setCurrMana(mana);
        character.setCurrStrength(strength);
        character.setCurrAgility(agility);
        character.setCurrIntelligence(intelligence);
        character.setCurrExpValue(currExpValue);
        character.setCurrNextLevelExp(nextLevelExp);
    }
}
