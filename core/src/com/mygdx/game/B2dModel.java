package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Entities.Player;

import java.security.SignatureSpi;
import java.util.Iterator;

import static com.mygdx.game.GlobalVariables.*;

public class B2dModel {
    public World world;
    private B2DModelContactListener b2DModelContactListener;

    //----------------------PLAYER VARIABLES-------------------//
    public Player player;
    public Body playerBody, circleBody;
    public RevoluteJoint playerRevoluteJoint, tempRevoluteJoint; //TO HANDLE THE CIRCLE WHEEL / LEGS


    //------------------------------------------------------------------------//
    private final TiledMapParser tiledMapParser;
    //------------------------------------------------------------------------//
    /**
     * This class will handle all BOX2D handling for:
     * Player, Entities, Maps, RayTracing, etc.
     */
    public B2dModel(Player player, TiledMap tiledMap){
        world = new World(new Vector2(0,WORLD_GRAVITY), true);
        b2DModelContactListener = new B2DModelContactListener(this);
        world.setContactListener(b2DModelContactListener);
        this.player = player;
        tiledMapParser = new TiledMapParser(tiledMap, this);
        createBody(tiledMapParser.playerSpawnX, tiledMapParser.playerSpawnY);

    }


    int timeStep = 12;
    int prioIndex = 0;
    public void logicStep(float delta) {
        updatePlayerMovement();
        world.step(1/40f, 8, 3);
        //TODO : MAKE A BETTER QUE
        //DESTROY BODIES FIRST
        //Make sure all bodies are destroyed

        if(Gdx.input.isKeyJustPressed(Input.Keys.X)){
            Array<Joint> joints = new Array<>();
            world.getJoints(joints);

            if(joints.contains(playerRevoluteJoint, true)){
                world.destroyJoint(playerRevoluteJoint);
                playerRevoluteJoint = null;
            }




            Array<Body> bodies = new Array<>();
            world.getBodies(bodies);

            if(bodies.contains(playerBody, true)) {
                world.destroyBody(playerBody);
                playerBody = null;
            }
            if(bodies.contains(circleBody, true)) {
                world.destroyBody(circleBody);
                circleBody = null;
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            createBody(tiledMapParser.playerSpawnX, tiledMapParser.playerSpawnY);
        }

        b2DModelContactListener.update();

        //THEN QUE CREATING BODIES
    }

    private void updatePlayerMovement(){
        //VELOCITY IS SOMEHOW OSCILLATING DUE TO THE WHEEL
        //THIS IS TO AVOID UNCERTAINTIES
        if(playerBody == null) return;
        float velocityX = (float) Math.floor(playerBody.getLinearVelocity().x);
        float velocityY = playerBody.getLinearVelocity().y;
        boolean isOscillatingX = (velocityX <= 1 && velocityX > 0) || (velocityX >= -1 && velocityX < 0);
        boolean isOscillatingY = (velocityY <= 5 && velocityY > 0) || (velocityY >= -5 && velocityY < 0);

        player.currentVelocity.y = isOscillatingY ? 0 : velocityY;
        player.currentVelocity.set(isOscillatingX ? 0 : velocityX, isOscillatingY ? 0 : velocityY);
        stateMovementResolution();

    }
    public void stateMovementResolution(){
        if(playerRevoluteJoint == null) return;
        m_jumpTimeout--;
        playerRevoluteJoint.setMotorSpeed(0); //By Default
        float velocity = player.isFacingRight ? -PLAYER_MOVEMENT_VELOCITY : PLAYER_MOVEMENT_VELOCITY;
        playerRevoluteJoint.enableMotor(true);
        playerRevoluteJoint.setMaxMotorTorque(PLAYER_WHEEL_TORQUE);
        switch (player.currentState){
            case  JUMPING: {
                //JUMP RESOLUTION
                PLAYER_BOOST_VALUE = PLAYER_JUMP_FORCE;
                remainingJumpSteps = 6;
                if(numFootContacts <= 0) remainingDoubleJump--;
                m_jumpTimeout = 15;

                break;
            }
            case RISING : {
                //RISE RESOLUTION
                if (remainingJumpSteps > 0) {
                    playerBody.applyForce(new Vector2(0, PLAYER_BOOST_VALUE), playerBody.getWorldCenter(), true);
                    remainingJumpSteps--;
                }
                playerRevoluteJoint.setMotorSpeed(velocity);
                break;
            }
            case FALLING: {
                playerRevoluteJoint.setMotorSpeed(velocity/2);
                break;
            }
            case RUNNING: {
                //RUN RESOLUTION AND FALLING RESOLUTION
                playerRevoluteJoint.setMotorSpeed(velocity);
                break;
            }
            case WALLCLING: {
                //WALL CLING RESOLUTION
                if(rightSideValue > 0 || leftSideValue > 0) playerBody.setLinearVelocity(0,0);
                else playerBody.setLinearVelocity(0, WORLD_GRAVITY);
                break;
            }
            case WALLCLIMB: {
                //WALL CLIMB RESOLUTION
                float RevSpeed = 15;
                float motorRev = player.isFacingRight ? -RevSpeed : RevSpeed;
                PLAYER_BOOST_VALUE = PLAYER_CLIMB_JUMP_FORCE;
                playerBody.setLinearVelocity(0,RevSpeed);

                if(rightSideValue > 0 || leftSideValue > 0) {
                    remainingJumpSteps = 6;
                    if(numFootContacts <= 0) remainingDoubleJump--;
                    m_jumpTimeout = 15;
                    playerRevoluteJoint.setMotorSpeed(motorRev);
                }
                else playerBody.setLinearVelocity(0, WORLD_GRAVITY);
                break;
            }
            case WALLCLIMBDOWN: {
                //WALL_CLIMB_DOWN RESOLUTION
                if(numFootContacts > 0) break;
                float RevSpeed = 15;
                float motorRev = player.isFacingRight ? RevSpeed : -RevSpeed;
                playerBody.setLinearVelocity(0,-RevSpeed);
                if(rightSideValue > 0 || leftSideValue > 0) playerRevoluteJoint.setMotorSpeed(motorRev);
                else playerBody.setLinearVelocity(0, WORLD_GRAVITY);
                break;
            }
            case CRASH_SLIDE: {
                //CRASH_SLIDE RESOLUTION
                break;
            }
            case CRASH: {
                //CRASH RESOLUTION
                playerBody.setLinearVelocity(0,0);
                playerRevoluteJoint.setMotorSpeed(0);
                playerRevoluteJoint.enableMotor(false);
                playerRevoluteJoint.setMaxMotorTorque(0);
                break;
            }
            case ROLLING: {
                //ROLLING RESOLUTION
                m_jumpTimeout = 6;
                playerRevoluteJoint.setMotorSpeed(velocity * 1.25f);
                break;
            }
            case LEDGE_UPWARDS:{
                playerBody.applyForce(new Vector2( playerBody.getLinearVelocity().x, 1000/PPM), new Vector2(PlayerWidth, PlayerHeight), true);
                player.subCharacterState = Player.states.GLIDING;
            }
        }

        //SUB_STATE RESOLUTIONS
        switch (player.subCharacterState){
            case GLIDING : {
                //GLIDING RESOLUTION
                float vel = PLAYER_MOVEMENT_AIR_VELOCITY  * (velocity /PLAYER_MOVEMENT_VELOCITY);
                playerBody.setLinearVelocity(new Vector2(-1*vel, playerBody.getLinearVelocity().y));
                break;
            }
            case LAND : {
                playerBody.setLinearVelocity(new Vector2(0, WORLD_GRAVITY * 8f));
                break;
            }
        }

    }

    private void createBody(float positionX, float positionY){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(positionX/PPM, positionY/PPM);

        if(playerBody == null || !playerBody.isActive())playerBody = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();

        //-------------------------------FOOT-----------------------------------//
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(PlayerWidth/PPM * 0.2f ,PlayerHeight/PPM * 0.1f, new Vector2(0, -PlayerHeight/PPM), 0);
        fixtureDef.shape = sensorShape;
        fixtureDef.isSensor = true;

        playerBody.createFixture(fixtureDef).setUserData(FootUserData);
        playerBody.setFixedRotation(true);

        //-------------------------------BODY-----------------------------------//
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(PlayerWidth/2/PPM, PlayerHeight/2/PPM);

        fixtureDef.shape = shape;
        fixtureDef.density = 1f/PPM;
        fixtureDef.friction = 1f;
        fixtureDef.isSensor = false;

        playerBody.createFixture(fixtureDef).setUserData(PlayerUserData);
        playerBody.setFixedRotation(true);

        shape.dispose();
        sensorShape.dispose();


        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(positionX/PPM, positionY/PPM);
        if(circleBody == null || !circleBody.isActive())circleBody = world.createBody(bodyDef);

        CircleShape s = new CircleShape();
        s.setRadius(PlayerWidth/2/PPM);
        s.setPosition(new Vector2(0,-PlayerHeight/2/PPM));
        fixtureDef.shape = s;
        fixtureDef.friction = PLAYER_FRICTION;
        fixtureDef.density = PLAYER_DENSITY;
        fixtureDef.isSensor = false;

        circleBody.createFixture(fixtureDef);
        circleBody.setFixedRotation(false);
        s.dispose();

        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
        revoluteJointDef.enableMotor = true;
        revoluteJointDef.maxMotorTorque = PLAYER_WHEEL_TORQUE;

        revoluteJointDef.bodyA = playerBody;
        revoluteJointDef.bodyB = circleBody;
        revoluteJointDef.collideConnected = false;
        revoluteJointDef.localAnchorA.set(0,-PlayerHeight/2/PPM);
        revoluteJointDef.localAnchorB.set(0f,-PlayerHeight/2/PPM);

        playerRevoluteJoint = (RevoluteJoint) world.createJoint(revoluteJointDef);
    }
    public void setRayCastPoints(){
        //-----------------------SETTING RAY CAST POINTS POSITION---------------------------//
        float positionY = player.positionY/PPM + PlayerHeight*0.25f/PPM;
        leftLegSideValue = 0;
        rightLegSideValue = 0;

        rightArmP1.set(player.positionX/PPM + PlayerWidth/2/PPM, positionY);
        rightArmP2.set(player.positionX/PPM + PlayerWidth * 0.75f/PPM, positionY);

        leftArmP1.set(player.positionX/PPM - PlayerWidth/2/PPM, positionY);
        leftArmP2.set(player.positionX/PPM - PlayerWidth * 0.75f/PPM, positionY);

        float legPositionY = player.positionY/PPM - PlayerHeight * 0.9f/PPM;

        rightLegP1.set(player.positionX/PPM + PlayerWidth/2/PPM, legPositionY);
        rightLegP2.set(player.positionX/PPM + PlayerWidth/PPM, legPositionY);

        leftLegP1.set(player.positionX/PPM - PlayerWidth/2/PPM, legPositionY);
        leftLegP2.set(player.positionX/PPM - PlayerWidth/PPM, legPositionY);

        world.rayCast(rightArmRayCastCallback, rightArmP1, rightArmP2);
        world.rayCast(leftArmRayCastCallback, leftArmP1, leftArmP2);

        world.rayCast(rightLegRayCastCallback, rightLegP1, rightLegP2);
        world.rayCast(leftLegRayCastCallBack, leftLegP1, leftLegP2);

        playerPoint.set(player.positionX/PPM, player.positionY/PPM);

    }
    public void dispose(){
        world.dispose();
    }

}

class B2DModelContactListener implements ContactListener {

