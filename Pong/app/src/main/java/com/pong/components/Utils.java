package com.pong.components;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.larvalabs.svgandroid.SVG;
import com.pong.MainActivity;
import com.pong.engine.Pong;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

public class Utils {

    private static final long NOACTION_LIMIT = 1000 * 60 * 4;  // 4 mins

    private static long lastActionTime;

    public static Activity activity;

    public static void setActivity(Activity activity) {
        Utils.activity = activity;
    }

    public static Activity getActivity() {
        return Utils.activity;
    }

    public static void setLastActionTime() {
        lastActionTime = System.currentTimeMillis();
    }

    public static void checkNoAction() {
        if (activity instanceof MainActivity && System.currentTimeMillis() - lastActionTime > NOACTION_LIMIT) {
            Pong pong = ((MainActivity) activity).getPong();
            if (pong == null)
                return;

            if (pong.isPaused()) { // toggle to play mode
                ((MainActivity) activity).onPause(null);
            } else {
                ((MainActivity) activity).onHome(null);
            }
        }
    }

    public static View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                v.setAlpha(0.3f);
            else if (event.getAction() == MotionEvent.ACTION_UP)
                v.setAlpha(1f);
            return false;
        }
    };


    public static Bitmap loadBitmapFromAssets(String fileName) {
        AssetManager assetManager = activity.getAssets();
        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(fileName);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            Log.e("Pong game", "load bitmap from assets");
        }
        return bitmap;
    }

    public static Bitmap loadScaledBitmap(Bitmap bmp, float scale) {
        if (bmp == null)
            return null;

        int orgWidth = bmp.getWidth();
        int orgHeight = bmp.getHeight();
        int toWidth = (int) (scale * orgWidth);
        int toHeight = (int) (scale * orgHeight);

        return Bitmap.createScaledBitmap(bmp, toWidth, toHeight, true);
    }

    public static void drawBitmap(Canvas canvas, Bitmap bmp, int x, int y, int angle)
    {
        Matrix matrix = new Matrix();
        matrix.postTranslate(-bmp.getWidth() / 2f, -bmp.getHeight() / 2f);
        matrix.postRotate(angle, 0, 0);
        matrix.postTranslate(x, y);
        canvas.drawBitmap(bmp, matrix, null);
    }

    public static void drawSVG(Canvas canvas, SVG svg, int x, int y, float scale, int angle) {
        Matrix matrix = new Matrix();
        Picture picture = svg.getPicture();

        matrix.postTranslate(-picture.getWidth() / 2f, -picture.getHeight() / 2f);
        matrix.postRotate(angle, 0, 0);
        matrix.postScale(scale, scale);
        matrix.postTranslate(x, y);

        canvas.setMatrix(matrix);
        canvas.drawPicture(picture);
        canvas.setMatrix(null);
    }

    private static Point dimen = null;
    public static Point getDimentionalSize()
    {
        if (dimen != null)
            return dimen;

        Display display = activity.getWindowManager().getDefaultDisplay();
        int realWidth;
        int realHeight;

        if (Build.VERSION.SDK_INT >= 17){
            //new pleasant way to get real metrics
            DisplayMetrics realMetrics = new DisplayMetrics();
            display.getRealMetrics(realMetrics);
            realWidth = realMetrics.widthPixels;
            realHeight = realMetrics.heightPixels;

        } else if (Build.VERSION.SDK_INT >= 14) {
            //reflection for this weird in-between time
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
                realHeight = (Integer) mGetRawH.invoke(display);
            } catch (Exception e) {
                //this may not be 100% accurate, but it's all we've got
                realWidth = display.getWidth();
                realHeight = display.getHeight();
                Log.e("Display Info", "Couldn't use reflection to get the real display metrics.");
            }

        } else {
            //This should be close, as lower API devices should not have window navigation bars
            realWidth = display.getWidth();
            realHeight = display.getHeight();
        }

        if (realHeight < realWidth) {
            realWidth += realHeight;
            realHeight = realWidth - realHeight;
            realWidth -= realHeight;
        }

        dimen = new Point(realWidth, realHeight);
        return dimen;
    }


    public static float getPercentalSize(float percent, boolean isOfLongEdge) {

        Point size = getDimentionalSize();
        int srcSize = isOfLongEdge ? Math.max(size.x, size.y) : Math.min(size.x, size.y);
        return percent * srcSize / 100.0f;
    }

    public static int getContentViewHeight() {
        Window window = getActivity().getWindow();
        return window.findViewById(Window.ID_ANDROID_CONTENT).getHeight();
    }

    public static boolean isPortrait() {
        return (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    public static void drawRotatedText(Canvas canvas, String text, int angle, int x, int y, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        float halfTextHeight = bounds.height() / 2;
        if (Utils.isPortrait())
            y += halfTextHeight;
        else
            x += halfTextHeight * (angle / 90);
        canvas.save();
        canvas.rotate(angle, x, y);
        canvas.drawText(text, x, y, paint);
        canvas.restore();
    }

    public static void makeFullScreen(Activity activity) {
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = activity.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);

            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if(Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
