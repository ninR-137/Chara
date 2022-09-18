package com.mygdx.game.Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Util;

import static com.mygdx.game.GlobalVariables.*;

public class Player {

    public float positionX, positionY;
    public Vector2 currentVelocity = new Vector2();

    private final Util safeTimer = new Util(), attackCoolDown = new Util();
    private PlayerAnimationHandler playerAnimationHandler;

    public enum states {
        IDLE, RUNNING, JUMPING,
        RISING, FALLING, CRASH , CRASH_SLIDE , ROLLING, WALLCLING,
        WALLCLIMB, WALLCLIMBDOWN, GLIDING, NONE, LAND, DRAWING, ATTACKING;
    }

    public states previousState = states.IDLE;
    public states currentState = states.IDLE;
    public states subCharacterState = states.NONE;
    public boolean playerShouldJump = false;
    public boolean isFacingRight = true;
    //BE VERY CAREFUL WHEN USING THIS
    private boolean transitionLock = false;
    private boolean isHorizontalInput = false;
    private boolean isVerticalInput = false;

    public Player() {

        //TEST ANIMATIONS
        playerAnimationHandler = new PlayerAnimationHandler(this);
    }

    public void render(Batch batch){
        playerAnimationHandler.render(batch);
    }
    public void update(){
        miscUpdate();
        if(rightSideValue > 0 || leftSideValue > 0) {
            wallClingUpdate();
        } else {
            if (!isHorizontalInput && !isVerticalInput) transitionState(states.IDLE);
            horizontalStateHandler();
            jumpInputHandler();
            riseHandler();
            fallHandler();
        }
        attackUpdate();
    }

    public void transitionState(states state){
        if(transitionLock) return;
        previousState = currentState;
        currentState = state;
    }
    public void miscUpdate(){
        isVerticalInput = numFootContacts == 0;

        //System.out.println(leftSideValue + "||" + rightSideValue);
        if(Gdx.input.isKeyPressed(Input.Keys.W)) playerShouldJump = true;
        if(Gdx.input.isKeyPressed(Input.Keys.A)) isFacingRight = false;
        if(Gdx.input.isKeyPressed(Input.Keys.D)) isFacingRight = true;


        if(transitionLock) {
            safeTimer.countSeconds();
            if (safeTimer.elapsedTimeInSecond >= 0.75f) {
                safeTimer.resetTime();
                transitionLock = false;
            }
        } else {
            safeTimer.resetTime();
        }

    }

