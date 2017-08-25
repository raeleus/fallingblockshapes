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
package com.ray3k.fallingblockshapes.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.SkeletonData;
import com.ray3k.fallingblockshapes.Core;
import com.ray3k.fallingblockshapes.EntityManager;
import com.ray3k.fallingblockshapes.InputManager;
import com.ray3k.fallingblockshapes.SaveShape;
import com.ray3k.fallingblockshapes.SpineDrawable;
import com.ray3k.fallingblockshapes.State;
import com.ray3k.fallingblockshapes.entities.TetrominoEntity;

public class GameState extends State {
    private int score;
    private static int highscore = 0;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    private Table table;
    private Label scoreLabel;
    private Label levelLabel;
    private Label linesLabel;
    private EntityManager entityManager;
    private SpineDrawable previewDrawable;
    private Table gameTable;
    private Array<SaveShape> shapes;
    private int lineCount;
    private int levelCount;
    private SaveShape nextShape;
    private Table previewTable;
    
    public static enum Team {
        PLAYER, ENEMY;
    }
    
    public GameState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        score = 0;
        lineCount = 8;
        levelCount = 1;
        
        inputManager = new InputManager(); 
        
        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);
        uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiViewport.apply();
        
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        
        gameCamera = new OrthographicCamera();
        gameViewport = new ScreenViewport(gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameViewport.apply();
        
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/ui/Falling Block Shapes.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        entityManager = new EntityManager();
        
        previewDrawable = new SpineDrawable(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/tetromino.json", SkeletonData.class), getCore().getSkeletonRenderer());
        
        shapes = new Array<SaveShape>();
        FileHandle parent = new FileHandle(Core.DATA_PATH + "/shapes/");
        Json json = new Json();
        for (FileHandle file : parent.list()) {
            shapes.add(json.fromJson(SaveShape.class, file));
        }
        
        nextShape = getShapes().random();
        createStageElements();
        
        Vector2 coord = gameTable.localToStageCoordinates(new Vector2(8.0f, 8.0f));
        
        gameCamera.position.set(Gdx.graphics.getWidth() / 2.0f - coord.x, Gdx.graphics.getHeight() / 2.0f - coord.y, 0);
        TetrominoEntity tet = new TetrominoEntity(this, shapes.random());
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        root.defaults().pad(25.0f);
        Table table = new Table();
        table.defaults().padLeft(25.0f).padRight(25.0f);
        root.add(table);
        
        Label label = new Label("Level", skin);
        table.add(label);
        
        table.row();
        levelLabel = new Label("1", skin);
        table.add(levelLabel);
        
        table.row();
        label = new Label("Lines", skin);
        table.add(label).padTop(25.0f);
        
        table.row();
        linesLabel = new Label("8", skin);
        table.add(linesLabel);
        
        table.row();
        label = new Label("Score", skin);
        table.add(label).padTop(25.0f);
        
        table.row();
        scoreLabel = new Label("0", skin);
        table.add(scoreLabel);
        
        table = new Table();
        root.add(table);
        
        Stack stack = new Stack();
        table.add(stack).width(266).height(616);
        
        Container container = new Container();
        Image image = new Image(skin.getTiledDrawable("play-grid"));
        container.setActor(image);
        container.pad(8.0f);
        container.fill();
        stack.add(container);
        
        gameTable = new Table(skin);
        gameTable.setBackground("window");
        stack.add(gameTable);
        
        table = new Table();
        root.add(table);
        
        label = new Label("Next", skin);
        table.add(label).padBottom(20.0f);
        
        table.row();
        previewTable = new Table(skin);
        previewTable.setBackground("window");
        table.add(previewTable).width(116.0f).height(116.0f);
        
        generatePreview();
        
        root.validate();
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.draw();
        
        gameCamera.update();
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        entityManager.draw(spriteBatch, delta);
        spriteBatch.end();
    }

    @Override
    public void act(float delta) {
        entityManager.act(delta);
        
        stage.act(delta);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        gameCamera.position.set(width / 2, height / 2.0f, 0.0f);
        
        uiViewport.update(width, height);
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        stage.getViewport().update(width, height, true);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        scoreLabel.setText(Integer.toString(score));
        if (score > highscore) {
            highscore = score;
        }
    }
    
    public void addScore(int score) {
        this.score += score;
        scoreLabel.setText(Integer.toString(this.score));
        if (this.score > highscore) {
            highscore = this.score;
        }
    }
    
    public void playDownSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/down.wav", Sound.class).play(.01f);
    }
    
    public void playLandSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/land.wav", Sound.class).play(.5f);
    }
    
    public void playLineSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/line.wav", Sound.class).play(.5f);
    }
    
    public void playRotateSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/rotate.wav", Sound.class).play(.5f);
    }
    
    public void playBonusSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/bonus.wav", Sound.class).play(.5f);
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(OrthographicCamera gameCamera) {
        this.gameCamera = gameCamera;
    }

    public Skin getSkin() {
        return skin;
    }

    public Stage getStage() {
        return stage;
    }

    public Array<SaveShape> getShapes() {
        return shapes;
    }

    public void subtractLines(int lines) {
        lineCount -= lines;
        if (lineCount <= 0) {
            lineCount = 8;
            levelCount++;
        }
        
        linesLabel.setText(Integer.toString(lineCount));
        levelLabel.setText(Integer.toString(levelCount));
    }

    public int getLevelCount() {
        return levelCount;
    }

    public SaveShape getNextShape() {
        return nextShape;
    }
    
    public void generateNextShape() {
        nextShape = shapes.random();
        generatePreview();
    }
    
    private void generatePreview() {
        if (previewTable != null) {
            previewTable.clearChildren();
            
            System.out.println(nextShape);
            System.out.println(nextShape.color);
            System.out.println(previewDrawable);
            System.out.println(previewDrawable.getSkeleton().findSlot("tetromino").getColor());
            previewDrawable.getSkeleton().findSlot("tetromino").getColor().set(nextShape.color);
            
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 4; x++) {
                    if (nextShape.grid[x][y]) {
                        Image image = new Image(previewDrawable);
                        previewTable.add(image).grow();
                    } else {
                        previewTable.add().grow();
                    }
                }
                if (y < 3) {
                    previewTable.row();
                }
            }
        }
    }
}