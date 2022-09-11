package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MotorJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mygdx.game.Entities.Player;
import com.mygdx.game.Entities.PlayerState;

import static com.mygdx.game.B2dModel.*;
import static com.mygdx.game.GlobalVariables.*;

public class B2dModel {
    public World world;

    //----------------------PLAYER VARIABLES-------------------//
    public Player player;
    public Body playerBody;
    public RevoluteJoint playerRevoluteJoint; //TO HANDLE THE CIRCLE WHEEL / LEGS

    //TODO: LIMIT PLAYER BOOST
    public boolean isPlayerBoosting = false; //HANDLES WHETHER THE PLAYER IS BOOSTING OR NOT

    //------------------------------------------------------------------------//
    //DOING SOME TESTING WITH TILED MAP PARSING
    private TiledMap tiledMap;
    public TiledMapImageLayer mapImage;
    //------------------------------------------------------------------------//
    /**
     * This class will handle all BOX2D handling for:
     * Player, Entities, Maps, RayTracing, etc.
     */
    public B2dModel(Player player){
        world = new World(new Vector2(0,WORLD_GRAVITY), true);
        world.setContactListener(new B2DModelContactListener());

        //PLAYER SETUP
        remainingJumpSteps = 0;
        m_jumpTimeout = 0;
        this.player = player;

        tiledMap = new TmxMapLoader().load("assets/TiledMaps/TestMap1/Test2.tmx");
        parseCollisionObjectLayer(world, tiledMap.getLayers().get("Object Layer 1").getObjects());
        parseSpawnObjectLayer(world, tiledMap.getLayers().get("Object Layer 2").getObjects());
        mapImage = (TiledMapImageLayer)tiledMap.getLayers().get("Ground");

        createPlayerBody(player.positionX, player.positionY);
        /*
        //TEST MAP
        createFloor();
        createWall(-40, 35);
        createWall(30, 0);
        */

    }

    public void renderMap(Batch batch){
        batch.draw(mapImage.getTextureRegion(), mapImage.getX(), mapImage.getY(), (float) mapImage.getTextureRegion().getRegionWidth(), (float) mapImage.getTextureRegion().getRegionHeight());
    }

    public void logicStep(float delta) {
        updatePlayerMovement();
        world.step(delta, 3, 3);
    }

