package com.mygdx.game.Entities;
import java.util.*;

public class PlayerState {
    private Character currentState = IDLE;
    public Stack<Character> stateStack = new Stack<Character>();
    public boolean isFacingRight = true;
    public static final Character IDLE = 'I', RUNNING = 'R', JUMPING = 'J',
            RISING = 'V', FALLING = 'F', CRASH = 'C', CRASH_SLIDE = 'L';

    public PlayerState() {
        stateStack.push(IDLE);
    }

    public Character getPlayerState() {
        return currentState;
    }

    public void setPlayerState(Character state) {
        this.currentState = state;
    }
}
