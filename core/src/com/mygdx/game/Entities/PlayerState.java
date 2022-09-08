package com.mygdx.game.Entities;
import java.util.*;

public class PlayerState {
    public Character currentState = IDLE;
    public Character previousState = IDLE;
    public Character subCharacterState = NONE;
    public boolean isFacingRight = true;
    public static final Character IDLE = 'I', RUNNING = 'R', JUMPING = 'J',
            RISING = 'V', FALLING = 'F', CRASH = 'C', CRASH_SLIDE = 'L', ROLLING = 'P', GLIDING = 'G', NONE = 'N';

    public PlayerState() {

    }
}