    private void updatePlayerMovement(){
        //VELOCITY IS SOMEHOW OSCILLATING DUE TO THE WHEEL
        //THIS IS TO AVOID UNCERTAINTIES
        float velocityX = (float) Math.floor(playerBody.getLinearVelocity().x);
        float velocityY = playerBody.getLinearVelocity().y;
        boolean isOscillatingX = (velocityX <= 1 && velocityX > 0) || (velocityX >= -1 && velocityX < 0);
        boolean isOscillatingY = (velocityY <= 5 && velocityY > 0) || (velocityY >= -5 && velocityY < 0);

        player.currentVelocity.y = isOscillatingY ? 0 : velocityY;
        player.currentVelocity.set(isOscillatingX ? 0 : velocityX, isOscillatingY ? 0 : velocityY);

        stateMovementResolution();
    }
    public void stateMovementResolution(){
        m_jumpTimeout--;
        playerRevoluteJoint.setMotorSpeed(0); //By Default
        float v = player.playerState.isFacingRight ? -PLAYER_MOVEMENT_VELOCITY : PLAYER_MOVEMENT_VELOCITY;
        playerRevoluteJoint.enableMotor(true);
        playerRevoluteJoint.setMaxMotorTorque(PLAYER_WHEEL_TORQUE);
        switch (player.playerState.currentState){
            case 'J' : {
                //JUMP RESOLUTION
                remainingJumpSteps = 6;
                if(numFootContacts <= 0) remainingDoubleJump--;
                m_jumpTimeout = 15;
                break;
            }
            case 'V' : {
                //RISE RESOLUTION
                if(remainingJumpSteps > 0){
                    playerBody.applyForce(new Vector2(0, PLAYER_JUMP_FORCE), playerBody.getWorldCenter(), true);
                    remainingJumpSteps--;
                }
                break;
            }
            case 'R' : {
                //RUN RESOLUTION
                playerRevoluteJoint.setMotorSpeed(v);
                break;
            }
            case 'W' : {
                //WALL CLING RESOLUTION
                if(rightSideValue > 0 || leftSideValue > 0) playerBody.setLinearVelocity(0,0);
                else playerBody.setLinearVelocity(0, WORLD_GRAVITY);
                break;
            }
            case 'Z' : {
                //WALL CLIMB RESOLUTION
                float RevSpeed = 10;
                float motorRev = player.playerState.isFacingRight ? -RevSpeed : RevSpeed;
                playerBody.setLinearVelocity(0,RevSpeed);
                if(rightSideValue > 0 || leftSideValue > 0) {
                    remainingJumpSteps = 4;
                    if(numFootContacts <= 0) remainingDoubleJump--;
                    m_jumpTimeout = 15;
                    playerRevoluteJoint.setMotorSpeed(motorRev);
                }
                else playerBody.setLinearVelocity(0, WORLD_GRAVITY);
                break;
            }
            case 'Q' : {
                //WALL_CLIMB_DOWN RESOLUTION
                float RevSpeed = 10;
                float motorRev = player.playerState.isFacingRight ? RevSpeed : -RevSpeed;
                playerBody.setLinearVelocity(0,-RevSpeed);
                if(rightSideValue > 0 || leftSideValue > 0) playerRevoluteJoint.setMotorSpeed(motorRev);
                else playerBody.setLinearVelocity(0, WORLD_GRAVITY);
                break;
            }
            case 'L' : {
                //CRASH_SLIDE RESOLUTION
                break;
            }
            case 'C' : {
                //CRASH RESOLUTION
                playerBody.setLinearVelocity(0,0);
                playerRevoluteJoint.setMotorSpeed(0);
                playerRevoluteJoint.enableMotor(false);
                playerRevoluteJoint.setMaxMotorTorque(0);
                break;
            }
            case 'P' : {
                //ROLLING RESOLUTION
                playerRevoluteJoint.setMotorSpeed(v * 0.75f);
                break;
            }
        }

        //SUB_STATE RESOLUTIONS
        switch (player.playerState.subCharacterState){
            case 'G' : {
                //GLIDING RESOLUTION
                playerBody.setLinearVelocity(new Vector2(-1*v, playerBody.getLinearVelocity().y));
                break;
            }
            case 'X' : {
                playerBody.setLinearVelocity(new Vector2(0, WORLD_GRAVITY * 8f));
                break;
            }
        }

    }
    private void createPlayerBody(float positionX, float positionY){
        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(positionX,positionY);

        playerBody = world.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        //-------------------------------FOOT-----------------------------------//
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(0.5f,0.15f, new Vector2(0, -2f), 0);
        fixtureDef.shape = sensorShape;
        fixtureDef.isSensor = true;
        playerBody = world.createBody(bodyDef);

        playerBody.createFixture(fixtureDef).setUserData(FootUserData);
        playerBody.setFixedRotation(true);

        //-------------------------------BODY-----------------------------------//
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(PlayerWidth/2, PlayerHeight/2);

        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;

        playerBody.createFixture(fixtureDef).setUserData(PlayerUserData);
        playerBody.setFixedRotation(true);

        //------------------------------WHEEL----------------------------------//

        CircleShape s = new CircleShape();
        s.setRadius(1);
        s.setPosition(new Vector2(0,-PlayerHeight/2));
        fixtureDef.shape = s;
        fixtureDef.friction = 100f;
        fixtureDef.density = 2f;
        fixtureDef.isSensor = false;
        Body circleBody = world.createBody(bodyDef);

        circleBody.createFixture(fixtureDef);
        circleBody.setFixedRotation(false);



        revoluteJointDef.enableMotor = true;
        revoluteJointDef.maxMotorTorque = PLAYER_WHEEL_TORQUE;

        revoluteJointDef.bodyA = playerBody;
        revoluteJointDef.bodyB = circleBody;
        revoluteJointDef.collideConnected = false;
        revoluteJointDef.localAnchorA.set(0,-PlayerHeight/2);
        revoluteJointDef.localAnchorB.set(0f,-PlayerHeight/2);
        playerRevoluteJoint = (RevoluteJoint) world.createJoint(revoluteJointDef);

        shape.dispose();
        s.dispose();
    }

