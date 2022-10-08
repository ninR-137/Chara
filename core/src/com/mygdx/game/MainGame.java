package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.mygdx.game.Screens.GameScreenManager;


public class MainGame extends Game {
	public static GameScreenManager gameScreenManager;
	@Override
	public void create () {
		gameScreenManager = new GameScreenManager(this);
		setScreen(GameScreenManager.PLAY_SCREEN);
	}


	//----------------------------------------------//

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void render () {
		super.render();

	}

	@Override
	public void dispose () {

	}
}
