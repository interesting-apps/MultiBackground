package com.apps.interestingapps.multibackground.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class AnimationUtilities {

	public static boolean animateTransition(SurfaceHolder holder,
			String selectedAnimation,
			Bitmap oldBitmap,
			Bitmap nextBitmap,
			int screenX,
			int screenY) {
		BitmapAnimation animation = null;
		switch (AnimationType.fromString(selectedAnimation)) {
		case CIRCLE_WITH_INCREASING_RADIUS:
			animation = new CircleWithIncreasingRadiusAnimation(screenX / 2,
					screenX, screenY);
			break;
		case SQUARE_WITH_INCREASING_DIMENSIONS:
			animation = new SquareWithIncreasingDimensionsAnimation(screenX,
					screenY);
			break;
		case ROTATING_CANVAS:
			animation = new RotatingCanvasAnimation(screenX, screenY);
			break;
		case INWARD_TWO_WAY_TRIANGLE:
			animation = new InwardTwoWayTriangleAnimation(screenX, screenY);
			break;
		case ALTERNATE_TILES_SHOWING_FROM_TOP_TO_BOTTOM:
			animation = new AlternateTilesShowingFromTopToBottomAnimation(
					screenX, screenY);
			break;
		case FULL_PAGE_FOLDING:
			animation = new FullPageFoldingAnimation(screenX, screenY);
		case NONE:
			// Do nothing
		default:
			// Do nothing
			break;
		}
		if (animation != null) {
			Canvas canvas = holder.lockCanvas();
			canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
			holder.unlockCanvasAndPost(canvas);
			animation.draw(holder, oldBitmap, nextBitmap);
			return true;
		}
		return false;
	}

	public static void drawBitmapWithoutAnimation(Bitmap bitmap,
			SurfaceHolder holder,
			int screenX,
			int screenY) {
		int wallpaperX = screenX / 2 - bitmap.getWidth() / 2;
		int wallpaperY = screenY / 2 - bitmap.getHeight() / 2;
		Canvas canvas = holder.lockCanvas();
		canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
		canvas.drawBitmap(bitmap, wallpaperX > 0 ? wallpaperX : 0,
				wallpaperY > 0 ? wallpaperY : 0, null);
		holder.unlockCanvasAndPost(canvas);
	}
}