    public void setRayCastPoints(){
        //-----------------------SETTING RAY CAST POINTS POSITION---------------------------//
        rightLegP1.set(player.positionX + PlayerWidth/2, player.positionY + PlayerHeight/2);
        rightLegP2.set(player.positionX + PlayerWidth * 0.75f, player.positionY + PlayerHeight/2);

        leftLegP1.set(player.positionX - PlayerWidth/2, player.positionY + PlayerHeight/2);
        leftLegP2.set(player.positionX - PlayerWidth * 0.75f, player.positionY + PlayerHeight/2);

        world.rayCast(rightLegRayCastCallback, rightLegP1, rightLegP2);
        world.rayCast(leftLegRayCastCallback, leftLegP1, leftLegP2);
    }
    /**
     * TEST TILEDMAPS
     */
    public static void parseCollisionObjectLayer(World world, MapObjects objects){

        Body body;
        BodyDef bDef = new BodyDef();
        FixtureDef fDef = new FixtureDef();
        bDef.type = BodyDef.BodyType.StaticBody;

        for(MapObject object : objects){
            Shape shape = createCollisionShape(object);

            if(shape != null) { //Just the shapes are not of ChainShape or PolygonShape
                body = world.createBody(bDef);
                fDef.shape = shape;
                fDef.friction = 0.25f;
                //TODO : THIS IS ACTUALLY IMPORTANT
                //fDef.filter.categoryBits = BIT_GROUND;
                body.createFixture(fDef).setUserData(WallUserData);
                shape.dispose();
            }
        }
    }
    private static Shape createCollisionShape(Object object){
        if(object instanceof PolylineMapObject) return createPolyLine((PolylineMapObject) object);
        if(object instanceof PolygonMapObject) return createPolygon((PolygonMapObject) object);
        return null;
    }
    public static ChainShape createPolyLine(PolylineMapObject polylineMapObject){
        float[] vertices = polylineMapObject.getPolyline().getTransformedVertices();
        Vector2[] worldVertices = new Vector2[vertices.length/2];

        for(int i = 0; i < worldVertices.length; i++){
            worldVertices[i] = new Vector2(vertices[i*2], vertices[i*2 + 1]);
        }
        ChainShape cs = new ChainShape();
        cs.createChain(worldVertices);
        return cs;
    }

    public static ChainShape createPolygon(PolygonMapObject polygonMapObject){
        float[] vertices = polygonMapObject.getPolygon().getTransformedVertices();
        Vector2[] worldVertices = new Vector2[vertices.length/2];

        for(int i = 0; i < worldVertices.length; i ++) {
            worldVertices[i] = new Vector2(vertices[i*2], vertices[i*2 + 1]);
        }

        ChainShape cs = new ChainShape();
        cs.createChain(worldVertices);
        return cs;
    }

    public void parseSpawnObjectLayer(World world,MapObjects objects){

        Body body;
        BodyDef bDef = new BodyDef();
        bDef.type = BodyDef.BodyType.StaticBody;
        FixtureDef fDef = new FixtureDef();

        for(MapObject object : objects){
            if(!(object instanceof RectangleMapObject)) continue;
            PolygonShape shape = new PolygonShape();
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            String name = object.getName();
            if ("Spawn".equals(name)) {
                player.positionX = ((RectangleMapObject) object).getRectangle().x + ((RectangleMapObject) object).getRectangle().width / 2;
                player.positionY = ((RectangleMapObject) object).getRectangle().y + ((RectangleMapObject) object).getRectangle().height / 2;
            } else if ("EndSpawn".equals(name)) {
                shape.setAsBox(rectangle.width / 2, rectangle.height / 2,
                        new Vector2(rectangle.x + rectangle.getWidth() / 2,
                                rectangle.y + rectangle.height / 2), 0);
                fDef.shape = shape;
                fDef.isSensor = true;
                body = world.createBody(bDef);
                body.createFixture(fDef).setUserData("EndSpawn");
            } else if ("BackSpawn".equals(name)) {
                shape.setAsBox(rectangle.width / 2, rectangle.height / 2,
                        new Vector2(rectangle.x + rectangle.getWidth() / 2,
                                rectangle.y + rectangle.height / 2), 0);
                fDef.shape = shape;
                fDef.isSensor = true;
                body = world.createBody(bDef);
                body.createFixture(fDef).setUserData("BackSpawn");
            }
        }
    }

    /**
     * TEST PLATFORMS
     */
    private void createFloor(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, -10);

        Body bodys = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(200, 1);
        bodys.createFixture(shape, 0.0f);

        shape.dispose();
    }
    private void createWall(float positionX, float angle){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(positionX, -8f);

        Body bodys = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();

        shape.setAsBox(1, 50, new Vector2(0,0), (float) Math.toRadians(angle));
        bodys.createFixture(shape, 0.0f).setUserData(WallUserData);
        shape.dispose();
    }

    public void dispose(){
        world.dispose();
        tiledMap.dispose();
        mapImage.getTextureRegion().getTexture().dispose();
    }
}

class B2DModelContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture childA = contact.getFixtureA();
        Fixture childB = contact.getFixtureB();

        if(childA.getUserData() == FootUserData && numFootContacts < 2) {
            numFootContacts ++;
            remainingDoubleJump = 1;
        }
        if(childB.getUserData() == FootUserData && numFootContacts < 2) {
            numFootContacts ++;
            remainingDoubleJump = 1;
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture childA = contact.getFixtureA();
        Fixture childB = contact.getFixtureB();

        if(childA.getUserData() == FootUserData) numFootContacts--;
        if(childB.getUserData() == FootUserData) numFootContacts--;
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
