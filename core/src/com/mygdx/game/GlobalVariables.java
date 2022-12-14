package com.mygdx.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Array;
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

    public static float VIEWPORT_SCALE = 0.4f;
    //THIS IS GOING TO BE IN PIXELS
    public static float VIEWPORT_WIDTH = 980 * VIEWPORT_SCALE;
    public static  float VIEWPORT_HEIGHT = 980 * VIEWPORT_SCALE;
    public static float PPM = 32; //PROTOTYPE
    public static final SpriteBatch mainSpriteBatch = new SpriteBatch();
    public static final OrthographicCamera mainCamera = new OrthographicCamera(VIEWPORT_WIDTH,VIEWPORT_HEIGHT); //This measurement is always in world units
    public static final OrthographicCamera B2DCam = new OrthographicCamera(VIEWPORT_WIDTH/PPM, VIEWPORT_HEIGHT/PPM);
    public static final ExtendViewport extendMainViewPort = new ExtendViewport(mainCamera.viewportWidth, mainCamera.viewportHeight, mainCamera);
    public static final ExtendViewport extendB2DViewPort = new ExtendViewport(B2DCam.viewportWidth, B2DCam.viewportHeight, B2DCam);
    public static boolean Render_Box2D = true;
    //-------------------------------------------------------------------------------------------------------------------------------------//
    public static final float PlayerWidth = 21.33f, PlayerHeight = 21f; //PIXEL MEASUREMENT
    private final static float DEFAULT_PLAYER_SIZE = PPM;
    public static final float WORLD_GRAVITY = -40f;
    public static final float PLAYER_MOVEMENT_VELOCITY = (1200f/PPM) * (PlayerWidth/DEFAULT_PLAYER_SIZE);
    public static final float PLAYER_MOVEMENT_AIR_VELOCITY = (250f/PPM) * (PlayerWidth/DEFAULT_PLAYER_SIZE);
    public static final float PLAYER_WHEEL_TORQUE = 6000;
    public static final float PLAYER_DENSITY = 0.1f;
    public static final float PLAYER_JUMP_FORCE = (400/PPM) * (PlayerWidth/DEFAULT_PLAYER_SIZE);
    public static final float PLAYER_CLIMB_JUMP_FORCE = (150/PPM) * (PlayerWidth/DEFAULT_PLAYER_SIZE);
    public static final float PLAYER_LEDGE_JUMP_FORCE = (100/PPM) * (PlayerWidth/DEFAULT_PLAYER_SIZE);

    public static float PLAYER_BOOST_VALUE = PLAYER_JUMP_FORCE;
    public static final float PLAYER_FRICTION = 500f/PPM;

    public static final String PlayerUserData = "Player", FootUserData = "Foot",
            WallUserData = "Wall", GroundUserData = "Ground", DeathUserData = "DEATH";
    public static final Box2DDebugRenderer b2dDebugRenderer = new Box2DDebugRenderer();

    //---------------------------------------RAY TRACING TEST FOR PLAYER-------------------------------------------------------//
    /**
     * There would be 2 rays to the feet of the player
     * one Left and one Right
     * To measure slope elevation from each side
     * There would also be 2 rays at each sie of the player to detect walls
     */

    public static ShapeRenderer sr = new ShapeRenderer();

    //RIGHT LEG
    public static Vector2 rightLegP1 = new Vector2(), rightLegP2 = new Vector2();
    public static float rightLegAngleInDegrees;
    //LEFT LEG
    public static Vector2 leftLegP1 = new Vector2(), leftLegP2 = new Vector2();
    public static float leftLegAngleInDegrees;

    //RIGHT ARM
    public static Vector2 rightArmP1 = new Vector2(), rightArmP2 = new Vector2();
    public static float rightSideAngleInDegrees;
    //LEFT ARM
    public static Vector2 leftArmP1 = new Vector2(), leftArmP2 = new Vector2();
    public static float leftSideAngleInDegrees;
    public static int leftSideValue = -1, rightSideValue = -1;
    public static int leftLegSideValue = -1, rightLegSideValue = -1;

    //ATTACK RANGE AND DIRECTION
    public static Vector2 playerPoint = new Vector2(), attackEndPoint = new Vector2();
    public static Vector2 normalizedAttackDirection = new Vector2();
    public static float ATTACK_RANGE = 5;
    public static Vector2 playerAngleBasis = new Vector2();
    public static int attackAngle;
    public static int approximatedAttackAngle = 0;


    public static RayCastCallback rightArmRayCastCallback = new RayCastCallback() {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            Vector2 v = new Vector2(rightArmP2.x - rightArmP1.x, rightArmP2.y - rightArmP1.y);
            Vector2 vnorm = v.nor();
            rightSideAngleInDegrees = (float) Math.toDegrees(Math.acos(vnorm.dot(normal))) - 90;
            if(fixture.getUserData() != null && fixture.getUserData().equals(WallUserData)){
                if(Math.round(rightSideAngleInDegrees) >= 80){
                    if(held_D && leftSideValue < 0) {
                        rightSideValue = 16;
                        if(held_A) rightSideValue = -100;
                    }
                }
            }
            return 0;
        }
    };

    public static RayCastCallback leftArmRayCastCallback = new RayCastCallback() {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            Vector2 v = new Vector2(leftArmP2.x - leftArmP1.x, leftArmP2.y - leftArmP1.y);
            Vector2 vnorm = v.nor();
            leftSideAngleInDegrees = (float) Math.toDegrees(Math.acos(vnorm.dot(normal))) - 90;
            if(fixture.getUserData() != null && fixture.getUserData().equals(WallUserData)){
                if(Math.round(leftSideAngleInDegrees) >= 80){
                    if(held_A && rightSideValue < 0) {
                        leftSideValue = 16;
                        if(held_D) leftSideValue = - 100;
                    }
                }
            }
            return 0;
        }
    };

    public static RayCastCallback rightLegRayCastCallback = new RayCastCallback() {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if(rightSideValue >= 16) return 0;

            Vector2 v = new Vector2(rightLegP2.x - rightLegP1.x, rightLegP2.y - rightLegP1.y);
            Vector2 vnorm = v.nor();
            rightLegAngleInDegrees = (float) Math.toDegrees(Math.acos(vnorm.dot(normal))) - 90;
            if(fixture.getUserData() != null && fixture.getUserData().equals(GroundUserData)){
                if(held_D)rightLegSideValue = 16;
            }
            return 0;
        }
    };

    public static RayCastCallback leftLegRayCastCallBack = new RayCastCallback() {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if(leftSideValue >= 16) return 0;

            Vector2 v = new Vector2(leftLegP2.x - leftLegP1.x, leftLegP2.y - leftLegP1.y);
            Vector2 vnorm = v.nor();
            leftLegAngleInDegrees = (float) Math.toDegrees(Math.acos(vnorm.dot(normal))) - 90;
            if(fixture.getUserData() != null && fixture.getUserData().equals(GroundUserData)){
                if(Math.round(leftLegAngleInDegrees) >= 80){
                    if(held_A)leftLegSideValue = 16;
                }
            }
            return 0;
        }
    };

    //----------------------------------------------------------------------------------------------//
    public static Animation<TextureRegion> animate(int rows, int cols, Texture spriteSheet, float frameDuration, boolean flipped) {
        TextureRegion[] frames = new TextureRegion[cols * rows];
        TextureRegion[][] tmpFrames = TextureRegion.split(spriteSheet, spriteSheet.getWidth()
                / cols, spriteSheet.getHeight() / rows);
        int index = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if(flipped) tmpFrames[i][j].flip(true, false);
                frames[index++] = tmpFrames[i][j];
            }
        }

        Animation<TextureRegion> animation = new Animation<TextureRegion>(frameDuration, frames);
        return animation;
    }
    //----------------------------------------------------------------------------------------------//
    //NOT SO CONSTANT VARIABLES
    //PLAYER CONTROLS
    public static boolean held_W = false, held_A = false, held_S = false, held_D = false, mouse_leftClicked = false;
    public static int remainingJumpSteps; //HANDLES THE JUMPING FORCE (INCREMENTS THE FORCE INTO THE DESIRED STEP INTERVAL)
    public static int numFootContacts = 0; //HANDLES THE FOOT - GROUND COLLISION
    public static int remainingDoubleJump = 1; //HANDLES THE DOUBLE JUMP COUNT
    public static int m_jumpTimeout; //TO MAKE SURE THAT YOU CANT JUMP IMMEDIATELY AS THE remainingJumpSteps IS STILL A NONZERO VALUE

    public static Array<Fixture> activationFixtures = new Array<>();
    public static Array<Fixture> deactivationFixtures = new Array<>();
    public static Array<Body> bodiesToDestroy = new Array<com.badlogic.gdx.physics.box2d.Body>();
    public static void dispose(){
        mainSpriteBatch.dispose();
    }
}
