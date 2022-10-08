package com.mygdx.game.Handlers;

import static com.mygdx.game.GlobalVariables.B2DCam;
import static com.mygdx.game.GlobalVariables.PPM;
import static com.mygdx.game.GlobalVariables.Render_Box2D;
import static com.mygdx.game.GlobalVariables.b2dDebugRenderer;
import static com.mygdx.game.GlobalVariables.extendB2DViewPort;
import static com.mygdx.game.GlobalVariables.extendMainViewPort;
import static com.mygdx.game.GlobalVariables.leftArmP1;
import static com.mygdx.game.GlobalVariables.leftArmP2;
import static com.mygdx.game.GlobalVariables.leftLegP1;
import static com.mygdx.game.GlobalVariables.leftLegP2;
import static com.mygdx.game.GlobalVariables.leftSideAngleInDegrees;
import static com.mygdx.game.GlobalVariables.leftSideValue;
import static com.mygdx.game.GlobalVariables.mainCamera;
import static com.mygdx.game.GlobalVariables.mainSpriteBatch;
import static com.mygdx.game.GlobalVariables.mouse_leftClicked;
import static com.mygdx.game.GlobalVariables.normalizedAttackDirection;
import static com.mygdx.game.GlobalVariables.playerAngleBasis;
import static com.mygdx.game.GlobalVariables.playerPoint;
import static com.mygdx.game.GlobalVariables.rightArmP1;
import static com.mygdx.game.GlobalVariables.rightArmP2;
import static com.mygdx.game.GlobalVariables.rightLegP1;
import static com.mygdx.game.GlobalVariables.rightLegP2;
import static com.mygdx.game.GlobalVariables.rightSideAngleInDegrees;
import static com.mygdx.game.GlobalVariables.rightSideValue;
import static com.mygdx.game.GlobalVariables.sr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.B2dModel;
import com.mygdx.game.Entities.Player;
import com.mygdx.game.GlobalVariables;
import com.mygdx.game.KeyboardController;
import com.mygdx.game.Rumble;

public class WorldManager {
    private final initWorld firstWorld;

    public WorldManager(){
        firstWorld = new initWorld();
    }

    public void render(){
        firstWorld.render();
    }

    public void dispose(){
        firstWorld.dispose();
    }
}

class initWorld{
    private final B2dModel b2dModel;
    private final Texture img;
    private final Player player;

    TiledMap tiledMap;
    TiledMapRenderer tiledMapRenderer;
    public initWorld(){
        img = new Texture("assets/badlogic.jpg");
        player = new Player();
        tiledMap = new TmxMapLoader().load("assets/TiledMaps/TestMap2/TestMap3.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        b2dModel = new B2dModel(player, tiledMap);
        KeyboardController keyboardController = new KeyboardController();
        Gdx.input.setInputProcessor(keyboardController);
    }

    public void render(){
        update(Gdx.graphics.getDeltaTime());

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiledMapRenderer.setView(mainCamera);
        tiledMapRenderer.render();


        if(Render_Box2D) {
            b2dDebugRenderer.render(b2dModel.world, B2DCam.combined);
            sr.setProjectionMatrix(B2DCam.combined);

            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.line(rightArmP1, rightArmP2);
            sr.line(leftArmP1, leftArmP2);
            sr.line(rightLegP1, rightLegP2);
            sr.line(leftLegP1, leftLegP2);
            if (mouse_leftClicked) sr.line(playerPoint, normalizedAttackDirection);
            sr.end();
        }

        mainSpriteBatch.setProjectionMatrix(mainCamera.combined);

        mainSpriteBatch.begin();
        player.render(mainSpriteBatch);
        mainSpriteBatch.end();
    }


    private void update(float dt){
        updateCameraPosition();
        extendMainViewPort.apply();
        extendB2DViewPort.apply();

        b2dModel.logicStep(dt);
        if(b2dModel.playerBody != null) {
            player.positionX = b2dModel.playerBody.getPosition().x * PPM;
            player.positionY = b2dModel.playerBody.getPosition().y * PPM;
            playerRaytraceSensorUpdates();
        }
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


    public void updateCameraPosition(){
        Vector3 position = mainCamera.position;
        float lerp = 0.25f;

        float playerPositionY = (float) Math.floor(player.positionY/10) * 10;
        position.x = mainCamera.position.x + (player.positionX - (mainCamera.position.x)) * lerp;
        position.y = (float) Math.floor(mainCamera.position.y + (playerPositionY - mainCamera.position.y) * lerp);

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

    public void dispose () {
        img.dispose();
        GlobalVariables.dispose();
        sr.dispose();

        player.dispose();
        b2dModel.dispose();

        tiledMap.dispose();
    }
}
