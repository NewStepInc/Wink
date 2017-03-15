package com.pong;

import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.pong.engine.Pong;
import com.pong.components.PongSurface;
import com.pong.components.Utils;

public class MainActivity extends AppCompatActivity implements Pong.OnStateChangeListener {
    private static Pong pong = null;
    private PongSurface surface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setActivity(this);
        Utils.setLastActionTime();

        super.onCreate(savedInstanceState);

        Utils.makeFullScreen(this);


        setContentView(R.layout.activity_main);

        findViewById(R.id.game_normalspeed).setOnTouchListener(Utils.onTouchListener);
        findViewById(R.id.game_doublespeed).setOnTouchListener(Utils.onTouchListener);
        findViewById(R.id.game_pause).setOnTouchListener(Utils.onTouchListener);
        findViewById(R.id.game_home).setOnTouchListener(Utils.onTouchListener);

        if (pong == null) {
            pong = new Pong(this);
            pong.setGameState(Pong.GAME_STATE_INTRODUCTION);
        }

        ((ImageView) findViewById(R.id.game_normalspeed)).setColorFilter(pong.isDoubleSpeed() ? 0xFF000000 : 0xFFFF0000, PorterDuff.Mode.SRC_IN);
        ((ImageView) findViewById(R.id.game_doublespeed)).setColorFilter(!pong.isDoubleSpeed() ? 0xFF000000 : 0xFFFF0000, PorterDuff.Mode.SRC_IN);
        ((ImageView) findViewById(R.id.game_pause)).setColorFilter(pong.isPaused() ? 0xFFFF0000 : 0xFF000000, PorterDuff.Mode.SRC_IN);

        surface = (PongSurface) findViewById(R.id.game_surface);
        surface.setPong(pong);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            onHome(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onNormalSpeed(View view) {
        pong.setDoubleSpeed(false);
        Utils.setLastActionTime();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView) findViewById(R.id.game_normalspeed)).setColorFilter(0xFFFF0000, PorterDuff.Mode.SRC_IN);
                ((ImageView) findViewById(R.id.game_doublespeed)).setColorFilter(0xFF000000, PorterDuff.Mode.SRC_IN);
            }
        });
    }

    public void onDoubleSpeed(View view) {
        pong.setDoubleSpeed(true);
        Utils.setLastActionTime();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView) findViewById(R.id.game_normalspeed)).setColorFilter(0xFF000000, PorterDuff.Mode.SRC_IN);
                ((ImageView) findViewById(R.id.game_doublespeed)).setColorFilter(0xFFFF0000, PorterDuff.Mode.SRC_IN);
            }
        });
    }

    public void onPause(View view) {
        pong.setPaused(!pong.isPaused());
        Utils.setLastActionTime();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView) findViewById(R.id.game_pause)).setColorFilter(pong.isPaused() ? 0xFFFF0000 : 0xFF000000, PorterDuff.Mode.SRC_IN);
            }
        });
    }

    public void onHome(View view) {
        pong = null;
        finish();
    }

    @Override
    public void onStateChanged(int oldState, int newState) {
        if (newState == Pong.GAME_STATE_PLAYING) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.game_normalspeed).setVisibility(View.VISIBLE);
                    findViewById(R.id.game_doublespeed).setVisibility(View.VISIBLE);
                    findViewById(R.id.game_pause).setVisibility(View.VISIBLE);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.game_normalspeed).setVisibility(View.INVISIBLE);
                    findViewById(R.id.game_doublespeed).setVisibility(View.INVISIBLE);
                    findViewById(R.id.game_pause).setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public static Pong getPong() {
        return MainActivity.pong;
    }
}