    public void attackUpdate(){
        attackCoolDown.countSeconds();
        if(mouse_leftClicked && attackCoolDown.elapsedTimeInSecond >= 0.5f) {
            attackCoolDown.resetTime();
            transitionState(states.DRAWING);
            transitionLock = true;
        }

        if(currentState.equals(states.DRAWING)){
            if(playerAnimationHandler.isDrawAnimFinished){
                transitionLock = false;
                transitionState(states.ATTACKING);
                playerAnimationHandler.isDrawAnimFinished = false;
                playerAnimationHandler.drawAnimTime = 0;
                transitionLock = true;
            }
        }

        if(currentState.equals(states.ATTACKING)){
            if(playerAnimationHandler.isAttackAnimFinished){
                transitionLock = false;
                playerAnimationHandler.isAttackAnimFinished = false;
                playerAnimationHandler.attackAnimtime = 0;
            }
        }
    }
    public void wallClingUpdate(){
        transitionState(states.WALLCLING);
        //SOME BUGS REGARDING TRANSITION LOCK DURING THE JUMPSTATE
        if(currentState.equals(states.JUMPING)){
            transitionLock = false;
            transitionState(states.WALLCLING);
        }
        if(held_W) transitionState(states.WALLCLIMB);
        if(held_S) transitionState(states.WALLCLIMBDOWN);
    }
    private void horizontalStateHandler(){
        boolean isGrounded = numFootContacts > 0;
        subCharacterState = states.NONE;
        if(held_D || held_A){
            isHorizontalInput = true;
            if(isGrounded) {
                transitionState(states.RUNNING);
                return;
            }
            subCharacterState = states.GLIDING;
            return;
        }
        //IF THE EXECUTION REACHES HERE IT MEANS INPUT VALUES FOR DIRECTION IS NOT PASSED
        isHorizontalInput = false;
    }
    private void jumpInputHandler(){
        if(!playerShouldJump || m_jumpTimeout > 0) return;
        if(numFootContacts > 0) {
            transitionState(states.JUMPING);
            transitionLock = true;
        }
        isVerticalInput = true;
        playerShouldJump = false;
    }
    private void riseHandler(){
        if(remainingJumpSteps > 0) {
            if(currentState.equals(states.JUMPING) && numFootContacts > 0){
                if(playerAnimationHandler.isJumAnimFinished){
                    transitionLock = false;
                    transitionState(states.RISING);
                    playerAnimationHandler.jumpAnimElapsedTime = 0;
                    playerAnimationHandler.isJumAnimFinished = false;
                    return;
                }
            }
            transitionState(states.RISING);
        }
    }
    private void fallHandler(){
        if(currentVelocity.y < 0 && numFootContacts <= 0) {
            transitionState(states.FALLING);
            return;
        }
        //CHECK IF CONDITIONS ARE FOR PLAYER CRASH
        if(numFootContacts > 0){
            //DUE TO OSCILLATING STATE CHANGES
            if(previousState.equals(states.FALLING)){
                states state = states.CRASH;
                //WILL BE REMOVING CRASH SLIDE FOR NOW
                transitionState(state);
                transitionLock = true;
                //System.out.println("TRANSITION IS NOW LOCKED");
                return;
            }
        }
        //CRASH TIMER HANDLER
        if(currentState.equals(states.CRASH)){
            if(playerAnimationHandler.isCrashAnimFinished){
                states state = held_A||held_D ? states.ROLLING : states.IDLE;
                transitionLock = false;
                transitionState(state);
                playerAnimationHandler.crashAnimElapsedTime = 0;
                playerAnimationHandler.isCrashAnimFinished = false;
                if(state.equals(states.ROLLING)){
                    transitionLock = true;
                }
            }
            return;
        }
        if(currentState.equals(states.ROLLING)){
            if(playerAnimationHandler.isRollingAnimFinished){
                states state = held_A || held_D ? states.RUNNING : states.IDLE;
                transitionLock = false;
                transitionState(state);
                playerAnimationHandler.rollingAnimTime = 0;
                playerAnimationHandler.isRollingAnimFinished = false;
            }
        }

    }



    public void dispose(){
        playerAnimationHandler.dispose();
    }
}

class PlayerAnimationHandler {
    private final Animation<TextureRegion> IdleAnimation, FlippedIdleAnimation;
    private final Animation<TextureRegion> RunAnimation, FlippedRunAnimation;
    private final Animation<TextureRegion> JumpAnimation, FlippedJumpAnimation;

    private final Animation<TextureRegion> ClingWallAnimation, FlippedClingWallAnimation;
    private final Animation<TextureRegion> ClimbWallAnimation, FlippedClimbWallAnimation;

    private final Animation<TextureRegion> RiseAnimation, FlippedRiseAnimation;
    private final Animation<TextureRegion> FallAnimation, FlippedFallAnimation;
    private final Animation<TextureRegion> CrashAnimation, FlippedCrashAnimation;
    private final Animation<TextureRegion> RollingAnimation, FlippedRollingAnimation;
    private final Animation<TextureRegion> DrawingAnimation, FlippedDrawingAnimation;
    private final Animation<TextureRegion> AttackAnimation, FlippedAttackAnimation;
    private final Animation<TextureRegion> AttackEffectAnimation, FlippedAttackEffectAnimation;

    private final Texture IdleTexture, RunTexture, JumpTexture, RiseTexture, FallTexture,
            CrashTexture, RollingTexture, ClingWallSpriteSheet, ClimbWallSpriteSheet, DrawSpriteSheet,
            AttackSpriteSheet ,placeHolder, AttackTexture, AttackPose, AttackEffect;

