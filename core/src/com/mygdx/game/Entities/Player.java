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
    private final Util util = new Util();
    private final Util util2 = new Util();
    private Sprite idleImage, runningImage, risingImage, fallingImage, crashingImage, crashSlideImage;
    private Sprite idleFlippedImage, runningFlippedImage, risingFlippedImage,
            fallingFlippedImage, crashingFlippedImage, crashFlippedSlideImage;

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
    }

    public void render(Batch batch){
        switch (playerState.getPlayerState()) {
            case 'I': {
                batch.draw(playerState.isFacingRight ? idleImage : idleFlippedImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
            case 'R' : {
                batch.draw(playerState.isFacingRight ? runningImage : runningFlippedImage, positionX - PlayerWidth / 2, positionY - PlayerHeight, PlayerWidth, PlayerWidth * 1.5f);
                break;
            }
            case  'V' : {
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
            default: {

            }
        }
    }
    public void update(){
        updateStateDueVelocity();
    }
    private void updateStateDueVelocity(){
        playerState.isFacingRight = currentVelocity.x >= 0;
        Character prevState = playerState.stateStack.pop();
        if(!playerState.getPlayerState().equals(PlayerState.CRASH)) util.resetTime();
        if(!playerState.getPlayerState().equals(PlayerState.CRASH_SLIDE)) util2.resetTime();

        if(B2dModel.numFootContacts <= 0){
            playerState.setPlayerState(currentVelocity.y > 0 ? PlayerState.RISING : PlayerState.FALLING);
            playerState.stateStack.push(playerState.getPlayerState());
            return;
        }

        playerState.setPlayerState(Math.abs(currentVelocity.x) > 0 ? returnNonZeroVelocityState(prevState, playerState.getPlayerState()) : returnZeroVelocityState(prevState, playerState.getPlayerState()));
        playerState.stateStack.push(playerState.getPlayerState());
    }

    private Character returnNonZeroVelocityState(Character prevState, Character currentState){
        if(prevState.equals(PlayerState.FALLING)) return PlayerState.CRASH_SLIDE;
        if(currentState.equals(PlayerState.CRASH_SLIDE)) {
            util2.countSeconds();
            if(util2.elapsedTimeInSecond >= 0.5f){
                util2.resetTime();
                return Math.abs(currentVelocity.x) >= 15 ? PlayerState.RUNNING : PlayerState.CRASH_SLIDE;
            }
            return Math.abs(currentVelocity.x) < 2 ? PlayerState.IDLE : PlayerState.CRASH_SLIDE;
        }
        return PlayerState.RUNNING;
    }
    private Character returnZeroVelocityState(Character prevState, Character currentState) {
        if(prevState.equals(PlayerState.FALLING)) return PlayerState.CRASH;
        if(currentState.equals(PlayerState.CRASH)) {
            util.countSeconds();
            if(util.elapsedTimeInSecond >= 0.5f){
                util.resetTime();
                return PlayerState.IDLE;
            }
            return PlayerState.CRASH;
        }
        return PlayerState.IDLE;
    }
}

