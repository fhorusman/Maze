/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maze.sprite;

/**
 *
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class Tile {
    // The textureId that binded to openGL
    private int textureId;
    // Tile positions relative to the image size, by percentage of 0f to 1f
    // u = x position on the image, while v = y position on the image.
    private float u, v, u2, v2;
    // Tile position on the image by pixels
    private int x, y;
    // Image source's total width and height;
    private int srcWidth, srcHeight;
    // The size of tile on the image by pixels
    private int width, height;
    // The size of the tile on the image by percentages
    private float rangeHeight, rangeWidth;
    // Tile's image rotation, if needed to be drawn rotated
    private float rotation;

    public Tile(int x, int y, int tileWidth, int tileHeight, int imageSrcWidth, int imageSrcHeight, int textureId) {
        this.x = x;
        this.y = y;
        this.width = tileWidth;
        this.height = tileHeight;
        this.srcWidth = imageSrcWidth;
        this.srcHeight = imageSrcHeight;
        this.rangeWidth = (float) tileWidth / imageSrcWidth;
        this.rangeHeight = (float) tileHeight / imageSrcHeight;
        this.u = (float) x / imageSrcWidth;
        this.v = (float) y / imageSrcHeight;
        this.u2 = (float) u + rangeWidth;
        this.v2 = (float) v + rangeHeight;
        this.textureId = textureId;
        System.out.println("u= " + u + " v:" + v + " u2:" + u2 + " v2:" + v2 + " rangeW:" + rangeWidth +  " rangeH: " + rangeHeight);
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public int getTextureId() {
        return textureId;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public float getU() {
        return u;
    }

    public void setU(float u) {
        this.u = u;
    }

    public float getV() {
        return v;
    }

    public void setV(float v) {
        this.v = v;
    }

    public float getU2() {
        return u2;
    }

    public void setU2(float u2) {
        this.u2 = u2;
    }

    public float getV2() {
        return v2;
    }

    public void setV2(float v2) {
        this.v2 = v2;
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getRangeHeight() {
        return rangeHeight;
    }

    public void setRangeHeight(float rangeHeight) {
        this.rangeHeight = rangeHeight;
    }

    public float getRangeWidth() {
        return rangeWidth;
    }

    public void setRangeWidth(float rangeWidth) {
        this.rangeWidth = rangeWidth;
    }
    
    @Override
    public Tile clone() {
        Tile tile = new Tile(x,y,width, height, srcWidth, srcHeight, textureId);
        tile.setRotation(rotation);
        return tile;
    }
}
