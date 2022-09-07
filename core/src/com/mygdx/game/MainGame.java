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
		player = new Player(0,0);
		b2dModel = new B2dModel(player);
		keyboardController = new KeyboardController(b2dModel);

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
		player.render(mainSpriteBatch);
		//mainSpriteBatch.draw(img, player.positionX - PlayerWidth/2, player.positionY - PlayerHeight, PlayerWidth, PlayerHeight * 1.5f);
		mainSpriteBatch.end();

		/*
		sr.setProjectionMatrix(mainCamera.combined);
		sr.begin(ShapeRenderer.ShapeType.Line);
		sr.line(p1,p2);
		sr.line(collision, normal);
		sr.end();
		*/

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
		rightLegAngleInDegrees = 0;
		leftLegAngleInDegrees = 0;
		b2dModel.setRayCastPoints();
		player.update();
	}


	/*
	private void calculateAngleFromNorm(){
		Vector2 v = new Vector2(p2.x - p1.x, p2.y - p1.y);
		Vector2 vnorm = v.nor();
		double angle = Math.acos(vnorm.dot(normal));
		System.out.println(Math.toDegrees(angle));
	}

	 */


	/**
	 * Under Tests still
	 * Convey motion through camera movement
	 */
	public void updateCameraPosition(){
		//a+(b-a) * lerp
		//a = camera position
		//b = target

		Vector3 position = mainCamera.position;
		float adjustmentX = 0;
		if(player.currentVelocity.x != 0)adjustmentX = player.playerState.isFacingRight ? 5 : -5;
		float lerp = Math.abs(player.currentVelocity.len()) > 0 ? 0.2f : 0.015f;

		position.x = mainCamera.position.x + (player.positionX - mainCamera.position.x) * lerp;
		position.y = mainCamera.position.y + (player.positionY - mainCamera.position.y) * lerp;
		mainCamera.position.set(position);
		mainCamera.update();
	}
	
	@Override
	public void dispose () {
		img.dispose();
		GlobalVariables.dispose();
		sr.dispose();
	}
}
