package com.pong.engine;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.view.View;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.pong.MainActivity;
import com.pong.components.PongSurface;
import com.pong.components.Utils;

import java.io.IOException;
import java.util.ArrayList;


public class Pong {
    public static final int GAME_STATE_NONE = -1;
    public static final int GAME_STATE_INTRODUCTION = 0;
    public static final int GAME_STATE_PLAYING = 1;
    public static final int GAME_STATE_WINNER = 2;

    public static final int GAME_SUB_STATE_0 = 0;
    public static final int GAME_SUB_STATE_1 = 1;
    public static final int GAME_SUB_STATE_2 = 2;

    public static final int GAME_SCORE_LIMIT = 5;
    public static final int GAME_NOTOUCH_SCORE_LIMIT = 2;

    SVG svgEyeBlue, svgEyeGreen, svgEyeClosed;
    SVG svgTearBlue, svgTearGreen;
    SVG svgBall;
    float scale;
    Point canvasSize;
    int eyeOffset;

    OnStateChangeListener mStateChangeListener = null;

    private boolean isDoubleSpeed, isPaused;

    public Pong(OnStateChangeListener onStateChangeListener) {
        state = GAME_STATE_NONE;
        isDoubleSpeed = true;
        isPaused = false;
        mStateChangeListener = onStateChangeListener;

        try {
            canvasSize = new Point((int) Utils.getPercentalSize(90, true), (int) Utils.getPercentalSize(85, false));
            eyeOffset = (int) getPercentalSize(10, true);

            svgEyeBlue = SVGParser.getSVGFromAsset(Utils.getActivity().getAssets(), "eye_blue.svg");
            svgEyeGreen = SVGParser.getSVGFromAsset(Utils.getActivity().getAssets(), "eye_green.svg");
            svgEyeClosed = SVGParser.getSVGFromAsset(Utils.getActivity().getAssets(), "eye_closed.svg");
            svgTearBlue = SVGParser.getSVGFromAsset(Utils.getActivity().getAssets(), "tear_blue.svg");
            svgTearGreen = SVGParser.getSVGFromAsset(Utils.getActivity().getAssets(), "tear_green.svg");
            svgBall = SVGParser.getSVGFromAsset(Utils.getActivity().getAssets(), "ball.svg");

            scale = getPercentalSize(20, false) / (float) svgEyeBlue.getPicture().getWidth();
        } catch (IOException e) {
            // Handle IOException here
        }
    }

    public boolean isDoubleSpeed() {
        return isDoubleSpeed;
    }

