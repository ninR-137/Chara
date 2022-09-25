package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
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


	//----------------------------------------------//

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		extendMainViewPort.update(width,height);
		extendB2DViewPort.update(width, height);
	}

	@Override
	public void render () {
		update(Gdx.graphics.getDeltaTime());

		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		b2dDebugRenderer.render(b2dModel.world, B2DCam.combined);

		mainSpriteBatch.setProjectionMatrix(mainCamera.combined);

		mainSpriteBatch.begin();
		player.render(mainSpriteBatch);
		mainSpriteBatch.end();




		sr.setProjectionMatrix(B2DCam.combined);

		sr.begin(ShapeRenderer.ShapeType.Line);
		sr.line(rightLegP1, rightLegP2);
		sr.line(leftLegP1, leftLegP2);
		if(mouse_leftClicked)sr.line(playerPoint, normalizedAttackDirection);
		sr.end();

	}


	public void update(float dt){
		updateCameraPosition();
		extendMainViewPort.apply();
		extendB2DViewPort.apply();

		b2dModel.logicStep(dt);
		player.positionX = b2dModel.playerBody.getPosition().x*PPM;
		player.positionY = b2dModel.playerBody.getPosition().y*PPM;
		playerRaytraceSensorUpdates();
		player.update();
		mouse_leftClicked = false;
	}


	private void playerRaytraceSensorUpdates(){
		rightSideAngleInDegrees = 0;
		leftSideAngleInDegrees = 0;
		leftSideValue --;
		rightSideValue --;
		b2dModel.setRayCastPoints();
		playerAngleBasis = new Vector2(playerPoint.x, playerPoint.y + 1/PPM);
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
			adjustmentX = player.isFacingRight ? 10 : -10;
		} else {
			adjustmentX = player.isFacingRight ? 8 : -8;
		}
		position.x = mainCamera.position.x + (player.positionX - (mainCamera.position.x)) * lerp;
		position.y = mainCamera.position.y + (player.positionY - mainCamera.position.y) * lerp;


		Vector3 v3 = new Vector3();
		v3.x = position.x/PPM;
		v3.y = position.y/PPM;
		mainCamera.position.set(position);
		mainCamera.update();
		B2DCam.position.set(v3);
		B2DCam.update();


		//--------------------------------------------------------------------------//
		if (Rumble.getRumbleTimeLeft() > 0){
			Rumble.tick(Gdx.graphics.getDeltaTime());
			mainCamera.translate(Rumble.getPos());
		}
		//---------------------------------------------------//
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
