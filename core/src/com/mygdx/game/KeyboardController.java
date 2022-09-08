package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mygdx.game.Entities.PlayerState;

import static com.mygdx.game.GlobalVariables.*;

public class KeyboardController implements InputProcessor{

    private B2dModel b2dModel;
    public KeyboardController(B2dModel b2dModel){
        this.b2dModel = b2dModel;
    }

    //JUST FOR FUTURE USE WHEN WE WANT TO CHANGE WORLDS
    public void setB2dModel(B2dModel b2dModel) {
        this.b2dModel = b2dModel;
    }

    @Override
    public boolean keyDown(int keycode) {

        switch (keycode){
            case Input.Keys.W : {
                held_W = true;
                if(B2dModel.m_jumpTimeout > 0) break;
                if(B2dModel.numFootContacts > 0 || B2dModel.remainingDoubleJump > 0) b2dModel.player.transitionState(PlayerState.JUMPING);
                break;
            }
            case Input.Keys.A: {
                held_A = true;
                if(B2dModel.numFootContacts != 0)  b2dModel.player.transitionState(PlayerState.RUNNING);
                if(B2dModel.numFootContacts >= 0) b2dModel.player.playerState.subCharacterState = PlayerState.GLIDING;
                b2dModel.player.playerState.isFacingRight = false;
                break;
            }
            case Input.Keys.D: {
                held_D = true;
                if(B2dModel.numFootContacts != 0)  b2dModel.player.transitionState(PlayerState.RUNNING);
                if(B2dModel.numFootContacts >= 0) b2dModel.player.playerState.subCharacterState = PlayerState.GLIDING;
                b2dModel.player.playerState.isFacingRight = true;
                break;
            }
            /*
            case Input.Keys.SHIFT_LEFT: {
                b2dModel.isPlayerBoosting = true;
            }
            */
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode){
            case Input.Keys.W: {
                held_W = false;
                break;
            }
            case Input.Keys.A: {
                if(B2dModel.numFootContacts != 0)  b2dModel.player.transitionState(PlayerState.IDLE);
                if(B2dModel.numFootContacts >= 0) b2dModel.player.playerState.subCharacterState = PlayerState.NONE;
                held_A = false;
                break;
            }
            case Input.Keys.D: {
                if(B2dModel.numFootContacts != 0)  b2dModel.player.transitionState(PlayerState.IDLE);
                if(B2dModel.numFootContacts >= 0) b2dModel.player.playerState.subCharacterState = PlayerState.NONE;
                held_D = false;
                break;
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    //private Vector3 tmp = new Vector3();

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        /*
        tmp.set(screenX, screenY, 0);
        mainCamera.unproject(tmp);
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            p2.set(tmp.x, tmp.y);
            b2dModel.world.rayCast( rayCastCallback, p1, p2);
        }
        else if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT)){
            p1.set(tmp.x, tmp.y);
        }
        return true;
        */
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
