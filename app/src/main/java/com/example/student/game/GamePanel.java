package com.example.student.game;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    private static final int TANKPOS = HEIGHT - 100;

    private long nextSpeedingTime;
    private long smokeStartTime;
    private long missileStartTime;
    private long tankStartTime;
    private long fuelStartTime;

    private MainThread thread;
    private Background bg;
    private Player player;

    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<Bullet> bullets;
    private ArrayList<Tank> tanks;
    private ArrayList<Fuel> fuels;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BotBorder> botborder;

    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best;



    public GamePanel(Context context)
    {
        super(context);

        getHolder().addCallback(this);

        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000)
        {
            counter++;
            try{thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;

            }catch(InterruptedException e){e.printStackTrace();}

        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.skyline));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.aircraft), 65, 25, 3, 100);

        smoke = new ArrayList<Smokepuff>();
        missiles = new ArrayList<Missile>();
        bullets = new ArrayList<Bullet>();
        fuels = new ArrayList<Fuel>();
        tanks = new ArrayList<Tank>();

        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BotBorder>();

        smokeStartTime=  System.nanoTime();
        missileStartTime = System.nanoTime();
        tankStartTime = System.nanoTime();
        fuelStartTime = System.nanoTime();
        nextSpeedingTime = System.nanoTime();

        thread = new MainThread(getHolder(), this);

        //Gameloop starten
        thread.setRunning(true);
        thread.start();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            if(!player.getPlaying() && newGameCreated && reset)
            {
                player.setPlaying(true);
                player.setUp(true);
            }
            if(player.getPlaying())
            {

                if(!started)started = true;
                reset = false;
                player.setUp(true);
            }
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update()
    {
        if(player.getPlaying()) {
            if(botborder.isEmpty())
            {
                player.setPlaying(false);
                return;
            }
            if(topborder.isEmpty())
            {
                player.setPlaying(false);
                return;
            }
            if(player.getFuel() <= 0)
            {
                player.setY(player.getY() + 15);
            }

            bg.update();
            player.update();

            //untere Randkollision prüfen
            for(int i = 0; i<botborder.size(); i++)
            {
                if(collision(botborder.get(i), player))
                    player.setPlaying(false);
            }

            //obere Randkollision prüfen
            for(int i = 0; i <topborder.size(); i++)
            {
                if(collision(topborder.get(i),player))
                    player.setPlaying(false);
            }

            //Update oberen Rand
            this.updateTopBorder();

            //Update untere Rand
            this.updateBottomBorder();

            //Raketen hinzufügen
            long missileElapsed = (System.nanoTime()-missileStartTime)/1000000;
            int missilePos = (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight);
            while(Math.abs(missilePos-TANKPOS) <= 20.0f || missilePos > TANKPOS ){
                missilePos = (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight);
            }
            if(missileElapsed >(2000 - player.getScore()/4)){

                //erste Rakete geht immer durch die Mitte
                if(missiles.size()==0)
                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.
                            missile),WIDTH + 10, HEIGHT/2, 45, 15, player.getScore(), 13));
                }
                else
                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+10, missilePos,
                            45,15, player.getScore(),13));
                }
                //Timer zurücksetzen
                missileStartTime = System.nanoTime();
            }

            //Raketen auf Kollision prüfen
            for(int i = 0; i<missiles.size();i++)
            {
                //Update Rakete
                missiles.get(i).update();

                if(collision(missiles.get(i),player))
                {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //Raketen entfernen, wenn sie vom Bildschirm verschwinden
                if(missiles.get(i).getX()<-100)
                {
                    missiles.remove(i);
                    break;
                }
            }

            //Benzinkanister an anderer Stelle als Rakete einfügen
            long fuelElapsed = (System.nanoTime()-fuelStartTime)/1000000;
            if(fuelElapsed >(3000 - player.getScore()/4)){
                    int fuelPos = (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight);
                    while((Math.abs(fuelPos-missilePos) <= 20.0f && Math.abs(fuelPos-TANKPOS) <= 10.0f) || fuelPos > TANKPOS){
                        fuelPos = (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight);
                    }
                        fuels.add(new Fuel(BitmapFactory.decodeResource(getResources(),R.drawable.fuel),
                            WIDTH+10, fuelPos,
                            28,32, player.getScore(),1));

                //Timer zurücksetzen
                fuelStartTime = System.nanoTime();
            }

            for(int i = 0; i<fuels.size();i++)
            {
                //Update Benzinkanister
                fuels.get(i).update();

                if(collision(fuels.get(i),player))
                {
                    fuels.remove(i);
                    player.changeFuel(10);
                    break;
                }
                //Benzinkanister entfernen, wenn sie vom Bildschirm verschwinden
                if(fuels.get(i).getX()<-100)
                {
                    fuels.remove(i);
                    break;
                }
            }

            //Tanks hinzufügen
            long tankElapsed = (System.nanoTime()-tankStartTime)/1000000;
            if(tankElapsed >(4000 - player.getScore()/4)){
                    tanks.add(new Tank(BitmapFactory.decodeResource(getResources(),R.drawable.tank),
                            WIDTH+10, TANKPOS,
                            51,40, player.getScore(),1));

                //Timer zurücksetzen
                tankStartTime = System.nanoTime();
            }

            for(int i = 0; i<tanks.size();i++)
            {
                //Update Panzer
                tanks.get(i).update();
                if(tanks.get(i).getX() <= (WIDTH/4) * 3 && !tanks.get(i).getShot()){

                    tanks.get(i).shoot();
                    bullets.add(new Bullet(tanks.get(i).getX(), tanks.get(i).getY() + 10, player.getX(), player.getY()));
                }

                if(collision(tanks.get(i),player) )
                {
                    tanks.remove(i);
                    player.setPlaying(false);
                    break;
                }

                //Panzer entfernen, wenn sie vom Bildschirm verschwinden
                if(tanks.get(i).getX()<-100)
                {
                    tanks.remove(i);
                    break;
                }
            }

            for(int i = 0; i<bullets.size();i++)
            {
                //Bullets Panzer
                bullets.get(i).update();

                if(collision(bullets.get(i),player))
                {
                    bullets.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //Panzer entfernen, wenn sie vom Bildschirm verschwinden
                if(bullets.get(i).getX()<-100)
                {
                    bullets.remove(i);
                    break;
                }
            }


            //Rauchstöße hinzufügen
            long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
            if(elapsed > 120){
                smoke.add(new Smokepuff(player.getX(), player.getY()+10));
                smokeStartTime = System.nanoTime();
            }

            for(int i = 0; i<smoke.size();i++)
            {
                smoke.get(i).update();
                if(smoke.get(i).getX()<-10)
                {
                    smoke.remove(i);
                }
            }
        }
        else{
            player.resetDYA();
            if(!reset)
            {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),player.getX(),
                        player.getY()-30, 100, 100, 25);
            }

            explosion.update();
            long resetElapsed = (System.nanoTime()-startReset)/1000000;

            if(resetElapsed > 2500 && !newGameCreated)
            {
                newGame();
            }
        }
    }
    public boolean collision(GameObject a, GameObject b)
    {
        if(Rect.intersects(a.getRectangle(), b.getRectangle()))
        {
            return true;
        }
        return false;
    }
    @Override
    public void draw(Canvas canvas)
    {
        super.draw(canvas);
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        if(canvas!=null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if(!dissapear) {
                player.draw(canvas);
            }

            for(Smokepuff sp: smoke)
            {
                sp.draw(canvas);
            }

            for(Missile m: missiles)
            {
                m.draw(canvas);
            }

            for(Tank t: tanks)
            {
                t.draw(canvas);
            }

            for(Bullet b: bullets)
            {
                b.draw(canvas);
            }

            for(Fuel f: fuels)
            {
                f.draw(canvas);
            }

            for(TopBorder tb: topborder)
            {
                tb.draw(canvas);
            }

            for(BotBorder bb: botborder)
            {
                bb.draw(canvas);
            }

            if(started)
            {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    public void updateTopBorder()
    {

        for(int i = 0; i<topborder.size(); i++)
        {

            topborder.get(i).update();

            //entfernen und hinzufügen neuer Randelemente, wenn sie vom Screen verschwinden
            if(topborder.get(i).getX()<-20)
            {
                topborder.remove(i);
                //Element von Arraylist entfernen und durch neues Element ersetzen

                if(topborder.get(topborder.size()-1).getHeight()>=maxBorderHeight)
                {
                    topDown = false;
                }
                if(topborder.get(topborder.size()-1).getHeight()<=minBorderHeight)
                {
                    topDown = true;
                }
                if(topDown)
                {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.border),
                            topborder.get(topborder.size()-1).getX()+20,0, topborder.get(topborder.size()-1).getHeight()));
                }
                else
                {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.border),
                            topborder.get(topborder.size()-1).getX()+20,0, topborder.get(topborder.size()-1).getHeight()));
                }

            }
        }

    }
    public void updateBottomBorder()
    {

        for(int i = 0; i<botborder.size(); i++)
        {
            botborder.get(i).update();

            if(botborder.get(i).getX()<-20)
            {
                botborder.remove(i);
                //Element von Arraylist entfernen und durch neues Element ersetzen

                if (botborder.get(botborder.size() - 1).getY() <= HEIGHT-maxBorderHeight)
                {
                    botDown = true;
                }
                if (botborder.get(botborder.size() - 1).getY() >= HEIGHT - minBorderHeight)
                {
                    botDown = false;
                }
                if (botDown) {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border),
                            botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1).getY() ));
                } else {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border),
                            botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1).getY() ));
                }
            }
        }
    }
    public void newGame()
    {
        dissapear = false;

        botborder.clear();
        topborder.clear();

        missiles.clear();
        tanks.clear();
        fuels.clear();
        smoke.clear();
        bullets.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        player.resetDYA();
        player.resetScore();
        player.resetFuel();
        player.setY(HEIGHT/2);

        if(player.getScore()>best)
        {
            best = player.getScore();
        }

        //Ränder erstellen
        for(int i = 0; i*20<WIDTH+40;i++)
        {
            //oberen Rand erstellen
            if(i==0)
            {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.border
                ),i*20,0, 10));
            }
            //Elemente hinzufügen, bis der Einstiegsbild gefüllt ist
            else
            {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.border
                ),i*20,0, topborder.get(i-1).getHeight()));
            }
        }
        for(int i = 0; i*20<WIDTH+40; i++)
        {
            //unteren Rand erstellen
            if(i==0)
            {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),R.drawable.border)
                        ,i*20,HEIGHT -60 ));
            }
            //Elemente hinzufügen, bis der Einstiegsbild gefüllt ist
            else
            {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border),
                        i * 20, botborder.get(i - 1).getY() ));
            }
        }

        newGameCreated = true;


    }
    public void drawText(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText("DISTANZ: " + (player.getScore()*3), 10, HEIGHT - 10, paint);
        int currentFuel = player.getFuel();
        if(currentFuel <= 0)
            currentFuel = 0;
        canvas.drawText("TREIBSTOFF: " + (currentFuel), WIDTH-280, HEIGHT - 10, paint);


        if(!player.getPlaying()&&newGameCreated&&reset)
        {
            Paint paint1 = new Paint();
            paint1.setTextSize(30);
            paint1.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            canvas.drawText("DRÜCKE UM ZU STARTEN", WIDTH/2 - 200, HEIGHT/2 - 100, paint1);

            paint1.setTextSize(20);
            canvas.drawText("HALTE ZUM STEIGEN", WIDTH/2- 200, HEIGHT/2 - 73, paint1);
            canvas.drawText("LASS LOS ZUM FALLEN", WIDTH/2- 200, HEIGHT/2 -50, paint1);
        }
    }

}