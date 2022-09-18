package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mygdx.game.Entities.Player;

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
    //------------------------------------------------------------------------//
    /**
     * This class will handle all BOX2D handling for:
     * Player, Entities, Maps, RayTracing, etc.
     */
    public B2dModel(Player player){
        world = new World(new Vector2(0,WORLD_GRAVITY), true);
        world.setContactListener(new B2DModelContactListener());
        this.player = player;
        createPlayerBody(0, 100);

        //TEST MAP
        createFloor();
        //createWall(-20,-45);
        createWall(-100, 0);
        createWall(100, 0);
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
        float v = player.isFacingRight ? -PLAYER_MOVEMENT_VELOCITY : PLAYER_MOVEMENT_VELOCITY;
        playerRevoluteJoint.enableMotor(true);
        playerRevoluteJoint.setMaxMotorTorque(PLAYER_WHEEL_TORQUE);
        switch (player.currentState){
            case  JUMPING: {
                //JUMP RESOLUTION
                remainingJumpSteps = 6;
                if(numFootContacts <= 0) remainingDoubleJump--;
                m_jumpTimeout = 15;
                break;
            }
            case RISING : {
                //RISE RESOLUTION
                if(remainingJumpSteps > 0){
                    playerBody.applyForce(new Vector2(0, PLAYER_JUMP_FORCE), playerBody.getWorldCenter(), true);
                    remainingJumpSteps--;
                }
                break;
            }
            case RUNNING: {
                //RUN RESOLUTION
                playerRevoluteJoint.setMotorSpeed(v);
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
                playerRevoluteJoint.setMotorSpeed(v * 1.25f);
                break;
            }
        }

        //SUB_STATE RESOLUTIONS
        switch (player.subCharacterState){
            case GLIDING : {
                //GLIDING RESOLUTION
                float vel = PLAYER_MOVEMENT_AIR_VELOCITY  * (v/PLAYER_MOVEMENT_VELOCITY);
                playerBody.setLinearVelocity(new Vector2(-1*vel, playerBody.getLinearVelocity().y));
                break;
            }
            case LAND : {
                playerBody.setLinearVelocity(new Vector2(0, WORLD_GRAVITY * 8f));
                break;
            }
        }

    }


    private void createPlayerBody(float positionX, float positionY){
        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(positionX/PPM,positionY/PPM);

        playerBody = world.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        //-------------------------------FOOT-----------------------------------//
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(PlayerWidth/PPM * 0.25f ,PlayerHeight/PPM * 0.05f, new Vector2(0, -PlayerHeight/PPM), 0);
        fixtureDef.shape = sensorShape;
        fixtureDef.isSensor = true;
        playerBody = world.createBody(bodyDef);

        playerBody.createFixture(fixtureDef).setUserData(FootUserData);
        playerBody.setFixedRotation(true);

        //-------------------------------BODY-----------------------------------//
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(PlayerWidth/2/PPM, PlayerHeight/2/PPM);

        fixtureDef.shape = shape;
        fixtureDef.density = 1f/PPM;
        fixtureDef.friction = 0f;

        playerBody.createFixture(fixtureDef).setUserData(PlayerUserData);
        playerBody.setFixedRotation(true);

        //------------------------------WHEEL----------------------------------//

        CircleShape s = new CircleShape();
        s.setRadius(PlayerWidth/2/PPM);
        s.setPosition(new Vector2(0,-PlayerHeight/2/PPM));
        fixtureDef.shape = s;
        fixtureDef.friction = PLAYER_FRICTION;
        fixtureDef.density = 2f/PPM;
        fixtureDef.isSensor = false;
        Body circleBody = world.createBody(bodyDef);

        circleBody.createFixture(fixtureDef);
        circleBody.setFixedRotation(false);



        revoluteJointDef.enableMotor = true;
        revoluteJointDef.maxMotorTorque = PLAYER_WHEEL_TORQUE;

        revoluteJointDef.bodyA = playerBody;
        revoluteJointDef.bodyB = circleBody;
        revoluteJointDef.collideConnected = false;
        revoluteJointDef.localAnchorA.set(0,-PlayerHeight/2/PPM);
        revoluteJointDef.localAnchorB.set(0f,-PlayerHeight/2/PPM);
        playerRevoluteJoint = (RevoluteJoint) world.createJoint(revoluteJointDef);

        shape.dispose();
        s.dispose();
    }

    public void setRayCastPoints(){
        //-----------------------SETTING RAY CAST POINTS POSITION---------------------------//

        float positionY = player.positionY/PPM + PlayerHeight*0.25f/PPM;

        rightLegP1.set(player.positionX/PPM + PlayerWidth/2/PPM, positionY);
        rightLegP2.set(player.positionX/PPM + PlayerWidth * 0.75f/PPM, positionY);

        leftLegP1.set(player.positionX/PPM - PlayerWidth/2/PPM, positionY);
        leftLegP2.set(player.positionX/PPM - PlayerWidth * 0.75f/PPM, positionY);

        world.rayCast(rightLegRayCastCallback, rightLegP1, rightLegP2);
        world.rayCast(leftLegRayCastCallback, leftLegP1, leftLegP2);

        playerPoint.set(player.positionX/PPM, player.positionY/PPM);
    }

    /**
     * TEST PLATFORMS
     */
    private void createFloor(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, -600/PPM);

        Body bodys = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(10000/PPM, 25/PPM); //NOW WE ARE REFERRING TO THIS VIA PIXEL MEASUREMENT
        bodys.createFixture(shape, 0.0f);

        shape.dispose();
    }
    private void createWall(float positionX, float angle){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(positionX/PPM, -8f/PPM);

        Body bodys = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();

        shape.setAsBox(25/PPM, 1000/PPM, new Vector2(0,0), (float) Math.toRadians(angle));
        bodys.createFixture(shape, 0.0f).setUserData(WallUserData);
        shape.dispose();
    }

    public void dispose(){
        world.dispose();
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
