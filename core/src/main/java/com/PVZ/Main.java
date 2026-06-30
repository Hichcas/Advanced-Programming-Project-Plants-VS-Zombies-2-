package com.PVZ;

import com.PVZ.controller.AppController;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.CommandParser;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() {
//          // here is what we used to have in previous Main.java file
        CommandParser.start();
        AppStatus.currentMenuType = MenuType.REGISTER;
//        AppController.render();
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();
//        System.out.println("the Start is triggered");
        AppController.render();
    }

    @Override
    public void dispose() {
        CommandParser.end();
        batch.dispose();
        image.dispose();
    }
}
