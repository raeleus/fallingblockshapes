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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.esotericsoftware.spine.SkeletonData;
import com.ray3k.fallingblockshapes.Core;
import com.ray3k.fallingblockshapes.SaveShape;
import com.ray3k.fallingblockshapes.SpineDrawable;
import com.ray3k.fallingblockshapes.State;

public class DesignState extends State {
    private Stage stage;
    private Skin skin;
    private Table root;
    private SaveShape saveImage;
    private Image[][] images;
    private SpineDrawable spineDrawable;

    public DesignState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        saveImage = new SaveShape();
        saveImage.grid = new boolean[4][4];
        saveImage.color = new Color(Color.RED);
        images = new Image[4][4];
        
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                saveImage.grid[x][y] = false;
            }
        }
        
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/ui/Falling Block Shapes.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        Gdx.input.setInputProcessor(stage);
        
        createMenu();
    }
    
    private void createMenu() {
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Label label = new Label("Create a Shape", skin, "title");
        root.add(label);
        
        root.row();
        Image image = new Image(skin.getTiledDrawable("editor-grid"));
        Table bg = new Table();
        bg.add(image).pad(8.0f).grow();
        
        Table table = new Table(skin);
        table.background("window");
        table.defaults().width(100.0f).height(100.0f);
        
        spineDrawable = new SpineDrawable(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/tetromino-big.json", SkeletonData.class), getCore().getSkeletonRenderer());
        spineDrawable.getAnimationState().setAnimation(0, "animation", true);
        spineDrawable.getSkeleton().findSlot("tetromino").getColor().set(Color.RED);
        
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                final Image imageF = new Image(spineDrawable);
                final int gridX = x;
                final int gridY = y;
                imageF.setVisible(false);
                Container container = new Container();
                container.setActor(imageF);
                container.setTouchable(Touchable.enabled);
                container.fill();
                images[gridX][gridY] = imageF;
                container.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float fx, float fy) {
                        saveImage.grid[gridX][gridY] = !saveImage.grid[gridX][gridY];
                        imageF.setVisible(saveImage.grid[gridX][gridY]);
                    }
                });
                
                table.add(container);
            }
            if (y < 3) {
                table.row();
            }
        }
        
        Stack stack = new Stack(bg, table);
        root.add(stack).expand();
        
        root.row();
        table = new Table();
        table.defaults().space(50.0f);
        root.add(table).padBottom(25.0f);
        
        TextButton textButton = new TextButton("Color", skin);
        table.add(textButton);
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                changeColor();
            }
        });
        
        textButton = new TextButton("Save", skin);
        table.add(textButton);
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                save();
            }
        });
        
        textButton = new TextButton("Load", skin);
        table.add(textButton);
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                load();
            }
        });
        
        textButton = new TextButton("Quit", skin);
        table.add(textButton);
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                quit();
            }
        });
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void act(float delta) {
        stage.act(delta);
    }

    @Override
    public void stop() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
    private void changeColor() {
        final TextButton textButton = new TextButton("OK", skin);
        final Dialog dialog = new Dialog("", skin);
        
        Label label = new Label("Choose a color.", skin);
        dialog.getContentTable().add(label);
        
        dialog.getContentTable().row();
        Table table = new Table();
        ScrollPane scrollPane = new ScrollPane(table, skin);
        scrollPane.setFadeScrollBars(false);
        dialog.getContentTable().add(scrollPane).grow();
        
        ButtonGroup buttonGroup = new ButtonGroup();
        TextButton button = new TextButton("blue", skin, "color-chooser");
        button.getLabel().setColor(Color.BLUE);
        buttonGroup.add(button);
        table.add(button);
        
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event,
                    Actor actor) {
                textButton.setUserObject(Color.BLUE);
            }
        });
        
        table.row();
        button = new TextButton("yellow", skin, "color-chooser");
        button.getLabel().setColor(Color.YELLOW);
        buttonGroup.add(button);
        table.add(button);
        
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event,
                    Actor actor) {
                textButton.setUserObject(Color.YELLOW);
            }
        });
        
        table.row();
        button = new TextButton("red", skin, "color-chooser");
        button.getLabel().setColor(Color.RED);
        buttonGroup.add(button);
        table.add(button);
        
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event,
                    Actor actor) {
                textButton.setUserObject(Color.RED);
            }
        });
        
        table.row();
        button = new TextButton("green", skin, "color-chooser");
        button.getLabel().setColor(Color.GREEN);
        buttonGroup.add(button);
        table.add(button);
        
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event,
                    Actor actor) {
                textButton.setUserObject(Color.GREEN);
            }
        });
        
        table.row();
        button = new TextButton("orange", skin, "color-chooser");
        button.getLabel().setColor(Color.ORANGE);
        buttonGroup.add(button);
        table.add(button);
        
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event,
                    Actor actor) {
                textButton.setUserObject(Color.ORANGE);
            }
        });
        
        table.row();
        button = new TextButton("pink", skin, "color-chooser");
        button.getLabel().setColor(Color.PINK);
        buttonGroup.add(button);
        table.add(button);
        
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event,
                    Actor actor) {
                textButton.setUserObject(Color.PINK);
            }
        });
        
        table.row();
        button = new TextButton("purple", skin, "color-chooser");
        button.getLabel().setColor(Color.PURPLE);
        buttonGroup.add(button);
        table.add(button);
        
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event,
                    Actor actor) {
                textButton.setUserObject(Color.PURPLE);
            }
        });
        
        textButton.setUserObject(Color.BLUE);
        
        dialog.getContentTable().row();
        dialog.getContentTable().add(textButton);
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                spineDrawable.getSkeleton().findSlot("tetromino").getColor().set((Color) textButton.getUserObject());
                saveImage.color = new Color((Color) textButton.getUserObject());
                dialog.hide();
            }
        });
        
        dialog.show(stage);
        dialog.setWidth(600.0f);
        dialog.setHeight(600.0f);
        dialog.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f, Align.center);
    }

    private void save() {
        final TextField textField = new TextField("", skin);
        
        final Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                saveShape(textField.getText());
            }
        };
        
        Label label = new Label("Enter a name for the shape.", skin);
        dialog.getContentTable().add(label);
        
        dialog.getContentTable().row();
        dialog.getContentTable().add(textField);
        
        dialog.getContentTable().row();
        TextButton textButton = new TextButton("OK", skin);
        dialog.getContentTable().add(textButton);
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                saveShape(textField.getText());
                dialog.hide();
            }
        });
        
        dialog.key(Keys.ENTER, null);
        
        dialog.show(stage);
        stage.setKeyboardFocus(textField);
    }
    
    private void load() {
        final TextButton textButton = new TextButton("OK", skin);
        final Dialog dialog = new Dialog("", skin);
        
        
        Label label = new Label("Select a shape.", skin);
        dialog.getContentTable().add(label);
        
        dialog.getContentTable().row();
        Table table = new Table();
        ScrollPane scrollPane = new ScrollPane(table, skin);
        scrollPane.setFadeScrollBars(false);
        dialog.getContentTable().add(scrollPane).grow();
        
        ButtonGroup buttonGroup = new ButtonGroup();
        for (final FileHandle file : Gdx.files.local(Core.DATA_PATH + "/shapes").list()) {
            ImageTextButton button = new ImageTextButton(file.nameWithoutExtension(), skin);
            buttonGroup.add(button);
            table.add(button);
            table.row();
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event,
                        Actor actor) {
                    textButton.setUserObject(file);
                }
            });
        }
        textButton.setUserObject(Gdx.files.local(Core.DATA_PATH + "/shapes").list()[0]);
        
        dialog.getContentTable().row();
        dialog.getContentTable().add(textButton);
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                loadShape((FileHandle)textButton.getUserObject());
                dialog.hide();
            }
        });
        
        dialog.show(stage);
        dialog.setWidth(600.0f);
        dialog.setHeight(600.0f);
        dialog.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f, Align.center);
    }
    
    private void quit() {
        getCore().getStateManager().loadState("menu");
    }
    
    private void saveShape(String name) {
        FileHandle parent = new FileHandle(Core.DATA_PATH + "/shapes");
        parent.mkdirs();
        
        FileHandle saveFile = parent.child(name + ".shape");
        
        Json json = new Json();
        saveFile.writeString(json.toJson(saveImage), false);
        getCore().getStateManager().loadState("menu");
    }
    
    private void loadShape(FileHandle fileHandle) {
        if (fileHandle != null) {
            Json json = new Json();
            saveImage = json.fromJson(SaveShape.class, fileHandle);
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 4; x++) {
                    images[x][y].setVisible(saveImage.grid[x][y]);
                }
            }
            
            spineDrawable.getSkeleton().findSlot("tetromino").getColor().set(saveImage.color);
        }
    }
}
