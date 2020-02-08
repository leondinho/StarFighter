package com.example.student.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

public class Fuel extends Item {

    public Fuel(Bitmap res, int x, int y, int w, int h, int s, int numFrames)
    {
        super(res, x, y, w, h, s, numFrames);

        //Geschwindigkeit abhänging vom score
        speed =  6 + (int) (rand.nextDouble()*score/30);

        if(speed>40)speed = 40;
    }
}