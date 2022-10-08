package com.mygdx.game.Screens;

import com.badlogic.gdx.Screen;
import com.mygdx.game.MainGame;

public class GameScreenManager {
    private final MainGame mainGame;
    public static Screen PLAY_SCREEN;
    public GameScreenManager(MainGame mainGame){
        this.mainGame = mainGame;
        PLAY_SCREEN = new PLAY_SCREEN();
    }

    public void setGameScreen(Screen screen){
        mainGame.setScreen(screen);
    }
}
