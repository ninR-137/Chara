package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.Entities.Player;

import static com.mygdx.game.GlobalVariables.*;

public class MainGame extends ApplicationAdapter {

	private B2dModel b2dModel;
	private Texture img;
	private Player player;
	private KeyboardController keyboardController;

	@Override
	public void create () {
		img = new Texture("assets/badlogic.jpg");

		player = new Player();
		b2dModel = new B2dModel(player);
		keyboardController = new KeyboardController();

		Gdx.input.setInputProcessor(keyboardController);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		extendMainViewPort.update(width,height);
	}

	@Override
	public void render () {
		update(Gdx.graphics.getDeltaTime());

		Gdx.gl.glClearColor(0,0,0,0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		b2dDebugRenderer.render(b2dModel.world, mainCamera.combined);
		mainSpriteBatch.setProjectionMatrix(mainCamera.combined);

		mainSpriteBatch.begin();
		//b2dModel.renderMap(mainSpriteBatch);
		player.render(mainSpriteBatch);
		mainSpriteBatch.end();


		sr.setProjectionMatrix(mainCamera.combined);
		sr.begin(ShapeRenderer.ShapeType.Line);
		sr.line(rightLegP1, rightLegP2);
		sr.line(leftLegP1, leftLegP2);
		sr.end();

	}

	public void update(float dt){
		updateCameraPosition();
		extendMainViewPort.apply();
		b2dModel.logicStep(dt);
		player.positionX = b2dModel.playerBody.getPosition().x;
		player.positionY = b2dModel.playerBody.getPosition().y;
		playerRaytraceSensorUpdates();
		player.update();
	}


	private void playerRaytraceSensorUpdates(){
		rightSideAngleInDegrees = 0;
		leftSideAngleInDegrees = 0;
		leftSideValue --;
		rightSideValue --;
		b2dModel.setRayCastPoints();
	}


	/**
	 * Under Tests still
	 * Convey motion through camera movement
	 */

	public void updateCameraPosition(){
		//a+(b-a) * lerp
		//a = camera position
		//b = target

		//TODO: CREATE LINEAR INTERPOLATION FOR adjustmentX
		Vector3 position = mainCamera.position;
		float adjustmentX;
		//float lerp = Math.abs(player.currentVelocity.len()) > 0 ? 0.05f : 0.015f;
		float lerp = 0.25f;

		if(Math.abs(player.currentVelocity.x) > 0) {
			adjustmentX = player.playerState.isFacingRight ? 10 : -10;
		} else {
			adjustmentX = player.playerState.isFacingRight ? 8 : -8;
		}
		position.x = mainCamera.position.x + (player.positionX - (mainCamera.position.x)) * lerp;
		position.y = mainCamera.position.y + (player.positionY - mainCamera.position.y) * lerp;
		mainCamera.position.set(position);
		mainCamera.update();
	}
	
	@Override
	public void dispose () {
		img.dispose();
		GlobalVariables.dispose();
		sr.dispose();
		player.dispose();
		b2dModel.dispose();
	}
}
