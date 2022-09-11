package com.mygdx.game.Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.B2dModel;
import com.mygdx.game.GlobalVariables;
import com.mygdx.game.Util;
import org.w3c.dom.Text;
import sun.awt.X11.XSystemTrayPeer;

import static com.mygdx.game.GlobalVariables.*;
import static jdk.vm.ci.meta.JavaKind.Char;

public class Player {

    public float positionX, positionY;
    public Vector2 currentVelocity = new Vector2();
    public PlayerState playerState = new PlayerState();
    private final Util jumpTimer = new Util(), crashTimer = new Util(),
            rollingTimer = new Util(), safeTimer = new Util();
    private final Sprite idleImage, runningImage, risingImage, fallingImage,
            crashingImage, crashSlideImage, jumpImage, rollingImage,
            wallClingImage , wallClimbImage, wallClimbDownImage;
    private final Sprite idleFlippedImage, runningFlippedImage, risingFlippedImage,
            fallingFlippedImage, crashingFlippedImage, crashFlippedSlideImage, jumpFlippedImage,
            flippedRollingImage, flippedWallClimbImage, flippedWallClingImage, flippedWallClimbDownImage;

    public Player() {
        //TODO : TEXTURE MANAGEMENT
        //TEST TEXTURES
        idleImage = new Sprite(new Texture("assets/Charas/IdleModel.png"));
        runningImage = new Sprite(new Texture("assets/Charas/RunningModel.png"));
        risingImage = new Sprite(new Texture("assets/Charas/RisingModel.png"));
        fallingImage = new Sprite(new Texture("assets/Charas/FallingModel.png"));
        crashingImage = new Sprite(new Texture("assets/Charas/CrashModel.png"));
        crashSlideImage = new Sprite(new Texture("assets/Charas/CrashSlideModel.png"));
        jumpImage = new Sprite(new Texture("assets/Charas/JumpModel.png"));
        rollingImage = new Sprite(new Texture("assets/Charas/RollingModel.png"));
        wallClingImage = new Sprite(new Texture("assets/Charas/WallClingModel.png"));
        wallClimbImage = new Sprite(new Texture("assets/Charas/WallClimbModel.png"));
        wallClimbDownImage = new Sprite(new Texture("assets/Charas/WallClimbDownModel.png"));

        //Flipped Textures
        idleFlippedImage = new Sprite(new Texture("assets/Charas/IdleModel.png"));
        idleFlippedImage.flip(true, false);
        runningFlippedImage = new Sprite(new Texture("assets/Charas/RunningModel.png"));
        runningFlippedImage.flip(true, false);
        risingFlippedImage = new Sprite(new Sprite(new Texture("assets/Charas/RisingModel.png")));
        risingFlippedImage.flip(true, false);
        fallingFlippedImage = new Sprite(new Texture("assets/Charas/FallingModel.png"));
        fallingFlippedImage.flip(true,false);
        crashingFlippedImage = new Sprite(new Texture("assets/Charas/CrashModel.png"));
        crashingFlippedImage.flip(true, false);
        crashFlippedSlideImage = new Sprite(new Texture("assets/Charas/CrashSlideModel.png"));
        crashFlippedSlideImage.flip(true, false);
        jumpFlippedImage = new Sprite(new Texture("assets/Charas/JumpModel.png"));
        jumpFlippedImage.flip(true, false);
        flippedRollingImage = new Sprite(new Texture("assets/Charas/RollingModel.png"));
        flippedRollingImage.flip(true, false);
        flippedWallClingImage = new Sprite(new Texture("assets/Charas/WallClingModel.png"));
        flippedWallClingImage.flip(true, false);
        flippedWallClimbImage = new Sprite(new Texture("assets/Charas/WallClimbModel.png"));
        flippedWallClimbImage.flip(true, false);
        flippedWallClimbDownImage = new Sprite(new Texture("assets/Charas/WallClimbDownModel.png"));
        flippedWallClimbDownImage.flip(true, false);
    }

