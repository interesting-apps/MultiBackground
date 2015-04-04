package com.apps.interestingapps.multibackground.animation;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;
import android.view.SurfaceHolder;

public class FullPageFoldingAnimation implements BitmapAnimation {

	private static final String TAG = "FullPageFoldingAnimation";
	private int screenX, screenY;
	private final int TOTAL_SQUARES_PER_ROW = 6;

	public FullPageFoldingAnimation(int screenX, int screenY) {
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

		Paint p1 = new Paint(Paint.FILTER_BITMAP_FLAG);
		p1.setShader(oldBitmapShader);
		int totalTimeInMillis = 2000, timeTakenInMillis = 0;
		long timeDiffPreviousFrame = 0, startTimeForFrame = 0;

		Paint p2 = new Paint(Paint.FILTER_BITMAP_FLAG);
		p2.setShader(nextBitmapShader);
		Canvas canvas = null;

		int horizontalLineOfpage = screenX / 20;
		int screenOneTenth = screenX / 20;
		int currentPageStartX = screenX - horizontalLineOfpage;
		int pageStartTopY = 0, pageStartBottomY = screenY - 50;
		float maxRadius = 3.5f * screenOneTenth;
		float currentRadius = 1;

		Paint pathPaint = new Paint();
		pathPaint.setColor(Color.WHITE);
		pathPaint.setStrokeWidth(5);
		pathPaint.setAntiAlias(true);
		pathPaint.setStrokeCap(Paint.Cap.ROUND);
		pathPaint.setStrokeJoin(Paint.Join.ROUND);
		pathPaint.setStyle(Paint.Style.FILL);
		// pathPaint.setShadowLayer(7, 0, 0, Color.RED);

		Paint pathLeftBorder = new Paint();
		pathLeftBorder.setColor(Color.BLACK);
		pathLeftBorder.setStrokeWidth(3);
		pathLeftBorder.setAntiAlias(true);
		pathLeftBorder.setStrokeCap(Paint.Cap.ROUND);
		pathLeftBorder.setStrokeJoin(Paint.Join.ROUND);
		pathLeftBorder.setStyle(Paint.Style.STROKE);
		// pathLeftBorder.setShadowLayer(7, 0, 0, Color.CYAN);

		while (timeTakenInMillis < totalTimeInMillis) {
			startTimeForFrame = System.currentTimeMillis();

			currentPageStartX = screenX - (screenX * timeTakenInMillis)
					/ totalTimeInMillis;
			currentRadius = (maxRadius * timeTakenInMillis) / totalTimeInMillis;
			if (currentPageStartX < 0) {
				break;
			}

			Path p = new Path();
			int oneTenthFromCurrentX = currentPageStartX + screenOneTenth;

			// p.moveTo(oneTenthFromCurrentX, pageStartBottomY);
			p.moveTo(currentPageStartX, pageStartBottomY);
			// Draw the bottom horizontal line of the page
			// p.lineTo(currentPageStartX, pageStartBottomY);
			// Draw the vertical line to mark start of page
			p.lineTo(currentPageStartX, pageStartTopY);
			// Draw the top horizontal line of the page
			// p.lineTo(oneTenthFromCurrentX, pageStartTopY);
			// Draw the top quarter circle
			// RectF topArcRectangle = new RectF(oneTenthFromCurrentX,
			// pageStartTopY, oneTenthFromCurrentX + currentRadius,
			// pageStartTopY + currentRadius);
			RectF topArcRectangle = new RectF(currentPageStartX, pageStartTopY,
					currentPageStartX + currentRadius,
					pageStartTopY + currentRadius);
			p.arcTo(topArcRectangle, -90, 90);

			// Draw the vertical line to mark end of page
			// p.lineTo(oneTenthFromCurrentX + currentRadius, pageStartBottomY
			// - currentRadius);
			p.lineTo(currentPageStartX + currentRadius, pageStartBottomY
					- currentRadius);

			// RectF bottomArcRectangle = new RectF(oneTenthFromCurrentX,
			// pageStartBottomY - currentRadius, oneTenthFromCurrentX
			// + currentRadius, pageStartBottomY);
			RectF bottomArcRectangle = new RectF(currentPageStartX,
					pageStartBottomY - currentRadius, currentPageStartX
							+ currentRadius, pageStartBottomY);
			Path path2 = new Path();
			// path2.moveTo(oneTenthFromCurrentX, pageStartBottomY);
			path2.moveTo(currentPageStartX, pageStartBottomY);
			path2.arcTo(bottomArcRectangle, 90, -90);
			Path path3 = new Path();
			path3.moveTo(currentPageStartX, pageStartBottomY);
			path3.lineTo(currentPageStartX, pageStartTopY);
			// path2.lineTo(oneTenthFromCurrentX + currentRadius, pageStartTopY
			// + currentRadius);
			path2.lineTo(currentPageStartX + currentRadius, pageStartTopY
					+ currentRadius);

			canvas = surfaceHolder.lockCanvas();
			canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

			canvas.drawPaint(p1);
			RectF secondPicRect = new RectF(currentPageStartX, 0, screenX,
					screenY);
			canvas.drawRect(secondPicRect, p2);
			canvas.drawPath(p, pathPaint);
			canvas.drawPath(path2, pathPaint);
			canvas.drawPath(path3, pathLeftBorder);

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