    float elapsedTime, jumpAnimElapsedTime, crashAnimElapsedTime, rollingAnimTime, drawAnimTime, attackAnimtime;

    public boolean isJumAnimFinished = false, isCrashAnimFinished = false, isRollingAnimFinished = false,
    isDrawAnimFinished = false, isAttackAnimFinished = false;
    private final Player player;
    private float positionX, positionY, width, height,
            effectPositionX, effectWidth, effectHeight;
    private TextureRegion currentFrame= new TextureRegion();
    private TextureRegion effectsFrame = new TextureRegion();

    private final TextureRegion AttackPoseRegion;
    private final TextureRegion FlippedAttackPoseRegion;

    public PlayerAnimationHandler(Player player){
        this.player = player;
        IdleTexture= new Texture("assets/Charas/CharaAnimTest/IdleSpriteSheet.png");
        RunTexture = new Texture("assets/Charas/CharaAnimTest/RunSpriteSheet.png");
        JumpTexture = new Texture("assets/Charas/CharaAnimTest/JumpSpriteSheet.png");
        RiseTexture = new Texture("assets/Charas/CharaAnimTest/RiseSpriteSheet.png");
        FallTexture = new Texture("assets/Charas/CharaAnimTest/FallSpriteSheet.png");
        CrashTexture = new Texture("assets/Charas/CharaAnimTest/CrashSpriteSheet.png");
        RollingTexture = new Texture("assets/Charas/CharaAnimTest/RollingSpriteSheet.png");
        ClingWallSpriteSheet = new Texture("assets/Charas/CharaAnimTest/ClingWallSpriteSheet.png");
        ClimbWallSpriteSheet = new Texture("assets/Charas/CharaAnimTest/ClimbSpriteSheet.png");
        DrawSpriteSheet = new Texture("assets/Charas/CharaAnimTest/DrawSpriteSheet.png");
        AttackSpriteSheet = new Texture("assets/Charas/CharaAnimTest/AttackSpriteSheet.png");
        AttackTexture = new Texture("assets/Charas/CharaAnimTest/AttackEffect.png");
        AttackPose = new Texture("assets/Charas/CharaAnimTest/AttackPose.png");
        AttackEffect = new Texture("assets/Charas/CharaAnimTest/AttackEffect2.png");
        placeHolder = new Texture("assets/Charas/CharaAnimTest/PlaceHolder.png");

        AttackPoseRegion = new TextureRegion(AttackPose);
        FlippedAttackPoseRegion = new TextureRegion(AttackPose);
        FlippedAttackPoseRegion.flip(true, false);

        IdleAnimation = animate(1, 8, IdleTexture,1/4f, false);
        FlippedIdleAnimation = animate(1, 8, IdleTexture,1/4f, true);

        RunAnimation = animate(1, 6, RunTexture,1/10f, false);
        FlippedRunAnimation = animate(1, 6, RunTexture,1/10f, true);

        JumpAnimation = animate(1, 5, JumpTexture,1/24f, false);
        FlippedJumpAnimation = animate(1, 5, JumpTexture,1/24f, true);

        RiseAnimation = animate(1, 3, RiseTexture,1/18f, false);
        FlippedRiseAnimation = animate(1, 3, RiseTexture,1/18f, true);

        FallAnimation = animate(1, 4, FallTexture,1/12f, false);
        FlippedFallAnimation = animate(1, 4, FallTexture,1/12f, true);

        CrashAnimation = animate(1, 6, CrashTexture,1/22f, false);
        FlippedCrashAnimation = animate(1, 6, CrashTexture,1/22f, true);

        RollingAnimation = animate(1,9, RollingTexture, 1/20f, false);
        FlippedRollingAnimation = animate(1,9, RollingTexture, 1/20f, true);

        ClingWallAnimation = animate(1, 2, ClingWallSpriteSheet, 1/2f, false);
        FlippedClingWallAnimation = animate(1, 2, ClingWallSpriteSheet, 1/2f, true);

        ClimbWallAnimation = animate(1, 4, ClimbWallSpriteSheet, 1/10f, false);
        FlippedClimbWallAnimation = animate(1, 4, ClimbWallSpriteSheet, 1/10f, true);

        DrawingAnimation = animate(1, 4, DrawSpriteSheet, 1/32f, false);
        FlippedDrawingAnimation = animate(1, 4, DrawSpriteSheet, 1/32f, true);

        AttackAnimation = animate(1, 3, AttackSpriteSheet, 1/8f, false);
        FlippedAttackAnimation = animate(1, 3, AttackSpriteSheet, 1/8f, true);

        AttackEffectAnimation = animate(1, 3, AttackTexture, 1/12f, false);
        FlippedAttackEffectAnimation = animate(1, 3, AttackTexture, 1/12f, true);

    }