    public void render(Batch batch){
        switch (playerState.currentState) {
            case 'I': {
                batch.draw(playerState.isFacingRight ? idleImage : idleFlippedImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
            case 'R' : {
                batch.draw(playerState.isFacingRight ? runningImage : runningFlippedImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
            case 'J' : {
                batch.draw(playerState.isFacingRight ? jumpImage : jumpFlippedImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
            case 'V' : {
                batch.draw(playerState.isFacingRight ? risingImage : risingFlippedImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
            case 'F' : {
                batch.draw(playerState.isFacingRight ? fallingImage : fallingFlippedImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
            case 'C' : {
                batch.draw(playerState.isFacingRight ? crashingImage : crashingFlippedImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
            case 'L' : {
                batch.draw(playerState.isFacingRight ? crashSlideImage : crashFlippedSlideImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
            case 'P' : {
                batch.draw(playerState.isFacingRight ? rollingImage : flippedRollingImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
            case 'W' : {
                batch.draw(playerState.isFacingRight ? wallClingImage : flippedWallClingImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
            case 'Z' : {
                batch.draw(playerState.isFacingRight ? wallClimbImage : flippedWallClimbImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
            case 'Q' : {
                batch.draw(playerState.isFacingRight ? wallClimbDownImage : flippedWallClimbDownImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
        }
    }

    //BE VERY CAREFUL WHEN USING THIS
    private boolean transitionLock = false;
    private boolean isHorizontalInput = false;
    private boolean isVerticalInput = false;
    //private int priorityStyle = 0;
    public void update(){
        miscUpdate();
        if(rightSideValue > 0 || leftSideValue > 0) {
            transitionState(PlayerState.WALLCLING);
            //SOME BUGS REGARDING TRANSITION LOCK DURING THE JUMPSTATE
            if(playerState.currentState.equals(PlayerState.JUMPING)){
                transitionLock = false;
                transitionState(PlayerState.WALLCLING);
            }
            if(held_W) transitionState(PlayerState.WALLCLIMB);
            if(held_S) transitionState(PlayerState.WALLCLIMBDOWN);
        } else {
            if (!isHorizontalInput && !isVerticalInput) {
                transitionState(PlayerState.IDLE);
            }
            horizontalStateHandler();
            jumpInputHandler();
            riseHandler(0.2f);
            fallHandler(0.2f, 0.3f);
        }
    }

    public void miscUpdate(){
        isVerticalInput = numFootContacts == 0;
        if(!playerState.currentState.equals(PlayerState.JUMPING)) jumpTimer.resetTime();
        if(!playerState.currentState.equals(PlayerState.CRASH_SLIDE) && !playerState.currentState.equals(PlayerState.CRASH)) crashTimer.resetTime();
        if(!playerState.currentState.equals(PlayerState.ROLLING)) rollingTimer.resetTime();

        //System.out.println(leftSideValue + "||" + rightSideValue);
        if(Gdx.input.isKeyPressed(Input.Keys.W)) playerState.playerShouldJump = true;
        if(Gdx.input.isKeyPressed(Input.Keys.A)) playerState.isFacingRight = false;
        if(Gdx.input.isKeyPressed(Input.Keys.D)) playerState.isFacingRight = true;

        //ON THE EXTREME CASE THAT OUR TRANSITION IS LOCKED AND THE TIMERS ARE NOT WORKING
        if(transitionLock) {
            safeTimer.countSeconds();
            if (safeTimer.elapsedTimeInSecond >= 0.5f) {
                //System.out.println("TIMER ERROR DETECTED TRANSITION LOCK DISABLED");
                safeTimer.resetTime();
                transitionLock = false;
            }
        } else {
            safeTimer.resetTime();
        }
    }

    private void horizontalStateHandler(){
        boolean isGrounded = numFootContacts > 0;
        playerState.subCharacterState = PlayerState.NONE;
        if(held_D || held_A){
            isHorizontalInput = true;
            if(isGrounded) {
                transitionState(PlayerState.RUNNING);
                return;
            }
            playerState.subCharacterState = PlayerState.GLIDING;
            return;
        }
        //IF THE EXECUTION REACHES HERE IT MEANS INPUT VALUES FOR DIRECTION IS NOT PASSED
        isHorizontalInput = false;
    }
    private void jumpInputHandler(){
        if(!playerState.playerShouldJump || m_jumpTimeout > 0) return;
        //THAT WAS AN ACCIDENT BUT I KINDA LIKE IT
        if(numFootContacts > 0) {
            transitionState(PlayerState.JUMPING);
            transitionLock = true;
        }
        isVerticalInput = true;
        playerState.playerShouldJump = false;
    }
    private void riseHandler(float t){
        // B2dModel.remainingJumpSteps > 0 is to make sure Rising State is finished
        if(remainingJumpSteps > 0) {
            //What we want is to stay in jump state at a time t then transition to Rising State
            if(playerState.currentState.equals(PlayerState.JUMPING) && numFootContacts > 0){
                jumpTimer.countSeconds();
                if(jumpTimer.elapsedTimeInSecond >= t){
                    jumpTimer.resetTime();
                    transitionLock = false;
                    transitionState(PlayerState.RISING);
                }
            }
            else {
                transitionState(PlayerState.RISING); //This is by default
            }
        }
    }

    //Falling and crashing states are dependent on velocity
    //Unlike horizontal and jumping states where it is dependent on input
    private void fallHandler(float t, float t2){
        //This one is an exception
        /*
        if(held_S && numFootContacts <= 0){
            playerState.subCharacterState = PlayerState.LAND;
        }
        */
        //------------------------------------------------------------------//

        if(currentVelocity.y < 0 && numFootContacts <= 0) {
            transitionState(PlayerState.FALLING);
            return;
        }

        //CHECK IF CONDITIONS ARE FOR PLAYER CRASH
        if(numFootContacts > 0){
            //DUE TO OSCILLATING STATE CHANGES
            if(playerState.previousState.equals(PlayerState.FALLING)){
                //Character state = Math.abs(currentVelocity.x) > 0 ? PlayerState.CRASH_SLIDE : PlayerState.CRASH;
                Character state;
                state = PlayerState.CRASH;
                boolean condition1 = currentVelocity.x - 5> 0 && playerState.isFacingRight;
                //I DONT KNOW WHY BUT THERE IS A SLIGHT ERROR
                boolean condition2 = currentVelocity.x + 5 < 0 && !playerState.isFacingRight; //THIS KINDA HANDLES IT BETTER?
                //if(condition1 || condition2) state = PlayerState.CRASH_SLIDE;
                //WILL BE REMOVING CRASH SLIDE FOR NOW
                transitionState(state);
                //System.out.println("Changed state to crash || Locked Transition Changes");
                transitionLock = true;
                return;
            }
        }

        //CRASH TIMER HANDLER
        if(playerState.currentState.equals(PlayerState.CRASH_SLIDE) || playerState.currentState.equals(PlayerState.CRASH)){
            crashTimer.countSeconds();
            if(crashTimer.elapsedTimeInSecond >= t){
                crashTimer.resetTime();
                Character state = held_A||held_D ? PlayerState.ROLLING : PlayerState.IDLE;
                //System.out.println("CHANGING STATE FROM CRASH TO : " + state + " || Unlocked Transition Changes");
                transitionLock = false;
                transitionState(state);
                if(state.equals(PlayerState.ROLLING)){
                    transitionLock = true;
                    //System.out.println("ROLLING STATE || LOCKED Transition Changes");
                }
            }
            return;
        }

        if(playerState.currentState.equals(PlayerState.ROLLING)){
            rollingTimer.countSeconds();
            if(rollingTimer.elapsedTimeInSecond >= t2){
                rollingTimer.resetTime();
                Character state = held_A || held_D ? PlayerState.RUNNING : PlayerState.IDLE;
                transitionLock = false;
                //System.out.println("CHANGING STATE FROM CRASH TO : " + state + " || Unlocked Transition Changes");
                transitionState(state);
            }
        }

    }
    public void transitionState(Character state){
        if(transitionLock) return;
        playerState.previousState = playerState.currentState;
        playerState.currentState = state;
    }



    public void dispose(){
        idleImage.getTexture().dispose();
        runningImage.getTexture().dispose();
        risingImage.getTexture().dispose();
        fallingImage.getTexture().dispose();
        crashingImage.getTexture().dispose();
        crashSlideImage.getTexture().dispose();

        idleFlippedImage.getTexture().dispose();
        runningFlippedImage.getTexture().dispose();
        risingFlippedImage.getTexture().dispose();
        fallingFlippedImage.getTexture().dispose();
        crashingFlippedImage.getTexture().dispose();
        crashFlippedSlideImage.getTexture().dispose();
    }
}

