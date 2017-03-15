package com.pong.components;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.pong.engine.Pong;


public final class PongSurface extends SurfaceView implements Callback{
	public static final int TIME_INTERVAL = 10;

    public final class DrawThread extends Thread {
		private boolean mRun = true;
		private boolean mPause = false;

		@Override
		public void run() {
            waitForPong();

			final SurfaceHolder surfaceHolder = getHolder();
			Canvas canvas = null;

			while (mRun) {
				try {
					while (mRun && mPause) {
						Thread.sleep(100);
					}

					canvas = surfaceHolder.lockCanvas();
					Utils.checkNoAction();

					if (canvas == null) {
						break;
					}

					synchronized (surfaceHolder) {
                        // background
                        canvas.drawARGB(255, 255, 255, 255);

                        // pong game status
						pong.updateFrame();
						pong.draw(canvas);
					}

					Thread.sleep(10);
				} catch (InterruptedException e) {
				} finally {
					if (canvas != null) {
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
		public void stopDrawing() {
			mRun = false;
		}
	}

    private Pong pong = null;
	private DrawThread drawThread;


	public PongSurface(Context context, AttributeSet attributes) {
		super(context, attributes);

		getHolder().addCallback(this);
		setFocusable(true);
	}

    public void setPong(Pong pong) {
        this.pong = pong;
    }

    private void waitForPong() {
        while (pong == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

	public DrawThread getDrawThread() {
		if (drawThread == null) {
			drawThread = new DrawThread();
		}
		return drawThread;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		getDrawThread().start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		getDrawThread().stopDrawing();
		while (true) {
			try {
				getDrawThread().join();
				break;
			} catch (InterruptedException e) {
			}
		}
		drawThread = null;
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){
			case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_MOVE:
				for (int count = event.getPointerCount() - 1; count >= 0; count--) {
					int id = event.getPointerId(count);
					int x = (int) event.getX(event.findPointerIndex(id));
				int y = (int) event.getY(event.findPointerIndex(id));
					pong.setEyePos(x, y);
				}
				break;
        }
        return true;
    }
}
