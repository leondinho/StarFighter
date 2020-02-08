package com.example.student.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Bullet extends GameObject {

    private int speed;
    private int playerPosX;
    private int playerPosY;
    public int r;

    public Bullet(int x, int y, int playerPosX, int playerPosY)
    {
        r = 5;
        super.x = x;
        super.y = y;

        speed = 30;
    }

    public void update() {
        //in Richtung Player schießen
        x -= speed;
        y -= (speed /5)*3;
    }

    public void draw(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x-r, y-r, r, paint);
    }

}