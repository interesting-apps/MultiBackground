package com.apps.interestingapps.multibackground.animation;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;
import android.view.SurfaceHolder;

public class RotatingCanvasAnimation implements BitmapAnimation {

	private static final String TAG = "RotatingCanvasAnimation";
	private int screenX, screenY;

	public RotatingCanvasAnimation(int screenX, int screenY) {
		this.screenX = screenX;
		this.screenY = screenY;
	}

	public void draw(SurfaceHolder surfaceHolder,
			Bitmap oldBitmap,
			Bitmap nextBitmap) {
		final float MAX_DEGREE = 90f;
		if (oldBitmap == null) {
			Log.d(TAG, "Old bitmap is null.");
			return;
		}

		if (nextBitmap == null) {
			Log.d(TAG, "Next Bitmap is null.");
			return;
		}

		BitmapShader nextBitmapShader = new BitmapShader(nextBitmap,
				Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
		BitmapShader oldBitmapShader = new BitmapShader(oldBitmap,
				Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

		if (screenX <= 0 || screenY <= 0) {
			Log.d(TAG,
					"Screen X or Screen Y not set. Call the method to set animation params.");
			return;
		}

		Paint p1 = new Paint(Paint.FILTER_BITMAP_FLAG);
		p1.setShader(oldBitmapShader);
		int totalTimeInMillis = 500, timeTakenInMillis = 0;
		long timeDiffPreviousFrame = 0, startTimeForFrame = 0;
		Paint p2 = new Paint(Paint.FILTER_BITMAP_FLAG);
		p2.setShader(nextBitmapShader);
		Canvas canvas = null;
		int screenMidX = screenX / 2, screenMidY = screenY / 2;
		float currentDegree = 1f;
		while (timeTakenInMillis < totalTimeInMillis
				&& currentDegree <= MAX_DEGREE) {
			startTimeForFrame = System.currentTimeMillis();
			canvas = surfaceHolder.lockCanvas();
			currentDegree = MAX_DEGREE * timeTakenInMillis / totalTimeInMillis;

			canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
			canvas.drawPaint(p2);
			canvas.save();
			canvas.rotate(currentDegree, screenMidX, screenMidY);
			canvas.drawPaint(p1);
			canvas.restore();

			surfaceHolder.unlockCanvasAndPost(canvas);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timeDiffPreviousFrame = System.currentTimeMillis()
					- startTimeForFrame;
			timeTakenInMillis += timeDiffPreviousFrame;
		}
		canvas = surfaceHolder.lockCanvas();
		canvas.drawPaint(p2);
		surfaceHolder.unlockCanvasAndPost(canvas);
	}
}
