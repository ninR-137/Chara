package com.mygdx.game.Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.B2dModel;
import com.mygdx.game.GlobalVariables;
import com.mygdx.game.Rumble;
import com.mygdx.game.Util;

import static com.mygdx.game.GlobalVariables.*;

public class Player {

    public float positionX, positionY;
    public Vector2 currentVelocity = new Vector2();

    private final Util crashValidatorTimer = new Util(), attackCoolDown = new Util(), rollCoolDown = new Util(),
    ledgeJumpCoolDown = new Util();
    private PlayerAnimationHandler playerAnimationHandler;
    private Sound slashSound, weirdBass;

    public enum states {
        IDLE, RUNNING, JUMPING,
        RISING, FALLING, CRASH , CRASH_SLIDE , ROLLING, WALLCLING,
        WALLCLIMB, WALLCLIMBDOWN, GLIDING, NONE, LAND, DRAWING, ATTACKING,
        DEAD, LEDGE_UPWARDS;
    }

    public states previousState = states.IDLE;
    public states currentState = states.IDLE;
    public states subCharacterState = states.NONE;
    public boolean isFacingRight = true;
    //BE VERY CAREFUL WHEN USING THIS
    public boolean transitionLock = false;

    public Player() {

        //TEST ANIMATIONS
        playerAnimationHandler = new PlayerAnimationHandler(this);
        slashSound = Gdx.audio.newSound(Gdx.files.internal("SoundEffects/NormalAttack.mp3"));
        weirdBass = Gdx.audio.newSound(Gdx.files.internal("SoundEffects/Bomb_004.wav"));
    }

    public void render(Batch batch){
        playerAnimationHandler.render(batch);
    }
    public void update(){
        //TODO : CLING AND DRAW
        subCharacterState = states.NONE;

        switch (currentState) {
            case IDLE:
                checkAttack();

                if(held_A || held_D) transitionState(states.RUNNING);
                if(Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                    transitionState(states.JUMPING);
                    transitionLock = true;
                }


                checkLedgeJump();
                break;
            case RISING:
                checkAttack();
                if(held_A || held_D) subCharacterState = states.GLIDING;
                break;
            case FALLING:
                crashValidatorTimer.countSeconds();
                checkAttack();
                if(held_A || held_D) subCharacterState = states.GLIDING;
                if(numFootContacts != 0) {
                    if(crashValidatorTimer.elapsedTimeInSecond > 4f) {
                        transitionState(states.CRASH);
                        transitionLock = true;
                    }
                    else {
                        transitionState(states.IDLE);
                    }
                    crashValidatorTimer.resetTime();
                }

                break;
            case CRASH:
                if(playerAnimationHandler.isCrashAnimFinished){
                    playerAnimationHandler.isCrashAnimFinished = false;
                    playerAnimationHandler.crashAnimElapsedTime = 0;
                    transitionLock = false;
                    if(held_A || held_D) {
                        transitionState(states.ROLLING);
                        transitionLock = true;
                    }
                }
                break;
            case ROLLING:
                if(playerAnimationHandler.isRollingAnimFinished){
                    playerAnimationHandler.isRollingAnimFinished = false;
                    playerAnimationHandler.rollingAnimTime = 0;
                    transitionLock = false;
                    if(held_A || held_D) {
                        transitionState(states.RUNNING);
                    }
                }

                break;
            case RUNNING:
                checkAttack();
                boolean horizontalMovementInput = held_A||held_D;
                if(Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                    transitionState(states.JUMPING);
                    transitionLock = true;
                }
                if(!horizontalMovementInput) initStateCheck();

                checkLedgeJump();
                break;
            case JUMPING:
                if(playerAnimationHandler.isJumAnimFinished){
                    playerAnimationHandler.jumpAnimElapsedTime = 0;
                    playerAnimationHandler.isJumAnimFinished = false;
                    transitionLock = false;
                    transitionState(states.RISING);
                }
                break;
            case DRAWING:
                if(playerAnimationHandler.isDrawAnimFinished){
                    playerAnimationHandler.isDrawAnimFinished = false;
                    slashSound.play(0.65f);
                    playerAnimationHandler.drawAnimTime = 0;
                    transitionLock = false;
                    transitionState(states.ATTACKING);
                    transitionLock = true;
                }
                break;
            case ATTACKING:
                if(playerAnimationHandler.isAttackAnimFinished){
                    playerAnimationHandler.isAttackAnimFinished = false;
                    playerAnimationHandler.attackAnimtime = 0;
                    transitionLock = false;
                    transitionState(states.IDLE);
                }
                break;
            case LEDGE_UPWARDS: {
                ledgeJumpCoolDown.resetTime();
                transitionState(states.FALLING);
            }
        }

        initStateCheck();
        wallClimbUpdate();
    }

