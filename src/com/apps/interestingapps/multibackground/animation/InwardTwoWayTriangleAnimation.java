package com.apps.interestingapps.multibackground.animation;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Canvas.VertexMode;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;
import android.view.SurfaceHolder;

public class InwardTwoWayTriangleAnimation implements BitmapAnimation {

	private static final String TAG = "InwardTwoWayTriangleAnimation";
	private int screenX, screenY;

	public InwardTwoWayTriangleAnimation(int screenX, int screenY) {
		this.screenX = screenX;
		this.screenY = screenY;
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

		if (screenX <= 0 || screenY <= 0) {
			Log.d(TAG,
					"Screen X or Screen Y not set. Call the method to set animation params.");
			return;
		}

		float[] bottomTriangleVertices = new float[] { screenX, screenY,
				screenX, screenY, screenX, screenY };
		float[] topTriangleVertices = new float[] { 0, 0, 0, 0, 0, 0 };
		Paint p1 = new Paint(Paint.FILTER_BITMAP_FLAG);
		p1.setShader(oldBitmapShader);
		int totalTimeInMillis = 1000, timeTakenInMillis = 0;
		long timeDiffPreviousFrame = 0, startTimeForFrame = 0;
		Paint p2 = new Paint(Paint.FILTER_BITMAP_FLAG);
		p2.setShader(nextBitmapShader);
		Canvas canvas = null;
		int currentX = 0, currentY = 0;
		while (timeTakenInMillis < totalTimeInMillis && currentX <= screenX
				&& currentY <= screenY) {
			startTimeForFrame = System.currentTimeMillis();
			canvas = surfaceHolder.lockCanvas();
			currentX = screenX * timeTakenInMillis / totalTimeInMillis;
			currentY = screenY * timeTakenInMillis / totalTimeInMillis;

			canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
			canvas.drawPaint(p1);
			if (currentX < 0 || currentY < 0) {
				surfaceHolder.unlockCanvasAndPost(canvas);
				break;
			}
			// Change X co-od left vertex
			bottomTriangleVertices[0] = screenX - currentX;
			topTriangleVertices[0] = currentX;
			// Change Y co-od of top vertex
			bottomTriangleVertices[3] = screenY - currentY;
			topTriangleVertices[3] = currentY;
			canvas.drawVertices(VertexMode.TRIANGLE_FAN, 6,
					bottomTriangleVertices, 0, bottomTriangleVertices, 0, null,
					0, null, 0, 0, p2);
			canvas.drawVertices(VertexMode.TRIANGLE_FAN, 6,
					topTriangleVertices, 0, topTriangleVertices, 0, null, 0,
					null, 0, 0, p2);
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
