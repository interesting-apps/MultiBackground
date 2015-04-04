package com.apps.interestingapps.multibackground.animation;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;
import android.view.SurfaceHolder;

public class CircleWithIncreasingRadiusAnimation implements BitmapAnimation {

	private static final String TAG = "CircleWithIncreasingRadiusAnimation";
	private float maxRadius;
	private int circleCenterX, circleCenterY;

	public CircleWithIncreasingRadiusAnimation(float maxRadius,
			int screenX,
			int screenY) {
		this.maxRadius = maxRadius;
		this.circleCenterX = screenX / 2;
		this.circleCenterY = screenY / 2;
	}

	public void draw(SurfaceHolder surfaceHolder,
			Bitmap oldBitmap,
			Bitmap nextBitmap) {
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

		if (maxRadius <= 0) {
			Log.d(TAG,
					"Max radius not set. Call the method to set animation params.");
			return;
		}

		Paint p1 = new Paint(Paint.FILTER_BITMAP_FLAG);
		p1.setShader(oldBitmapShader);
		int totalTimeInMillis = 500, timeTakenInMillis = 0;
		long timeDiffPreviousFrame = 0, startTimeForFrame = 0;
		float radius = 1.0f;
		Paint p2 = new Paint(Paint.FILTER_BITMAP_FLAG);
		p2.setShader(nextBitmapShader);
		Canvas canvas = null;
		while (timeTakenInMillis < totalTimeInMillis) {
			startTimeForFrame = System.currentTimeMillis();
			radius = maxRadius * timeTakenInMillis / totalTimeInMillis;
			canvas = surfaceHolder.lockCanvas();
			canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
			canvas.drawPaint(p1);
			canvas.drawCircle(circleCenterX, circleCenterY, radius, p2);
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
