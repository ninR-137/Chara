package com.mygdx.game;
import java.util.Random;

public class Util {
    Random random;
    long start = System.nanoTime();
    public double elapsedTimeInSecond;

    public void countSeconds(){
        long end = System.nanoTime();
        long elapsedTime = end - start;
        // 1 second = 1_000_000_000 nano seconds
        elapsedTimeInSecond += (double) elapsedTime / 1000000000;
        long s = (long) elapsedTimeInSecond;
        //System.out.println(s + " seconds");
        start = end;
    }

    public void resetTime(){
        start = System.nanoTime();
        elapsedTimeInSecond = 0;
    }

    public Util(){
        random = new Random();
    }
}
