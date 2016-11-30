/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import static org.lwjgl.opengl.GL11.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

/**
 *
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class Test1 {
    int WIDTH = 800;
    int HEIGHT = 600;
    
    float x = 100, y = 100;
    float rotation = 0;
    
    long lastFrame;
    
    int fps;
    long lastFPS;
    
    boolean vsync;
    
    public void start() {
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
        
        getDelta(); 
        lastFPS = getTime();
        
        while(!Display.isCloseRequested()) {
            int delta = getDelta();
            
            update(delta);
            //Render OpenGL
            renderGL();
            
            pollInput();
            Display.update();
            
            if(Display.isActive()) {
                Display.sync(60);
            }
        }
        
        Display.destroy();
    }
    
    public void update(int delta) {
        // rotate quad
        rotation += 0.15f * delta;
        if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) x -= 0.35f * delta;
        if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) x += 0.35f * delta;
        
        if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) y -= 0.35f * delta;
        if(Keyboard.isKeyDown(Keyboard.KEY_UP)) y += 0.35f * delta;
        
        while(Keyboard.next()) {
            if(Keyboard.getEventKeyState()) {
                if(Keyboard.getEventKey() == Keyboard.KEY_F) {
                    setDisplayMode(WIDTH, HEIGHT, !Display.isFullscreen());
                } else if(Keyboard.getEventKey() == Keyboard.KEY_V) {
                    vsync = !vsync;
                    Display.setVSyncEnabled(vsync);
                }
            }
        }
        
        // keep quad on the screen
        if(x < 0) x = 0;
        if(x > WIDTH) x = WIDTH;
        if(y < 0) y = 0;
        if(y > HEIGHT) y = HEIGHT;
        
        updateFPS();
    }
    
    public void updateFPS() {
        if(getTime() - lastFPS > 1000) {
            Display.setTitle("FPS: " + fps);
            fps = 0; //reset the FPS counter
            lastFPS += 1000; //add one second
        }
        fps++;
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
    
    public void pollInput() {
        if(Mouse.isButtonDown(0)) {
            int x = Mouse.getX();
            int y = Mouse.getY();
            System.out.println("Mouse Down @X = " + x + " Y = " + y);
        }
        
        if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            System.out.println("Space Key is Down");
        }
        
        while(Keyboard.next()) {
            if(Keyboard.getEventKeyState()) {
                if(Keyboard.getEventKey() == Keyboard.KEY_A) {
                    System.out.println("A Key Pressed");
                }
                if(Keyboard.getEventKey() == Keyboard.KEY_S) {
                    System.out.println("S Key Pressed");
                }
                if(Keyboard.getEventKey() == Keyboard.KEY_D) {
                    System.out.println("D Key Pressed");
                }
                if(Keyboard.getEventKey() == Keyboard.KEY_W) {
                    System.out.println("W Key Pressed");
                }
            } else {
                if(Keyboard.getEventKey() == Keyboard.KEY_A) {
                    System.out.println("A Key Released");
                }
                if(Keyboard.getEventKey() == Keyboard.KEY_S) {
                    System.out.println("S Key Released");
                }
                if(Keyboard.getEventKey() == Keyboard.KEY_D) {
                    System.out.println("D Key Released");
                }
                if(Keyboard.getEventKey() == Keyboard.KEY_W) {
                    System.out.println("W Key Released");
                }
            }
        }
    }
    
    public void initGL() {
        int maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
        System.out.println("maxSize = " + maxSize);
        boolean npotSupported = GLContext.getCapabilities().GL_ARB_texture_non_power_of_two;
        System.out.println("npotSupported = " + npotSupported);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, WIDTH, 0, HEIGHT, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0f, 0f, 0f, 0f);
    }
    
    public void renderGL() {
            //Clear the screen and depth buffer
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            //Set the color of the quad(R,G,B,A)
            glColor3f(0.5f, 0.5f, 1.0f);
            
            //Draw quad
            glPushMatrix();
                glTranslatef(x, y, 0);
                glRotatef(rotation, 0f, 0f, 1f);
                glTranslatef(-x, -y, 0);
                
                glBegin(GL_QUADS);
                    glVertex2f(x - 50, y - 50);
                    glVertex2f(x + 50, y - 50);
                    glVertex2f(x + 50, y + 50);
                    glVertex2f(x - 50, y + 50);
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

                for (int i = 0; i < modes.length; i++) {
                    DisplayMode current = modes[i];

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
    
    public static void main(String[] args) {
        Test1 test = new Test1();
        test.start();
    }
}
