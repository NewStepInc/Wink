package com.pong.engine;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.pong.components.Utils;

import java.util.Random;

public class Ball {
    Pong pong;
    int x, y;
    int direction;
    float tortoiseSpeed, hareSpeed_origin, hareSpeed;
    Paint paint;

    int ballSize;

    public Ball(Pong pong, int x, int y) {
        ballSize = (int) (pong.svgBall.getPicture().getWidth() * pong.getScale() / 2); //(int) Utils.getPercentalSize(2, false);

        this.pong = pong;
        this.x = x;
        this.y = y;

        hareSpeed_origin = pong.getPercentalSize(3.5f, false);
        tortoiseSpeed = hareSpeed_origin * 3 / 5;

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Random rand = new Random();
    public void startPlaying(boolean isToBlue) {
        setXY((int) pong.getPercentalSize(50, true), (int) pong.getPercentalSize(50, false));
        direction = -45 + (Math.abs(rand.nextInt()) % 90);
        if (isToBlue)
            direction += 180;
    }

    public void setRound(int round) {
        hareSpeed = hareSpeed_origin + round * tortoiseSpeed / 6.0f;
    }

    public int updateFrame(Eye eyeBlue, Eye eyeGreen) {
        float gameSpeed = pong.isDoubleSpeed() ? hareSpeed : tortoiseSpeed;
        x = (int) (x + gameSpeed * Math.cos(Math.toRadians(direction)));
        y = (int) (y + gameSpeed * Math.sin(Math.toRadians(direction)));

        if (y < ballSize / 2) {
            y = ballSize / 2;
            direction = -direction;
        }
        if (y > pong.getPercentalSize(100, false) - ballSize / 2) {
            y = (int) (pong.getPercentalSize(100, false) - ballSize / 2);
            direction = -direction;
        }

        direction = eyeBlue.checkConflictBall();
        direction = eyeGreen.checkConflictBall();

        if (x < -ballSize / 2 - Utils.getPercentalSize(10, true)) {
            return -1;
        }

        if (x > pong.getPercentalSize(100, true) + ballSize / 2 + Utils.getPercentalSize(10, true)) {
            return 1;
        }
        return 0;
    }

    public void draw(Canvas canvas, int offsetX, int offsetY) {
        int x = this.x + offsetX;
        int y = this.y + offsetY;
        if (Utils.isPortrait())
        {
            x = canvas.getWidth() - (this.y + offsetY);
            y = this.x + offsetX;
        }
//        canvas.drawCircle(x, y, ballSize, paint);
        Utils.drawSVG(canvas, pong.svgBall, x, y, pong.getScale(), 0);
    }
}
