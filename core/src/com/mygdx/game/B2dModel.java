package com.mygdx.game;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MotorJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mygdx.game.Entities.Player;

import static com.mygdx.game.B2dModel.*;
import static com.mygdx.game.GlobalVariables.*;

public class B2dModel {
    public World world;

    //----------------------PLAYER VARIABLES-------------------//
    public Player player;
    public Body playerBody;
    public RevoluteJoint playerRevoluteJoint; //TO HANDLE THE CIRCLE WHEEL / LEGS
    public int remainingJumpSteps; //HANDLES THE JUMPING FORCE (INCREMENTS THE FORCE INTO THE DESIRED STEP INTERVAL)
    //TODO: LIMIT PLAYER BOOST
    public boolean isPlayerBoosting = false; //HANDLES WHETHER THE PLAYER IS BOOSTING OR NOT

    public static int numFootContacts = 0; //HANDLES THE FOOT - GROUND COLLISION
    public static int remainingDoubleJump = 1; //HANDLES THE DOUBLE JUMP COUNT
    public int m_jumpTimeout; //TO MAKE SURE THAT YOU CANT JUMP IMMEDIATELY AS THE remainingJumpSteps IS STILL A NONZERO VALUE
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
        createPlayerBody(player.positionX, player.positionY);

        //TEST MAP
        createFloor();
        createWall();
    }

    // Physics logic goes here
    public void logicStep(float delta) {
        updatePlayerMovement();
        //setRayCastPoints();
        world.step(delta, 3, 3);
    }

    private void updatePlayerMovement(){
        //player.currentVelocity = playerBody.getLinearVelocity();
        //System.out.println(Math.floor(playerBody.getLinearVelocity().x) + "||" + Math.floor(playerBody.getLinearVelocity().y));
        //VELOCITY IS SOMEHOW OSCILATING DUE TO THE WHEEL
        //THIS IS TO AVOID UNCERTAINTIES
        float velocityX = (float) Math.floor(playerBody.getLinearVelocity().x);
        float velocityY = playerBody.getLinearVelocity().y;
        boolean isOscilatingX = (velocityX <= 1 && velocityX > 0) || (velocityX >= -1 && velocityX < 0);
        boolean isOscilatingY = (velocityY <= 5 && velocityY > 0) || (velocityY >= -5 && velocityY < 0);

        player.currentVelocity.y = isOscilatingY ? 0 : velocityY;
        player.currentVelocity.set(isOscilatingX ? 0 : velocityX, isOscilatingY ? 0 : velocityY);

        m_jumpTimeout--;

        if(remainingJumpSteps > 0){
            playerBody.applyForce(new Vector2(0, PLAYER_JUMP_FORCE), playerBody.getWorldCenter(), true);
            remainingJumpSteps--;
        }
        playerRevoluteJoint.setMotorSpeed(0);
        if(held_D && playerBody.getLinearVelocity().x <= PLAYER_MOVEMENT_VELOCITY && numFootContacts <= 0) playerBody.setLinearVelocity(new Vector2(PLAYER_MOVEMENT_VELOCITY, playerBody.getLinearVelocity().y));
        if(held_A && playerBody.getLinearVelocity().x >= -PLAYER_MOVEMENT_VELOCITY && numFootContacts <= 0) playerBody.setLinearVelocity(new Vector2(-PLAYER_MOVEMENT_VELOCITY, playerBody.getLinearVelocity().y));

        if(held_D && numFootContacts > 0) playerRevoluteJoint.setMotorSpeed(-PLAYER_MOVEMENT_VELOCITY);
        if(held_A && numFootContacts > 0) playerRevoluteJoint.setMotorSpeed(PLAYER_MOVEMENT_VELOCITY);

        if(isPlayerBoosting){
            Vector2 velocity = playerBody.getLinearVelocity();
            //Normalize then Scale
            velocity.nor();
            velocity.scl(50);
            playerBody.setLinearVelocity(velocity);
            isPlayerBoosting = false;
        }
    }
    private void createPlayerBody(float positionX, float positionY){
        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(positionX,positionY);

        playerBody = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(PlayerWidth/2, PlayerHeight/2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;

        playerBody.createFixture(fixtureDef).setUserData(PlayerUserData);
        playerBody.setFixedRotation(true);

        CircleShape s = new CircleShape();
        s.setRadius(1);
        s.setPosition(new Vector2(0,-PlayerHeight/2));
        fixtureDef.shape = s;
        fixtureDef.friction = 100f;
        fixtureDef.density = 2f;
        Body circleBody = world.createBody(bodyDef);

        circleBody.createFixture(fixtureDef).setUserData(FootUserData);
        circleBody.setFixedRotation(false);

        revoluteJointDef.enableMotor = true;
        revoluteJointDef.maxMotorTorque = 300f;

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
        rightLegP1.set(player.positionX + PlayerWidth/2, player.positionY - PlayerHeight/2);
        rightLegP2.set(player.positionX + PlayerWidth * 1.5f, player.positionY - PlayerHeight/2);

        leftLegP1.set(player.positionX - PlayerWidth/2, player.positionY - PlayerHeight/2);
        leftLegP2.set(player.positionX - PlayerWidth * 1.5f, player.positionY -PlayerHeight/2);

        world.rayCast(rightLegRayCastCallback, rightLegP1, rightLegP2);
        world.rayCast(leftLegRayCastCallback, leftLegP1, leftLegP2);
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
        shape.setAsBox(50, 1);
        bodys.createFixture(shape, 0.0f);

        shape.dispose();
    }


    private void createWall(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(-10, -8f);

        Body bodys = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();

        shape.setAsBox(1, 50, new Vector2(0,0), (float) Math.toRadians(45));
        bodys.createFixture(shape, 0.0f);
        shape.dispose();
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
