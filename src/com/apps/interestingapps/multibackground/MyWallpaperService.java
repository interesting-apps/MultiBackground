package com.apps.interestingapps.multibackground;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.apps.interestingapps.multibackground.common.MultiBackgroundConstants;

public class MyWallpaperService extends WallpaperService {

	public Engine onCreateEngine() {
		return new MyWallpaperEngine();
	}

	private class MyWallpaperEngine extends Engine {
		private final Handler handler = new Handler();
		private int currentBackground;
		private float downX,  upX;
		private final double THRESHOLD_FOR_SCREEN_CHANGE = 0.41;
		private final double SCREEN_COVERAGE_FACTOR = 0.45;
		private int screenX;
		private boolean changeBackground = false;
		private double screenCoverageRequired ;
		private static final String TAG = "MyWallpaperService";	

		private final Runnable drawRunner = new Runnable() {

			public void run() {
				draw();
			}

		};
		private boolean visible = true;

		public MyWallpaperEngine() {
			changeBackground = true;
			handler.post(drawRunner);
			setTouchEventsEnabled(true);
			currentBackground = R.drawable.green_background;
			downX = 0;
			upX = 0;
			WindowManager wm = (WindowManager) getApplicationContext()
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			screenX = display.getWidth();
			screenCoverageRequired = screenX * SCREEN_COVERAGE_FACTOR;
			Log.i(TAG,"****Constructor called");
		}

		public void onVisibilityChanged(boolean visible) {
			this.visible = visible;
			Log.i(TAG, "Changed the visibility to: " + visible);
			if (visible) {
				changeBackground = false;
				handler.post(drawRunner);
			} else {
				handler.removeCallbacks(drawRunner);
			}
		}

		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			Log.i(TAG, "Destroyed called");
			this.visible = false;
			handler.removeCallbacks(drawRunner);
		}

		public void onSurfaceChanged(SurfaceHolder holder,
				int format,
				int width,
				int height) {
			super.onSurfaceChanged(holder, format, width, height);
		}

		public void onTouchEvent(MotionEvent event) {

			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				Log.i(TAG, "Touch event: " + "Action = up " + " X = "
						+ event.getX() + " upTime = " + event.getEventTime()
						+ " downTime = " + event.getDownTime() + " upTime = "
						+ event.getEventTime());
				upX = event.getX();
				long upTime = event.getEventTime();
				long downTime = event.getDownTime();
				long timeDiff = upTime - downTime;
				float actualDistanceX = upX - downX;
				float absoluteDistanceX = Math.abs(actualDistanceX);
				if (timeDiff != 0 && absoluteDistanceX != 0) {
					double threshold = absoluteDistanceX / timeDiff;
					Log.i(TAG, "Threshold = " + threshold);
					Log.i(TAG, "Distance = " + actualDistanceX + " Time = "
							+ timeDiff + " ScreenX = " + screenX);
					if (absoluteDistanceX >= screenCoverageRequired
							|| threshold >= THRESHOLD_FOR_SCREEN_CHANGE) {
						try {
							/*
							 * TODO: take into consideration :
							 * 
							 * 1. long press can be done before/after down/up
							 * events --> Checked if distance is greater than
							 * half the screen size, it will change the home
							 * screen
							 * 
							 * 2. Current screen can be the last screen in the
							 * direction of movement --> Currently couldn't find
							 * a better way, so changing the feature of the app.
							 * 
							 * 3. Multitouch events
							 */
							changeBackground = true;
							canvas = holder.lockCanvas();
							if (canvas != null) {
								changeBackground(canvas);
							}
						} finally {
							if (canvas != null)
								holder.unlockCanvasAndPost(canvas);
						}
					} else {
						Log.i(TAG, "Too Slow: " + threshold);
					}
				}
				break;
			case MotionEvent.ACTION_DOWN:
				downX = event.getX();
				break;
			}
			super.onTouchEvent(event);
		}

		private void draw() {
			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			try {
				canvas = holder.lockCanvas();
				if (canvas != null) {
					changeBackground(canvas);
				}
			} finally {
				if (canvas != null)
					holder.unlockCanvasAndPost(canvas);
			}
			handler.removeCallbacks(drawRunner);
			if (visible) {
				handler.postDelayed(drawRunner, 50000);
			}
		}

		// Surface view requires that all elements are drawn completely
		private void changeBackground(Canvas canvas) {
			if (changeBackground) {
				Log.i(TAG, "Changing background");
				Resources res = getResources();

				if (currentBackground == R.drawable.green_background
						|| currentBackground < 0) {
					currentBackground = R.drawable.red_background;
				} else {
					currentBackground = R.drawable.green_background;
				}
				changeBackground = false;
				Bitmap backgroundimg = BitmapFactory.decodeResource(res,
						currentBackground);
				canvas.drawBitmap(backgroundimg, 0, 0, null);
			}
		}
	}
}