    public Pong setDoubleSpeed(boolean isDoubleSpeed) {
        this.isDoubleSpeed = isDoubleSpeed;
        return this;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public Pong setPaused(boolean isPaused) {
        this.isPaused = isPaused;
        return this;
    }

    public SVG getSvgEyeBlue() {
        return svgEyeBlue;
    }

    public SVG getSvgEyeGreen() {
        return svgEyeGreen;
    }

    public SVG getSvgEyeClosed() {
        return svgEyeClosed;
    }

    public SVG getTearBlue() {
        return svgTearBlue;
    }

    public SVG getTearGreen() {
        return svgTearGreen;
    }

    public float getScale() {
        return scale;
    }

    public int getGameState() {
        return this.state;
    }

    public int getGameSubState() {
        return this.subState;
    }

    public Ball getBall() {
        return ball;
    }

    public float getPercentalSize(float percent, boolean isOfLongEdge) {
        int srcSize = isOfLongEdge ? Math.max(canvasSize.x, canvasSize.y) : Math.min(canvasSize.x, canvasSize.y);
        return percent * srcSize / 100.0f;
    }


    int state, subState;
    int non_touch_score, scoreBlue, scoreGreen;
    Eye eyeBlue, eyeGreen;
    ArrayList<Tear> tearList = new ArrayList<Tear>();
    Ball ball;

    public void setGameState(int state) {
        if (mStateChangeListener != null) {
            mStateChangeListener.onStateChanged(this.state, state);
        }

        switch (state) {
            case GAME_STATE_INTRODUCTION:
                eyeBlue = new Eye(this, true,
                        (int) getPercentalSize(40, true),
                        (int) getPercentalSize(37, false));
                eyeGreen = new Eye(this, false,
                        (int) getPercentalSize(60, true),
                        (int) getPercentalSize(37, false));
                tearList.clear();
                ball = new Ball(this,
                        (int) getPercentalSize(50, true),
                        (int) getPercentalSize(62, false));
                break;
            case GAME_STATE_PLAYING:
                Utils.setLastActionTime();
                scoreBlue = scoreGreen = non_touch_score = 0;
                eyeBlue.setY((int) getPercentalSize(50, false));
                eyeGreen.setY((int) getPercentalSize(50, false));
                break;
            case GAME_STATE_WINNER:
                eyeBlue.setY((int) getPercentalSize(50, false));
                eyeGreen.setY((int) getPercentalSize(50, false));
                break;
        }

        this.state = state;

        setGameSubState(GAME_SUB_STATE_0);
    }


    public void setGameSubState(int subState) {

        if (state == GAME_STATE_INTRODUCTION) {
            switch (subState) {
                case GAME_SUB_STATE_0:
                    timeDelay = 100;
                    break;
                case GAME_SUB_STATE_1:
                    timeDelay = 500;
                    break;
                case GAME_SUB_STATE_2:
                    ball.setXY((int) getPercentalSize(50, true), (int) getPercentalSize(50, false));
                    timeDelay = 500;
                    break;
            }
        } else if (state == GAME_STATE_PLAYING) {
            switch (subState) {
                case GAME_SUB_STATE_0:
                    eyeBlue.setY((int) getPercentalSize(50, false));
                    eyeGreen.setY((int) getPercentalSize(50, false));
                    ball.setXY((int) getPercentalSize(50, true), (int) getPercentalSize(50, false));
                    ball.setRound(scoreBlue + scoreGreen);
                    timeDelay = 1000;
                    break;
                case GAME_SUB_STATE_1:
                    timeDelay = 10;
                    break;
            }
        } else if (state == GAME_STATE_WINNER) {
            switch (subState) {
                case GAME_SUB_STATE_0:
                    timeDelay = 200;
                    break;
                case GAME_SUB_STATE_1:
                    timeDelay = 200;
                    break;
                case GAME_SUB_STATE_2:
                    timeDelay = 200;
                    break;
            }
        }

        this.subState = subState;

        frameNo = 0;
    }

    long timeDelay = 0;
    long frameNo = 0;
    boolean isBlueWin = false;
    public void updateFrame() {
        if (isPaused)
            return;

        for (int i = tearList.size() - 1; i >= 0; i--) {
            if (!tearList.get(i).updateFrame())
                tearList.remove(i);
        }

        if (timeDelay > 0) {
            timeDelay -= PongSurface.TIME_INTERVAL;

            if (timeDelay <= 0) {
                timeDelay = 0;

                if (state == GAME_STATE_INTRODUCTION) {
                    switch (subState) {
                        case GAME_SUB_STATE_1:                          // move to each position
                            eyeBlue.moveEye(eyeOffset, (int) getPercentalSize(50, false));
                            eyeGreen.moveEye(canvasSize.x - eyeOffset, (int) getPercentalSize(50, false));
                            break;
                        case GAME_SUB_STATE_2:                          // rotating
                            eyeBlue.rotateEye(90, 3);
                            eyeGreen.rotateEye(-90, 3);
                            break;
                    }
                } else if (state == GAME_STATE_PLAYING) {
                    switch (subState) {
                        case GAME_SUB_STATE_0:
                            ball.startPlaying(isBlueWin);
                            break;
                    }
                } else if (state == GAME_STATE_WINNER) {
                    switch (subState) {
                        case GAME_SUB_STATE_0:
                            break;
                        case GAME_SUB_STATE_1:
                            if (isBlueWin)
                                eyeBlue.rotateEye(360 * 4, 10);
                            else
                                eyeGreen.rotateEye(360 * 4, 10);
                            break;
                        case GAME_SUB_STATE_2:
                            break;
                    }
                }
            }
            return;
        }

        frameNo ++;

        if (state == GAME_STATE_INTRODUCTION) {
            switch (subState) {
                case GAME_SUB_STATE_0:                          // blinking
                    if (frameNo > 75) {
                        setGameSubState(GAME_SUB_STATE_1);
                        return;
                    } else {
                        eyeBlue.setClosed(frameNo % 15 < 7);
                        eyeGreen.setClosed(eyeBlue.getClosed());
                    }
                    break;
            }
        } else if (state == GAME_STATE_PLAYING) {
            switch (subState) {
                case GAME_SUB_STATE_1:
                    if (frameNo > 160) {
                        if (non_touch_score == GAME_NOTOUCH_SCORE_LIMIT) {
                            ((MainActivity) Utils.getActivity()).onHome(null);
                            return;
                        }

                        if (scoreBlue == GAME_SCORE_LIMIT || scoreGreen == GAME_SCORE_LIMIT)
                        {
                            setGameState(GAME_STATE_WINNER);
                            return;
                        }

                        setGameSubState(GAME_SUB_STATE_0);
                        return;
                    }
                    if (frameNo <= 60) {    //blinking
                        if (isBlueWin) {
                            eyeBlue.setClosed(frameNo % 15 < 7);
                        } else {
                            eyeGreen.setClosed(frameNo % 15 < 7);
                        }
                    } else if (frameNo == 61) {                // 1st tearing
                        int x, y;
                        if (isBlueWin) {
                            x = eyeGreen.getX();
                            y = eyeGreen.getY();
                        } else {
                            x = eyeBlue.getX() - (int) getPercentalSize(3, true) * 2;
                            y = eyeBlue.getY();
                        }

                        tearList.add(new Tear(this, !isBlueWin, x, y - (int) getPercentalSize(3, false)));
                    } else if (frameNo == 110) {                // 2nd tearing
                        int x, y;
                        if (isBlueWin) {
                            x = eyeGreen.getX();
                            y = eyeGreen.getY();
                        } else {
                            x = eyeBlue.getX() - (int) getPercentalSize(3, true) * 2;
                            y = eyeBlue.getY();
                        }
                        tearList.add(new Tear(this, !isBlueWin, x + (int) getPercentalSize(3, true), y + (int) getPercentalSize(3, false)));
                    }
                    break;
            }
        } else if (state == GAME_STATE_WINNER) {
            switch (subState) {
                case GAME_SUB_STATE_0:      //blinking
                    if (frameNo > 70) {
                        setGameSubState(GAME_SUB_STATE_1);
                        return;
                    } else {
                        if (isBlueWin)
                            eyeBlue.setClosed(frameNo % 15 < 7);
                        else
                            eyeGreen.setClosed(frameNo % 15 < 7);
                    }
                    break;
                case GAME_SUB_STATE_1:      //rotating
                    break;
                case GAME_SUB_STATE_2:      //blinking
                    if (frameNo > 60) {
                        setGameState(GAME_STATE_PLAYING);
                        return;
                    } else {
                        if (isBlueWin)
                            eyeBlue.setClosed(frameNo % 15 < 7);
                        else
                            eyeGreen.setClosed(frameNo % 15 < 7);
                    }
                    break;
            }
        }

        boolean greenUpdate = eyeGreen.updateFrame();
        boolean blueUpdate = eyeBlue.updateFrame();
        if (greenUpdate || blueUpdate) {  // action finished
            if (state == GAME_STATE_INTRODUCTION) {
                if (subState == GAME_SUB_STATE_1) { // moving
                    setGameSubState(GAME_SUB_STATE_2);
                    return;
                } else if (subState == GAME_SUB_STATE_2) { // rotating
                    setGameState(GAME_STATE_PLAYING);
                    return;
                }
            } else if (state == GAME_STATE_WINNER) {
                if (subState == GAME_SUB_STATE_1) {   // rotating
                    setGameSubState(GAME_SUB_STATE_2);
                    return;
                }
            }
        }

        if (state == GAME_STATE_PLAYING && subState == GAME_SUB_STATE_0) {
            int ret = ball.updateFrame(eyeBlue, eyeGreen);
            if (ret != 0) {
                if (ret == -1) {
                    scoreGreen++;
                    isBlueWin = false;
                } else {
                    scoreBlue++;
                    isBlueWin = true;
                }
                setGameSubState(GAME_SUB_STATE_1);

                non_touch_score++;
            }
        }
    }

    public void draw(Canvas canvas) {
        if (getGameState() == GAME_STATE_NONE)
            return;

        int canvasWidth = Math.max(canvas.getWidth(), canvas.getHeight());
        int canvasHeight = Math.min(canvas.getWidth(), canvas.getHeight());
        int offsetX = (canvasWidth - canvasSize.x) / 2;
        int offsetY = (canvasHeight - canvasSize.y) / 2;


        // testcode start
//        Paint paint = new Paint();
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setColor(Color.GRAY);
//        int x = offsetX, y = offsetY, width = canvasSize.x, height = canvasSize.y;
//        float round = getPercentalSize(5, false);
//        if (Utils.isPortrait()) {
//            x = offsetY;
//            y = offsetX;
//            width = canvasSize.y;
//            height = canvasSize.x;
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            canvas.drawRoundRect(x, y, x + width, y + height, round, round, paint);
//        }
        // testcode end

        if (state != GAME_STATE_INTRODUCTION)
            drawScore(canvas, offsetX, offsetY);

        for (int i = 0; i < tearList.size(); i++)
            tearList.get(i).draw(canvas, offsetX, offsetY);

        if ((state == GAME_STATE_INTRODUCTION && subState != GAME_SUB_STATE_1) ||
            state == GAME_STATE_PLAYING)
            ball.draw(canvas, offsetX, offsetY);

        eyeBlue.draw(canvas, offsetX, offsetY);
        eyeGreen.draw(canvas, offsetX, offsetY);
    }

    void drawScore(Canvas canvas, int offX, int offY) {
        Paint paint = new Paint();
        paint.setTypeface(Typeface.create("Serif", Typeface.NORMAL));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(getPercentalSize(4, true));
        paint.setAntiAlias(true);


        int x, y;
        int angle;
        if (Utils.isPortrait()) {
            x = offY;//canvas.getWidth() / 2;
            y = offX;
            angle = 0;
            paint.setTextAlign(Paint.Align.LEFT);
        } else {
            x = 0;
            y = canvasSize.y + offY; //canvas.getHeight() / 2;
            angle = 90;
            paint.setTextAlign(Paint.Align.RIGHT);
        }
        Utils.drawRotatedText(canvas, String.format("%d", scoreBlue), angle, x, y, paint);

        if (Utils.isPortrait()) {
            x = canvasSize.y + offY; //canvas.getWidth() / 2;
            y = canvasSize.x + offX; //canvas.getHeight() - offX;
            angle = 0;
        } else {
            x = canvas.getWidth();
            y = offY; //canvas.getHeight() / 2;
            angle = -90;
        }
        paint.setTextAlign(Paint.Align.RIGHT);
        Utils.drawRotatedText(canvas, String.format("%d", scoreGreen), angle, x, y, paint);
    }

    public void setEyePos(int x, int y) {
        if (state != GAME_STATE_PLAYING || isPaused)
            return;

        non_touch_score = -1;
        if (Utils.isPortrait()) {
            int off = (int) getPercentalSize(5, true);
            x = (int) ((Utils.getPercentalSize(100, false) - x) - (Utils.getPercentalSize(100, false) - canvasSize.y) / 2);
            if (y > eyeGreen.getX() - off) {
                eyeGreen.setY(x);
            } else if (y < eyeBlue.getX() + off * 3) {
                eyeBlue.setY(x);
            }
        } else {
            int off = (int) getPercentalSize(5, true);
            y -= (Utils.getContentViewHeight() - canvasSize.y) / 2;
            if (x > eyeGreen.getX() - off) {
                eyeGreen.setY(y);
            } else if (x < eyeBlue.getX() + off * 3)
                eyeBlue.setY(y);
        }

        Utils.setLastActionTime();
    }

    public interface OnStateChangeListener {
        public void onStateChanged(int oldState, int newState);
    }
}
