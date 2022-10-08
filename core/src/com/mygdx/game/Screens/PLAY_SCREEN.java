package com.mygdx.game.Screens;

import static com.mygdx.game.GlobalVariables.extendB2DViewPort;
import static com.mygdx.game.GlobalVariables.extendMainViewPort;

import com.badlogic.gdx.Screen;
import com.mygdx.game.Handlers.WorldManager;

public class PLAY_SCREEN implements Screen {
    private WorldManager worldManager;
    @Override
    public void show() {
        worldManager = new WorldManager();
    }

    @Override
    public void render(float delta) {
        worldManager.render();
    }

    @Override
    public void resize(int width, int height) {
        extendMainViewPort.update(width,height);
        extendB2DViewPort.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        worldManager.dispose();
    }
}