    private final B2dModel model;
    public B2DModelContactListener(B2dModel model){
        this.model = model;
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture childA = contact.getFixtureA();
        Fixture childB = contact.getFixtureB();
        checkBeginFootCollision(childA, childB);
        checkDeathCollision(childA, childB);
    }

    @Override
    public void endContact(Contact contact) {
        Fixture childA = contact.getFixtureA();
        Fixture childB = contact.getFixtureB();
        checkEndFootCollision(childA, childB);
    }

    private void checkBeginFootCollision(Fixture childA, Fixture childB){
        try {
            if(childA.getUserData().equals(FootUserData) && childB.getUserData().equals(GroundUserData) && numFootContacts < 2) {
                numFootContacts ++;
                remainingDoubleJump = 1;
            }
            if(childB.getUserData().equals(FootUserData) && childA.getUserData().equals(GroundUserData) && numFootContacts < 2) {
                numFootContacts ++;
                remainingDoubleJump = 1;
            }
        } catch (NullPointerException ignored){
        }
    }
    private void checkEndFootCollision(Fixture childA, Fixture childB){
        try {
            if (childA.getUserData().equals(FootUserData) && childB.getUserData().equals(GroundUserData))
                numFootContacts--;
            if (childB.getUserData().equals(FootUserData) && childA.getUserData().equals(GroundUserData))
                numFootContacts--;
        } catch (NullPointerException ignored){
        }
    }
    private void checkDeathCollision(Fixture childA, Fixture childB){
        try{
            if(childA.getUserData().equals(PlayerUserData) && childB.getUserData().equals(DeathUserData)){
                return;
            }
            if(childB.getUserData().equals(PlayerUserData) && childA.getUserData().equals(DeathUserData)){
            }
        }catch (Exception ignored){}
    }

