package com.apps.interestingapps.multibackground.listeners;

import java.util.List;

import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.apps.interestingapps.multibackground.AnimateTransitionActivity;
import com.apps.interestingapps.multibackground.animation.AnimationUtilities;

public class AnimationDemoImageViewTouchListener implements OnTouchListener {

	private static final String TAG = "AnimationDemoImageViewTouchListener";
	private float upX = 0, downX = 0, actualDistanceX = 0;
	private static final int MIN_SWIPE_DISTANCE = 50;
	private SurfaceView surfaceView;
	private List<Bitmap> demoBitmaps;
	private int currentSelectedBitmapIndex = 0;
	private int previousSelectedBitmapIndex = 2;
	private int screenX, screenY;
	private AnimateTransitionActivity animateTransitionActivity;
	private boolean removeBackground = true;

	public AnimationDemoImageViewTouchListener(AnimateTransitionActivity animateTransitionActivity,
			List<Bitmap> demoBitmaps,
			int screenX,
			int screenY) {
		this.demoBitmaps = demoBitmaps;
		this.screenX = screenX;
		this.screenY = screenY;
		this.animateTransitionActivity = animateTransitionActivity;
	}

	public boolean onTouch(View v, MotionEvent event) {
		surfaceView = (SurfaceView) v;
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			upX = event.getX();
			actualDistanceX = upX - downX;
			previousSelectedBitmapIndex = currentSelectedBitmapIndex;
			float absoluteDistance = Math.abs(actualDistanceX);
			if (absoluteDistance > MIN_SWIPE_DISTANCE) {
				if (actualDistanceX > 0) {
					// Left to right swipe
					if (currentSelectedBitmapIndex == 0) {
						currentSelectedBitmapIndex = demoBitmaps.size();
					}
					currentSelectedBitmapIndex = (currentSelectedBitmapIndex - 1)
							% demoBitmaps.size();
				} else if (actualDistanceX < 0) {
					// Right to left swipe
					currentSelectedBitmapIndex = (currentSelectedBitmapIndex + 1)
							% demoBitmaps.size();
				}
				animateTransitionActivity
						.setCurrentSelectedBitmapIndex(currentSelectedBitmapIndex);
				if (removeBackground) {
					removeBackground = false;
					surfaceView.setBackgroundResource(0);
				}
				boolean drawnWithAnimation = AnimationUtilities
						.animateTransition(surfaceView.getHolder(),
								animateTransitionActivity
										.getCurrentSelectedAnimationDetails()
										.getAnimationType()
										.getAnimationTypeName(), demoBitmaps
										.get(previousSelectedBitmapIndex),
								demoBitmaps.get(currentSelectedBitmapIndex),
								screenX, screenY);
				if (!drawnWithAnimation) {
					AnimationUtilities.drawBitmapWithoutAnimation(demoBitmaps
							.get(currentSelectedBitmapIndex), surfaceView
							.getHolder(), screenX, screenY);
				}
				return true;
			}
			break;
		case MotionEvent.ACTION_DOWN:
			downX = event.getX();
			return true;
		}
		return false;
	}
}
