package com.mygdx.game.Entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.B2dModel;
import com.mygdx.game.Util;
import org.w3c.dom.Text;
import sun.awt.X11.XSystemTrayPeer;

import static com.mygdx.game.GlobalVariables.*;
import static jdk.vm.ci.meta.JavaKind.Char;

public class Player {

    public float positionX, positionY;
    public Vector2 currentVelocity = new Vector2();
    public PlayerState playerState = new PlayerState();
    private final Util jumpTimer = new Util(), crashTimer = new Util(), rollingTimer = new Util();
    private final Sprite idleImage, runningImage, risingImage, fallingImage, crashingImage, crashSlideImage, jumpImage, rollingImage;
    private final Sprite idleFlippedImage, runningFlippedImage, risingFlippedImage,
            fallingFlippedImage, crashingFlippedImage, crashFlippedSlideImage, jumpFlippedImage, flippedRollingImage;

    public Player(float positionX, float positionY) {
        this.positionX = positionX;
        this.positionY = positionY;

        //TEST TEXTURES
        idleImage = new Sprite(new Texture("assets/Charas/IdleModel.png"));
        runningImage = new Sprite(new Texture("assets/Charas/RunningModel.png"));
        risingImage = new Sprite(new Texture("assets/Charas/RisingModel.png"));
        fallingImage = new Sprite(new Texture("assets/Charas/FallingModel.png"));
        crashingImage = new Sprite(new Texture("assets/Charas/CrashModel.png"));
        crashSlideImage = new Sprite(new Texture("assets/Charas/CrashSlideModel.png"));
        jumpImage = new Sprite(new Texture("assets/Charas/JumpModel.png"));
        rollingImage = new Sprite(new Texture("assets/Charas/RollingModel.png"));

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
            default: {

            }
        }
    }

    private boolean transitionMade = false;
    public void update(){
        transitionMade = false;
        updateJumpToRiseTransition(0.2f);
        if(!transitionMade)updateFallToCrashTransition(1f, 1.5f);
    }

    public void updateJumpToRiseTransition(float t){
        if(playerState.currentState.equals(PlayerState.JUMPING)){
            jumpTimer.countSeconds();
            if(jumpTimer.elapsedTimeInSecond >= t){
                jumpTimer.resetTime();
                transitionState(PlayerState.RISING);
            }
            return;
        }
        jumpTimer.resetTime();

        if(playerState.currentState.equals(PlayerState.RISING) && B2dModel.numFootContacts > 0){
            Character state = Math.abs(currentVelocity.x) > 0 ? PlayerState.ROLLING: PlayerState.CRASH;
            transitionState(state);
        }
    }

    public void updateFallToCrashTransition(float t, float t2){
        if(currentVelocity.y < 0) {
            Character crashState = Math.abs(currentVelocity.x) > 0 ? PlayerState.CRASH_SLIDE : PlayerState.CRASH;
            Character state = B2dModel.numFootContacts <= 0 ? PlayerState.FALLING : crashState;
            transitionState(state);
            return;
        }


        if(playerState.currentState.equals(PlayerState.FALLING) && currentVelocity.y == 0 && B2dModel.numFootContacts > 0){
            Character state = Math.abs(currentVelocity.x) > 0 ? PlayerState.CRASH_SLIDE : PlayerState.CRASH;
            transitionState(state);
            return;
        }


        if(playerState.currentState.equals(PlayerState.CRASH_SLIDE) || playerState.currentState.equals(PlayerState.CRASH)){
            crashTimer.countSeconds();
            float timer = t;
            if(playerState.currentState.equals(PlayerState.CRASH)) timer = 2;
            if(crashTimer.elapsedTimeInSecond >= timer){
                crashTimer.resetTime();
                Character state = Math.abs(currentVelocity.x) > 0 ? PlayerState.ROLLING : PlayerState.IDLE;
                transitionState(state);
            }
            return;
        }
        crashTimer.resetTime();

        if(playerState.currentState.equals(PlayerState.ROLLING)){
            rollingTimer.countSeconds();
            if(rollingTimer.elapsedTimeInSecond >= t2){
                rollingTimer.resetTime();
                Character state = held_A || held_D ? PlayerState.RUNNING : PlayerState.IDLE;
                transitionState(state);
            }
            return;
        }
        rollingTimer.resetTime();
    }

    public void transitionState(Character state){
        playerState.previousState = playerState.currentState;
        playerState.currentState = state;
        transitionMade = true;
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

