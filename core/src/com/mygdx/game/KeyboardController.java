package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import static com.mygdx.game.GlobalVariables.*;

public class KeyboardController implements InputProcessor{
    @Override
    public boolean keyDown(int keycode) {

        switch (keycode){
            case Input.Keys.W : {
                held_W = true;
                break;
            }
            case Input.Keys.A: {
                held_A = true;
                break;
            }
            case Input.Keys.D: {
                held_D = true;
                break;
            }
            case Input.Keys.S: {
                held_S = true;
                break;
            }
            case Input.Keys.SHIFT_LEFT: {
                break;
            }
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
                held_A = false;
                break;
            }
            case Input.Keys.D: {
                held_D = false;
                break;
            }
            case Input.Keys.S: {
                held_S = false;
                break;
            }
            case Input.Keys.SHIFT_LEFT: {
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
        //------------ATTACK RAY CAST TEST-----------------------//
        Vector3 tmp = new Vector3();
        tmp.set(screenX, screenY, 0);
        mainCamera.unproject(tmp);
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            attackEndPoint.set(tmp.x/PPM, tmp.y/PPM);
        }
        //NORMALIZE
        float xdirection = attackEndPoint.x - playerPoint.x;
        float ydirection = attackEndPoint.y - playerPoint.y;
        normalizedAttackDirection.set(xdirection , ydirection);
        normalizedAttackDirection.nor();

        Vector2 temp = new Vector2();
        temp.set(normalizedAttackDirection.x, normalizedAttackDirection.y);

        normalizedAttackDirection.scl(ATTACK_RANGE);
        normalizedAttackDirection.x += playerPoint.x;
        normalizedAttackDirection.y += playerPoint.y;

        temp.nor();
        playerAngleBasis.set(0, 1/PPM);
        playerAngleBasis.nor();
        float angle = (float) Math.toDegrees(Math.acos(temp.dot(playerAngleBasis)));

        attackAngle = (int) Math.floor(Math.round(angle));

        if(button == 0) mouse_leftClicked = true;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(button == 0) mouse_leftClicked = false;
        return false;
    }


    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
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
