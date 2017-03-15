package com.pong.engine;

import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Point;

import com.larvalabs.svgandroid.SVG;
import com.pong.components.PongSurface;
import com.pong.components.Utils;

import java.util.Random;

public class Eye {
    Pong pong;
    boolean isBlue;
    int x, y;
    int angle;
    boolean isClosed;
    long toggleEyeTime;
    float rotateAngle;
    int offX, offY;

    public Eye(Pong pong, boolean isBlue, int x, int y) {
        this.pong = pong;
        this.isBlue = isBlue;
        this.angle = 0;
        this.x = x;
        this.y = y;
        this.isClosed = true;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Eye setY(int y) {
        int size = (int) (pong.getSvgEyeClosed().getPicture().getWidth() * pong.getScale());
        this.y = (int) Math.min(Math.max(y, size / 2), pong.getPercentalSize(100, false) - size / 2);
        return this;
    }

    public void setClosed(boolean isClosed) {
        this.isClosed = isClosed;
    }

    public boolean getClosed() {
        return isClosed;
    }

    // move eye image to (x, y)
    int steps, curStep;
    public void moveEye(int x, int y) {
        offX = x - this.x;
        offY = y - this.y;
        steps = (int) (Math.sqrt(offX*offX + offY*offY) / 10 + 0.99999);
        curStep = -1;
    }

    // toggle eye-close state after @timeMills
    public void toggleEye(long timeMills) {
        this.toggleEyeTime = (timeMills + 9) / PongSurface.TIME_INTERVAL * PongSurface.TIME_INTERVAL;
    }

    public int checkConflictBall() {
        Ball ball = pong.getBall();
        int direction = ball.direction;

        Picture pic = pong.getSvgEyeClosed().getPicture();
        Point size = new Point((int) (pic.getHeight() * pong.getScale()), (int) (pic.getWidth() * pong.getScale()));

        if (ball.y > y + size.y / 2 || ball.y < y - size.y / 2 ||
            ball.x > x + size.x / 2 || ball.x < x - size.x / 2)
            return direction;

        setClosed(false);
        toggleEye(300);


        if (ball.x > x) {
//            ball.x = x + size.x / 2;
            direction = (int) (180.0f * 2 * (ball.y - y) / (float) (size.y * 5 / 2));
        } else {
//            ball.x = x - size.x / 2;
            direction = (int) (180.0f * 2 * (y - ball.y) / (float) (size.y * 5 / 2)) + 180;
        }

        return direction;
    }


    // rotate eye image by angle in degrees
    float progressOfrotate;
    float rotateSpeed;
    public void rotateEye(float angle, float speed) {
        this.rotateAngle = angle;
        progressOfrotate = 0;
        rotateSpeed = speed;
    }


    Random rand = new Random();
    public boolean updateFrame() {

        if (toggleEyeTime > 0) {
            toggleEyeTime -= PongSurface.TIME_INTERVAL;

            if (toggleEyeTime <= 0) {
                isClosed = !isClosed;
                toggleEyeTime = 0;
                return true;
            }
        }

        if (rotateAngle != 0) {
            progressOfrotate = Math.min(progressOfrotate + rotateSpeed, Math.abs(rotateAngle));
            if (progressOfrotate == Math.abs(rotateAngle)) {
                angle = (int)(angle + rotateAngle) % 360;
                rotateAngle = 0;
                progressOfrotate = 0;
                return true;
            }
        }

        if (steps > 0) {
            curStep++;

            if (curStep == steps) {
                steps = 0;
                curStep = 0;

                this.x += offX;
                this.y += offY;

                return true;
            }
        }


//        if (pong.getGameState() == Pong.GAME_STATE_PLAYING && pong.getGameSubState() == Pong.GAME_SUB_STATE_0 && isBlue) { // is Bot?
//            double speed = pong.getPercentalSize(1, false);
//
//            double percentage = 50;
//
//            if (rand.nextDouble() * 100 < percentage) {
//                if (Math.abs(y - pong.getBall().y) > speed * 2)
//                if (y > pong.getBall().y)
//                    setY((int) (y - speed));
//                else if (y < pong.getBall().y)
//                    setY((int) (y + speed));
//            }
//        }


        return false;
    }

    public void draw(Canvas canvas, int offsetX, int offsetY) {
        SVG svg;

        if (isClosed)
            svg = pong.getSvgEyeClosed();
        else if (isBlue)
            svg = pong.getSvgEyeBlue();
        else
            svg = pong.getSvgEyeGreen();

        int x = this.x + offsetX,
            y = this.y + offsetY,
            angle = this.angle + (int)((rotateAngle>0)?progressOfrotate:-progressOfrotate);
        if (steps > 0 && curStep >= 0) {
            x += offX * curStep / steps;
            y += offY * curStep / steps;
        }

        if (Utils.isPortrait()) {
            int tmp = x;
            x = canvas.getWidth() - y;
            y = tmp;

            angle += 90;
        }
        Utils.drawSVG(canvas, svg, x, y, pong.getScale(), angle);
    }
}
