package com.example.student.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

public class Item extends GameObject {

    protected int score;
    protected int speed;
    protected Random rand = new Random();
    protected Animation animation = new Animation();
    protected Bitmap spritesheet;

    public Item(Bitmap res, int x, int y, int w, int h, int s, int numFrames)
    {
        super.x = x;
        super.y = y;
        width = w;
        height = h;
        score = s;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        for(int i = 0; i<image.length;i++)
        {
            image[i] = Bitmap.createBitmap(spritesheet, 0, i*height, width, height);
        }

        animation.setFrames(image);
        animation.setDelay(100-speed);

    }
    public void update(){
        x -= speed;
        animation.update();
    }

    public void draw(Canvas canvas)
    {
        try{
            canvas.drawBitmap(animation.getImage(),x,y,null);
        }catch(Exception e){}
    }

    @Override
    public int getWidth()
    {
        //Versatz für realistischere Kollision
        return width-10;
    }

}