    public void render(Batch batch){
        update();
        switch (player.currentState) {
            case IDLE: {
                currentFrame = player.isFacingRight ? FlippedIdleAnimation.getKeyFrame(elapsedTime,true) : IdleAnimation.getKeyFrame(elapsedTime, true);
                batch.draw(currentFrame,positionX, positionY, width, height);
                break;
            }
            case RUNNING : {
                currentFrame = player.isFacingRight ? FlippedRunAnimation.getKeyFrame(elapsedTime,true) : RunAnimation.getKeyFrame(elapsedTime, true);
                batch.draw(currentFrame,positionX, positionY, width, height);
                break;
            }
            case JUMPING: {
                jumpAnimElapsedTime += Gdx.graphics.getDeltaTime();
                currentFrame = player.isFacingRight ? FlippedJumpAnimation.getKeyFrame(jumpAnimElapsedTime,false) : JumpAnimation.getKeyFrame(jumpAnimElapsedTime, false);
                batch.draw(currentFrame,positionX, positionY, width, height);
                break;
            }
            case RISING: {
                currentFrame = player.isFacingRight ? FlippedRiseAnimation.getKeyFrame(elapsedTime,true) : RiseAnimation.getKeyFrame(elapsedTime, true);
                batch.draw(currentFrame,positionX, positionY, width, height);
                break;
            }
            case FALLING: {
                currentFrame = player.isFacingRight ? FlippedFallAnimation.getKeyFrame(elapsedTime,true) : FallAnimation.getKeyFrame(elapsedTime, true);
                batch.draw(currentFrame,positionX, positionY, width, height);
                break;
            }
            case CRASH: {
                crashAnimElapsedTime += Gdx.graphics.getDeltaTime();
                currentFrame = player.isFacingRight ? FlippedCrashAnimation.getKeyFrame(crashAnimElapsedTime,false) : CrashAnimation.getKeyFrame(crashAnimElapsedTime, false);
                batch.draw(currentFrame,positionX, positionY, width, height);
                break;
            }
            case ROLLING: {
                rollingAnimTime += Gdx.graphics.getDeltaTime();
                currentFrame = player.isFacingRight ? FlippedRollingAnimation.getKeyFrame(rollingAnimTime,false) : RollingAnimation.getKeyFrame(rollingAnimTime, false);
                batch.draw(currentFrame,positionX, positionY, width, height);
                break;
            }
            case WALLCLING:{
                currentFrame = player.isFacingRight ? FlippedClingWallAnimation.getKeyFrame(elapsedTime,true) : ClingWallAnimation.getKeyFrame(elapsedTime, true);
                batch.draw(currentFrame,positionX, positionY, width, height);
                break;
            }
            case WALLCLIMB:
            case WALLCLIMBDOWN: {
                currentFrame = player.isFacingRight ? FlippedClimbWallAnimation.getKeyFrame(elapsedTime,true) : ClimbWallAnimation.getKeyFrame(elapsedTime, true);
                batch.draw(currentFrame,positionX, positionY, width, height);
                break;
            }
            case DRAWING:{
                drawAnimTime += Gdx.graphics.getDeltaTime();
                currentFrame = player.isFacingRight ? FlippedDrawingAnimation.getKeyFrame(drawAnimTime,false) : DrawingAnimation.getKeyFrame(drawAnimTime, false);
                batch.draw(currentFrame,positionX, positionY, width, height);
                break;
            }
            case ATTACKING:{
                attackAnimtime += Gdx.graphics.getDeltaTime();
                //currentFrame = player.isFacingRight ? FlippedAttackAnimation.getKeyFrame(attackAnimtime,false) : AttackAnimation.getKeyFrame(attackAnimtime, false);

                currentFrame = player.isFacingRight ? FlippedAttackPoseRegion : AttackPoseRegion;
                effectsFrame = player.isFacingRight ? FlippedAttackEffectAnimation.getKeyFrame(attackAnimtime,false) : AttackEffectAnimation.getKeyFrame(attackAnimtime, false);
                batch.draw(currentFrame,positionX, positionY, width, height);

                //0 - 60 vertical Upwards
                //60 - 130 horizontal
                //130 - 180 vertical downwards
                float theta = 0;
                /*
                if (attackAngle >= 0 && attackAngle <= 60) theta = 90 - attackAngle;
                if (attackAngle > 60 && attackAngle < 130) theta = 0;
                if (attackAngle >= 130 && attackAngle <= 180) theta = -attackAngle + 90;
                */

                if (attackAngle >= 0 && attackAngle <= 60) theta = 45;
                if (attackAngle > 60 && attackAngle < 130) theta = 0;
                if (attackAngle >= 130 && attackAngle <= 180) theta = -45;

                if(!player.isFacingRight) {
                    if (attackAngle >= 0 && attackAngle <= 60) theta = -45;
                    if (attackAngle >= 130 && attackAngle <= 180) theta = 45;
                }

                //batch.draw(AttackEffect, effectPositionX, positionY, AttackEffect.getWidth(), AttackEffect.getHeight());
                batch.draw(effectsFrame, effectPositionX, positionY, effectWidth/2, effectHeight/2, effectWidth, effectHeight, 3, 0.5f, theta);
                break;
            }

        }
        //batch.draw(currentFrame,positionX, positionY, width, height);

    }

