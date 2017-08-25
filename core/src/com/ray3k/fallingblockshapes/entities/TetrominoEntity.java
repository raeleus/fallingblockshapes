/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ray3k.fallingblockshapes.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.ray3k.fallingblockshapes.Entity;
import com.ray3k.fallingblockshapes.SaveShape;
import com.ray3k.fallingblockshapes.states.GameState;
import java.util.Comparator;

public class TetrominoEntity extends Entity {
    private final GameState gameState;
    private final BlockEntity[][] grid = new BlockEntity[4][4];
    private final static float INPUT_DELAY = .1f;
    private final static float SLIDE_DELAY = .5f;
    private float inputCounter;
    private float rotateCounter;
    public static float fallDelay;
    private float fallCounter;
    
    public TetrominoEntity(GameState gameState, SaveShape saveShape) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
        
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (saveShape.grid[x][y]) {
                    grid[x][y] = new BlockEntity(gameState, this);
                    grid[x][y].getSkeleton().findSlot("tetromino").getColor().set(saveShape.color);
                }
            }
        }
        
        setPosition(3 * 25, 24 * 25 - getTop());
        
        updateBlockPositions();
        if (checkForCollision(0, 0)) {
            dispose();
            new GameOverTimerEntity(gameState, 2.0f);
        }
        
        inputCounter = -1;
        rotateCounter = -1;
        fallDelay = .60f / gameState.getLevelCount();
        fallCounter = fallDelay;
    }
    
    @Override
    public void create() {
        
    }

    @Override
    public void act(float delta) {
        inputCounter -= delta;
        rotateCounter -= delta;
        fallCounter -= delta;
        
        if (rotateCounter < 0) {
            rotateCounter = -1;
            
            if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W)) {
                rotateBlocks();
                rotateCounter = INPUT_DELAY;
            } else if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S)) {
                while (!checkForCollisionBelow()) {
                    addY(-25);
                    updateBlockPositions();
                }
                gameState.playLandSound();
                fallCounter = SLIDE_DELAY;
                rotateCounter = INPUT_DELAY;
            } 
        }
        
        if (inputCounter < 0) {
            inputCounter = -1;
            
            
            if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
                if (!checkForCollisionLeft()) {
                    addX(-25);
                }
                inputCounter = INPUT_DELAY;
            } else if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
                if (!checkForCollisionRight() ) {
                    addX(25);
                }
                inputCounter = INPUT_DELAY;
            }
        }
        
        updateBlockPositions();
        
        if (getX() + getLeft() < 0.0f) {
            setX(-getLeft());
            updateBlockPositions();
        } else if (getX() + getRight() > 250.0f) {
            setX(250.0f - getRight());
            updateBlockPositions();
        }
        
        if (fallCounter < 0) {
            fallCounter = fallDelay;
            
            if (checkForCollisionBelow()) {
                dispose();
                clearLines();
                new TetrominoEntity(gameState, gameState.getNextShape());
                gameState.generateNextShape();
            } else {
                addY(-25);
                gameState.playDownSound();
                updateBlockPositions();
                
                if (checkForCollisionBelow()) {
                    fallCounter = SLIDE_DELAY;
                    gameState.playLandSound();
                }
            }
        }
    }
    
    private void updateBlockPositions() {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                if (grid[x][y] != null) {
                    grid[x][y].setPosition(getX() + x * 25.0f, getY() + (3 - y) * 25.0f);
                }
            }
        }
    }

    @Override
    public void act_end(float delta) {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void destroy() {
        
    }

    @Override
    public void collision(Entity other) {
    }
    
    private float getLeft() {
        float returnValue = -10;
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                if (grid[x][y] != null) {
                    returnValue = x * 25;
                    break;
                }
            }
            if (returnValue > -1) {
                break;
            }
        }
        return returnValue;
    }
    
    private float getRight() {
        float returnValue = -10;
        for (int x = 3; x >= 0; x--) {
            for (int y = 0; y < 4; y++) {
                if (grid[x][y] != null) {
                    returnValue = (x + 1) * 25;
                    break;
                }
            }
            if (returnValue > -1) {
                break;
            }
        }
        return returnValue;
    }
    
    private float getBottom() {
        float returnValue = -10;
        for (int y = 3; y >= 0; y--) {
            for (int x = 0; x < 4; x++) {
                if (grid[x][y] != null) {
                    returnValue = (3 - y) * 25;
                    break;
                }
            }
            if (returnValue > -1) {
                break;
            }
        }
        return returnValue;
    }
    
    private float getTop() {
        float returnValue = -10;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (grid[x][y] != null) {
                    returnValue = (4 - y) * 25;
                    break;
                }
            }
            if (returnValue > -1) {
                break;
            }
        }
        return returnValue;
    }
    
    private void rotateBlocks() {
        float previousBottom = getBottom();
        BlockEntity[][] oldGrid = new BlockEntity[4][4];
        
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                oldGrid[x][y] = grid[x][y];
            }
        }
        
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                grid[x][y] = oldGrid[y][3-x];
            }
        }
        
        addY(previousBottom - getBottom());
        updateBlockPositions();
        
        if (checkForCollision(0, 0)) {
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    grid[x][y] = oldGrid[x][y];
                }
            }
            
            addY(-(previousBottom - getBottom()));
            updateBlockPositions();
        }
    }
    
    private boolean checkForCollisionBelow() {
        return checkForCollision(0, -25);
    }
    
    private boolean checkForCollisionLeft() {
        return checkForCollision(-25, 0);
    }
    
    private boolean checkForCollisionRight() {
        return checkForCollision(25, 0);
    }
    
    private boolean checkForCollision(int displaceX, int displaceY) {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                BlockEntity collider = grid[x][y];
                if (collider != null) {
                    if (MathUtils.round(collider.getY() + displaceY) < 0) {
                        return true;
                    } else if (MathUtils.round(collider.getX() + displaceX) < 0) {
                        return true;
                    } else if (MathUtils.round(collider.getX() + displaceX) > 250) {
                        return true;
                    }

                    Rectangle.tmp2.set(collider.getX() + displaceX, collider.getY() + displaceY, 25, 25);
                    for (Entity entity : gameState.getEntityManager().getEntities()) {
                        if (entity instanceof BlockEntity) {
                            BlockEntity blockEntity = (BlockEntity) entity;
                            if (!blockEntity.getParent().equals(this)) {
                                Rectangle.tmp.set(MathUtils.round(blockEntity.getX()), MathUtils.round(blockEntity.getY()), 25, 25);

                                if (Rectangle.tmp.overlaps(Rectangle.tmp2)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private void clearLines() {
        Array<BlockEntity> blocks = new Array<BlockEntity>();
        for (Entity entity : gameState.getEntityManager().getEntities()) {
            if (entity instanceof BlockEntity) {
                blocks.add((BlockEntity) entity);
            }
        }
        
        blocks.sort(new Comparator<BlockEntity>() {
            @Override
            public int compare(BlockEntity o1, BlockEntity o2) {
                return MathUtils.round(o1.getY() - o2.getY());
            }
        });
        
        Array<BlockEntity> level = new Array<BlockEntity>();
        int lines = 0;
        for (BlockEntity block : blocks) {
            block.addY(-25*lines);
            if (level.size == 0 || MathUtils.isEqual(level.first().getY(), block.getY())) {
                level.add(block);
                if (level.size == 10) {
                    for (BlockEntity clearBlock : level) {
                        clearBlock.dispose();
                    }
                    level.clear();
                    lines++;
                }
            } else {
                level.clear();
                level.add(block);
            }
        }
        
        if (lines == 1) {
            gameState.addScore(10 * gameState.getLevelCount());
            gameState.playLineSound();
        } else if (lines == 2) {
            gameState.addScore(25 * gameState.getLevelCount());
            gameState.playLineSound();
        } else if (lines == 3) {
            gameState.addScore(50 * gameState.getLevelCount());
            gameState.playLineSound();
        } else if (lines >= 4) {
            gameState.addScore(100 * gameState.getLevelCount());
            gameState.playBonusSound();
        }
        
        gameState.subtractLines(lines);
    }
}