    private void checkAttack(){
        if(mouse_leftClicked && attackCoolDown.elapsedTimeInSecond > 1) {
            transitionState(states.DRAWING);
            transitionLock = true;
            attackCoolDown.resetTime();
        }
    }

    private void checkLedgeJump(){
        if(leftLegSideValue > 0 || rightLegSideValue > 0) {
            if(ledgeJumpCoolDown.elapsedTimeInSecond > 0.2f) {
                transitionState(states.LEDGE_UPWARDS);
            } else {
                transitionState(states.IDLE);
            }
        }
    }
    private void wallClimbUpdate(){
        ledgeJumpCoolDown.countSeconds();
        boolean leftCondition = leftSideValue > 0 && held_A;
        boolean rightCondition = rightSideValue > 0 && held_D;
        if(leftCondition || rightCondition) {
            transitionState(states.WALLCLING);
        }

        if(currentState.equals(states.WALLCLING)){
            if(held_W) {
                transitionState(states.WALLCLIMB);
                return;
            }
            if(held_S) {
                transitionState(states.WALLCLIMBDOWN);
                return;
            }

            if(!(leftCondition||rightCondition)) transitionState(states.IDLE);
        }
    }
    public void initStateCheck(){
        attackCoolDown.countSeconds();
        rollCoolDown.countSeconds();
        if(Gdx.input.isKeyPressed(Input.Keys.D)) isFacingRight = true;
        if(Gdx.input.isKeyPressed(Input.Keys.A)) isFacingRight = false;
        boolean Grounded = numFootContacts > 0;
        boolean movementInput = held_A||held_D||held_W;

        if(Grounded && !movementInput) transitionState(states.IDLE);
        if(!Grounded && currentVelocity.y <= 0) {
            transitionState(states.FALLING);
        }
        if(Grounded && Gdx.input.isKeyPressed(Input.Keys.S) && rollCoolDown.elapsedTimeInSecond > 1f) {
            transitionState(states.ROLLING);
            transitionLock = true;
            rollCoolDown.resetTime();
        }


        //IMPORTANT TO MAKE SURE JUMP STEPS GETS FINISHED
        if (remainingJumpSteps > 0) transitionState(states.RISING);
    }

    public void transitionState(states state){
        if(transitionLock) return;
        previousState = currentState;
        currentState = state;
    }



    public void dispose(){
        playerAnimationHandler.dispose();
        slashSound.dispose();
        weirdBass.dispose();
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

        JumpAnimation = animate(1, 5, JumpTexture,1/42f, false);
        FlippedJumpAnimation = animate(1, 5, JumpTexture,1/42f, true);

        RiseAnimation = animate(1, 3, RiseTexture,1/3f, false);
        FlippedRiseAnimation = animate(1, 3, RiseTexture,1/3f, true);

        FallAnimation = animate(1, 4, FallTexture,1/3f, false);
        FlippedFallAnimation = animate(1, 4, FallTexture,1/3f, true);

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

        AttackAnimation = animate(1, 3, AttackSpriteSheet, 1/4f, false);
        FlippedAttackAnimation = animate(1, 3, AttackSpriteSheet, 1/4f, true);

        AttackEffectAnimation = animate(1, 3, AttackTexture, 1/28f, false);
        FlippedAttackEffectAnimation = animate(1, 3, AttackTexture, 1/28f, true);

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
            case FALLING:
            case LEDGE_UPWARDS: {
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
                Rumble.rumble(12, .2f);
                attackAnimtime += Gdx.graphics.getDeltaTime();

                currentFrame = player.isFacingRight ? FlippedAttackPoseRegion : AttackPoseRegion;
                effectsFrame = player.isFacingRight ? FlippedAttackEffectAnimation.getKeyFrame(attackAnimtime,false) : AttackEffectAnimation.getKeyFrame(attackAnimtime, false);
                batch.draw(currentFrame,positionX, positionY, width, height);


                if (attackAngle >= 0 && attackAngle <= 60) approximatedAttackAngle = 45;
                if (attackAngle > 60 && attackAngle < 130) approximatedAttackAngle = 0;
                if (attackAngle >= 130 && attackAngle <= 180) approximatedAttackAngle = -45;

                if(!player.isFacingRight) {
                    if (attackAngle >= 0 && attackAngle <= 60) approximatedAttackAngle = -45;
                    if (attackAngle >= 130 && attackAngle <= 180) approximatedAttackAngle = 45;
                }
                
                batch.draw(effectsFrame, effectPositionX, positionY, effectWidth/2, effectHeight/2, effectWidth, effectHeight, 4, 0.5f, approximatedAttackAngle);
                break;
            }


        }
        //batch.draw(currentFrame,positionX, positionY, width, height);

    }

    private void update(){
        positionX = player.positionX - PlayerWidth /2;
        positionY = player.positionY - PlayerHeight;
        width = (PlayerWidth/currentFrame.getRegionWidth()) * currentFrame.getRegionWidth();
        height = PlayerHeight * 1.5f;

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