    private void update(){
        positionX = player.positionX - PlayerWidth /2;
        positionY = player.positionY - PlayerHeight;
        width = currentFrame.getRegionWidth();
        height = currentFrame.getRegionHeight();

        effectWidth = effectsFrame.getRegionWidth();
        effectHeight = effectsFrame.getRegionHeight();
        effectPositionX = positionX - effectWidth/2 + PlayerWidth/2;

        elapsedTime += Gdx.graphics.getDeltaTime();




        if(JumpAnimation.isAnimationFinished(jumpAnimElapsedTime)) isJumAnimFinished = true;
        if(CrashAnimation.isAnimationFinished(crashAnimElapsedTime)) isCrashAnimFinished = true;
        if(RollingAnimation.isAnimationFinished(rollingAnimTime)) isRollingAnimFinished = true;
        if(DrawingAnimation.isAnimationFinished(drawAnimTime)) isDrawAnimFinished = true;
        if(DrawingAnimation.isAnimationFinished(attackAnimtime)) isAttackAnimFinished = true;
    }

    public void dispose(){
        IdleTexture.dispose();
        RunTexture.dispose();
        JumpTexture.dispose();
        RiseTexture.dispose();
        FallTexture.dispose();
        CrashTexture.dispose();
        RollingTexture.dispose();
        ClingWallSpriteSheet.dispose();
        ClimbWallSpriteSheet.dispose();
        DrawSpriteSheet.dispose();
        AttackSpriteSheet.dispose();
        AttackTexture.dispose();
        AttackPose.dispose();
        AttackEffect.dispose();
        placeHolder.dispose();
    }
}

