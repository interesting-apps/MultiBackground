package com.apps.interestingapps.multibackground.animation;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;
import android.view.SurfaceHolder;

public class AlternateTilesShowingFromTopToBottomAnimation implements
		BitmapAnimation {

	private static final String TAG = "AlternateTilesShowingFromTopToBottomAnimation";
	private int screenX, screenY;
	private final int TOTAL_SQUARES_PER_ROW = 6;

	public AlternateTilesShowingFromTopToBottomAnimation(int screenX,
			int screenY) {
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
		int totalTimeInMillis = 1000, timeTakenInMillis = 0;
		long timeDiffPreviousFrame = 0, startTimeForFrame = 0;
		Paint p2 = new Paint(Paint.FILTER_BITMAP_FLAG);
		p2.setShader(nextBitmapShader);
		Canvas canvas = null;
		int squareLength = screenX / TOTAL_SQUARES_PER_ROW;
		int maxNumberOfSquares = TOTAL_SQUARES_PER_ROW * (screenY / squareLength);
		int currentSquaresToShow = 1;
		while (timeTakenInMillis < totalTimeInMillis
				&& currentSquaresToShow <= maxNumberOfSquares) {
			startTimeForFrame = System.currentTimeMillis();
			canvas = surfaceHolder.lockCanvas();
			currentSquaresToShow = maxNumberOfSquares * timeTakenInMillis
					/ totalTimeInMillis;
			List<int[]> positionsOfSquares = getPositionsOfSquaresToShow(
					currentSquaresToShow, maxNumberOfSquares, squareLength);
			canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
			canvas.drawPaint(p1);
			for (int[] positionOfSquare : positionsOfSquares) {
				canvas.drawRect(positionOfSquare[0], positionOfSquare[1],
						positionOfSquare[0] + squareLength, positionOfSquare[1]
								+ squareLength, p2);
			}
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

	private List<int[]> getPositionsOfSquaresToShow(int totalSquares,
			int maxNumberOfSquares,
			int squareLength) {
		int i = 0;
		List<int[]> positions = new ArrayList<int[]>();
		int currentSquares = 0;
		while (i <= maxNumberOfSquares && currentSquares <= totalSquares) {
			if (i % 2 != 0) {
				int[] position = new int[2];
				position[0] = (i % 6) * squareLength;
				position[1] = (i / 6) * squareLength;
				positions.add(position);
				currentSquares++;
			}
			i++;
		}
		if (currentSquares == totalSquares) {
			return positions;
		}
		i = 0;
		while (i <= maxNumberOfSquares && currentSquares <= totalSquares) {
			if (i % 2 == 0) {
				int[] position = new int[2];
				position[0] = (i % 6) * squareLength;
				position[1] = (i / 6) * squareLength;
				positions.add(position);
				currentSquares++;
			}
			i++;
		}
		return positions;
	}

}
