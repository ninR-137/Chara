package com.mygdx.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GlobalVariables {

    /**
     * rendering can be handled by the spritebatch between
     * mainSpriteBatch.begin();
     * ....RENDERING CODE GOES HERE...
     * mainSpriteBatch.end();
     * -----------------------
     * mainCamera is using WorldUnits not Pixel Measurements
     * you can update camera position with : mainCamera.position.set(new Vector2(x,y), z);
     * -----------------------
     * Planning to use ExtendViewport for rendering the game world
     * never forget to always use : extendMainViewPort.apply();
     * in the render() method
     */

    public static int VIEWPORT_WIDTH = 45;
    public static  int VIEWPORT_HEIGHT =45;
    public static final SpriteBatch mainSpriteBatch = new SpriteBatch();

    //SOMEHOW THIS POSTED NO PROBLEMS NO WIERD PPM CONVERSIONS IS NEEDED IF I JUST ASSUME MY MAIN CAMERA TO BE IN METERS
    //RATHER THAN PIXELS
    public static final OrthographicCamera mainCamera = new OrthographicCamera(VIEWPORT_WIDTH,VIEWPORT_HEIGHT); //This measurement is always in world units
    public static final ExtendViewport extendMainViewPort = new ExtendViewport(mainCamera.viewportWidth, mainCamera.viewportHeight, mainCamera);
    public static final float WORLD_GRAVITY = -40f;

    public static final float PLAYER_MOVEMENT_VELOCITY = 25f;
    public static final int PLAYER_WHEEL_TORQUE = 1000;
    public static final float PLAYER_JUMP_FORCE = 3000;

    public static final String PlayerUserData = "Player", FootUserData = "Foot", WallUserData = "Wall";
    public static final float PlayerWidth = 2, PlayerHeight = 2f;
    public static final Box2DDebugRenderer b2dDebugRenderer = new Box2DDebugRenderer();

    //---------------------------------------RAY TRACING TEST FOR PLAYER-------------------------------------------------------//
    /**
     * There would be 2 rays to the feet of the player
     * one Left and one Right
     * To measure slope elevation from each side
     * There would also be 2 rays at each sie of the player to detect walls
     */

    //RIGHT LEG
    public static ShapeRenderer sr = new ShapeRenderer();
    public static Vector2 rightLegP1 = new Vector2(), rightLegP2 = new Vector2();
    public static float rightSideAngleInDegrees;
    public static Vector2 leftLegP1 = new Vector2(), leftLegP2 = new Vector2();
    public static float leftSideAngleInDegrees;
    public static int leftSideValue = -1, rightSideValue = -1;


    public static RayCastCallback rightLegRayCastCallback = new RayCastCallback() {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            Vector2 v = new Vector2(rightLegP2.x - rightLegP1.x, rightLegP2.y - rightLegP1.y);
            Vector2 vnorm = v.nor();
            rightSideAngleInDegrees = (float) Math.toDegrees(Math.acos(vnorm.dot(normal))) - 90;
            if(fixture.getUserData() != null && fixture.getUserData().equals(WallUserData)){
                if(Math.round(rightSideAngleInDegrees) >= 80){
                    if(held_D && leftSideValue < 0 && !held_A) rightSideValue = 16;
                    if(held_A) rightSideValue = -100;
                }
            }
            return 0;
        }
    };

    public static RayCastCallback leftLegRayCastCallback = new RayCastCallback() {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            Vector2 v = new Vector2(leftLegP2.x - leftLegP1.x, leftLegP2.y - leftLegP1.y);
            Vector2 vnorm = v.nor();
            leftSideAngleInDegrees = (float) Math.toDegrees(Math.acos(vnorm.dot(normal))) - 90;
            if(fixture.getUserData() != null && fixture.getUserData().equals(WallUserData)){
                if(Math.round(leftSideAngleInDegrees) >= 80){
                    if(held_A && rightSideValue < 0 && !held_D) leftSideValue = 16;
                    if(held_D) leftSideValue = -100;
                }
            }

            return 0;
        }
    };
    //----------------------------------------------------------------------------------------------//

    //NOT SO CONSTANT VARIABLES
    //PLAYER CONTROLS
    public static boolean held_W = false, held_A = false, held_S = false, held_D = false;
    public static int remainingJumpSteps; //HANDLES THE JUMPING FORCE (INCREMENTS THE FORCE INTO THE DESIRED STEP INTERVAL)
    public static int numFootContacts = 0; //HANDLES THE FOOT - GROUND COLLISION
    public static int remainingDoubleJump = 1; //HANDLES THE DOUBLE JUMP COUNT
    public static int m_jumpTimeout; //TO MAKE SURE THAT YOU CANT JUMP IMMEDIATELY AS THE remainingJumpSteps IS STILL A NONZERO VALUE
    public static void dispose(){
        mainSpriteBatch.dispose();
    }
}