    public void update(){
        try {
            for (Body body : bodiesToDestroy) {
                body.getWorld().destroyBody(body);
                bodiesToDestroy.removeValue(body, true);
                body.setActive(false);
            }
        } catch (NullPointerException ignored){}
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
class TiledMapParser{
    private TiledMap tiledMap;
    private TiledMapTileLayer collisionTileLayer;
    private B2dModel model;
    private Body body;
    private TiledMapTileSets tileSets;
    private final BodyDef bodyDef;
    public float playerSpawnX, playerSpawnY; //Player position is in pixel measurement
    public TiledMapParser(TiledMap tiledMap,  B2dModel model){
        this.tiledMap = tiledMap;
        this.model = model;
        tileSets = tiledMap.getTileSets();
        collisionTileLayer = (TiledMapTileLayer) tiledMap.getLayers().get("CollisionLayer");

        MapObjects spawnObjects = tiledMap.getLayers().get("SpawnLayer").getObjects();

        for(MapObject mapObject : spawnObjects){
            if(!(mapObject instanceof RectangleMapObject)) continue;
            Rectangle rectangle = ((RectangleMapObject) mapObject).getRectangle();
            switch (mapObject.getName()) {
                case "Spawn":
                    playerSpawnX = rectangle.getX() + rectangle.width/2/PPM;
                    playerSpawnY = rectangle.getY() + rectangle.height/2/PPM;
                    break;
            }
        }

        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        createCollisionLayer();
        parseDeathCollision(model.world, tiledMap.getLayers().get("DeathLayer").getObjects());
    }

    //FIRST ITERATION COLLISION PARSER
    private void createCollisionLayer(){
        Iterator<TiledMapTileSet> tileSetIterator = tileSets.iterator();
        int tilePattern = 0;
        int lastXValue = 0;

        for(int y = 0; y < collisionTileLayer.getHeight(); y++){
            for(int x = 0; x < collisionTileLayer.getWidth(); x++) {
                if(collisionTileLayer.getCell(x, y) == null){
                    if(tilePattern != 0){
                        int startingX = lastXValue - tilePattern;
                        int width = tilePattern * 32;
                        float positionX = startingX + 1 + (float)width/2/PPM;
                        float positionY = (y + 1/2f)* collisionTileLayer.getTileHeight()/PPM;

                        createBox(positionX, positionY, width);
                        tilePattern = 0;
                    }
                }

                if(collisionTileLayer.getCell(x, y) != null){
                    tilePattern += 1;
                    lastXValue = x;
                }
            }
        }
    }



    private void createBox(float positionX, float positionY, float width){
        bodyDef.position.set(positionX, positionY);
        PolygonShape Box = new PolygonShape();
        Box.setAsBox(width/2/PPM, (float)collisionTileLayer.getTileHeight()/2/PPM);

        body = model.world.createBody(bodyDef);
        body.createFixture(Box, 0.0f).setUserData(GroundUserData);
        Box.dispose();
    }


    public void parseDeathCollision(World world, MapObjects objects){

        Body body;
        BodyDef bodyDef = new BodyDef();
        FixtureDef fixtureDef = new FixtureDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        for(MapObject object : objects){
            if(object instanceof RectangleMapObject){
                PolygonShape shape = new PolygonShape();
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                bodyDef.position.set(rect.x/PPM + rect.width/2/PPM, rect.y/PPM + rect.height/2/PPM);
                body = world.createBody(bodyDef);
                shape.setAsBox(rect.width/2/PPM, rect.height/2/PPM);
                fixtureDef.shape = shape;
                fixtureDef.isSensor = true;
                body.createFixture(fixtureDef).setUserData(DeathUserData);
            }
        }
    }
}
