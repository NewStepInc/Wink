package com.pong.engine;

import android.graphics.Canvas;

import com.larvalabs.svgandroid.SVG;
import com.pong.components.Utils;

public class Tear {
    Pong pong;
    boolean isBlue;
    int x, y;

    public Tear(Pong pong, boolean isBlue, int x, int y) {
        this.pong = pong;
        this.isBlue = isBlue;
        this.x = x;
        this.y = y;
    }

    public boolean updateFrame() {
        if (x < -Utils.getPercentalSize(5, true) || x > pong.getPercentalSize(105, true)) {
            return false;
        }

        int offset = (int) pong.getPercentalSize(0.2f, true);
        x += isBlue ? -offset : offset;
        return true;
    }

    public void draw(Canvas canvas, int offsetX, int offsetY) {
        SVG svg = isBlue ? pong.getTearBlue() : pong.getTearGreen();
        int angle = isBlue ? 90 : -90;
        int x = this.x + offsetX;
        int y = this.y + offsetY;
        if (Utils.isPortrait()) {
            x = canvas.getWidth() - (this.y + offsetY);
            y = this.x + offsetX;
            angle += 90;
        }

        Utils.drawSVG(canvas, svg, x, y, pong.getScale() / 2.5f, angle);
    }
}
