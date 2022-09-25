package com.mygdx.game;

import com.badlogic.gdx.math.Vector3;

import java.util.Random;

public class Rumble {
    private static float time;
    private static float currentTime;
    private static float power;
    private static float currentPower;
    private static Random random;
    private static Vector3 pos = new Vector3();

    public static void rumble(float rumblePower, float rumbleLength){
        random = new Random();
        power = rumblePower;
        time = rumbleLength;
        currentTime = 0;
    }

    public static Vector3 tick(float deltaTime){
        if(currentTime <= time){
            currentPower = power * ((currentTime - time) / time);

            pos.x = (random.nextFloat() - 0.5f) * 2 * currentPower;
            pos.y = (random.nextFloat() - 0.5f) * 2 * currentPower;

            currentTime += deltaTime;
        }else {
            time = 0;
        }
        return pos;
    }

    public static float getRumbleTimeLeft(){
        return  time;
    }

    public static Vector3 getPos(){
        return pos;
    }
}
