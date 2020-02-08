package com.example.student.game;

import android.graphics.Bitmap;

public class Tank extends Item {

    private boolean shot;

    public Tank(Bitmap res, int x, int y, int w, int h, int s, int numFrames)
    {
        super(res, x, y, w, h, s, numFrames);
        speed = 6;
        shot = false;
    }

    public boolean getShot(){
        return shot;
    }

    public void shoot(){
        shot = true;
    }
}