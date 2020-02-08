package com.example.student.game;

import android.graphics.Bitmap;

public class Missile extends Item {

    public Missile(Bitmap res, int x, int y, int w, int h, int s, int numFrames)
    {
        super(res, x, y, w,h,s, numFrames);

        //Geschwindigkeit abhängig vom Score
        speed = 7 + (int) (rand.nextDouble()*score/30);

        //Bremse für Geschwindigkeit
        if(speed>40)speed = 40;
    }

    public void speedUp(){
        speed++;
    }
}