package com.apps.interestingapps.multibackground;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.apps.interestingapps.multibackground.common.DatabaseHelper;
import com.apps.interestingapps.multibackground.common.MultiBackgroundImage;
import com.apps.interestingapps.multibackground.common.MultiBackgroundUtilities;

public class MyWallpaperService extends WallpaperService {

	public Engine onCreateEngine() {
		return new MyWallpaperEngine();
	}

	private class MyWallpaperEngine extends Engine {
		private final Handler handler = new Handler();
		private float downX, upX;
		private final double THRESHOLD_FOR_SCREEN_CHANGE = 0.41;
		private final double SCREEN_COVERAGE_FACTOR = 0.45;
		private int screenX, screenY;
		private boolean changeBackground = false;
		private double screenCoverageRequired;
		private DatabaseHelper databaseHelper;
		private List<MultiBackgroundImage> imageList;
		private int currentImageNumber = 0;
		private static final String TAG = "MyWallpaperService";
		private float actualDistanceX;

		private final Runnable drawRunner = new Runnable() {

			public void run() {
				draw();
			}

		};
		private boolean visible = true;

		@SuppressWarnings("deprecation")
		public MyWallpaperEngine() {
			downX = 0;
			upX = 0;
			WindowManager wm = (WindowManager) getApplicationContext()
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();

			screenX = display.getWidth();
			screenY = display.getHeight();
			screenCoverageRequired = screenX * SCREEN_COVERAGE_FACTOR;
			Log.i(TAG, "****Constructor called");
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			Log.i(TAG, "On Create called");
			setTouchEventsEnabled(true);
			databaseHelper = DatabaseHelper
					.initializeDatabase(getApplicationContext());
			if (databaseHelper != null) {
				imageList = databaseHelper.getAllImages();
			}
			changeBackground = true;
			handler.post(drawRunner);
		}

		public void onVisibilityChanged(boolean visible) {
			this.visible = visible;
			Log.i(TAG, "Changed the visibility to: " + visible);
			if (visible) {
				handler.post(drawRunner);
			} else {
				handler.removeCallbacks(drawRunner);
			}
		}

		public void onDestroy() {
			Log.i(TAG, "Destroy app called");
			if (databaseHelper != null) {
				databaseHelper.closeDatabase();
				handler.removeCallbacks(drawRunner);
			}
		}

		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);

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
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				upX = event.getX();
				long upTime = event.getEventTime();
				long downTime = event.getDownTime();
				long timeDiff = upTime - downTime;
				actualDistanceX = upX - downX;
				float absoluteDistanceX = Math.abs(actualDistanceX);
				if (timeDiff != 0 && absoluteDistanceX != 0) {
					double threshold = absoluteDistanceX / timeDiff;
					Log.i(TAG, "Threshold = " + threshold);
					Log.i(TAG, "Distance = " + actualDistanceX + " Time = "
							+ timeDiff + " ScreenX = " + screenX);
					if (absoluteDistanceX >= screenCoverageRequired
							|| threshold >= THRESHOLD_FOR_SCREEN_CHANGE) {
						/*
						 * TODO: take into consideration :
						 *
						 * 1. long press can be done before/after down/up events
						 * --> Checked if distance is greater than half the
						 * screen size, it will change the home screen
						 *
						 * 2. Current screen can be the last screen in the
						 * direction of movement --> Currently couldn't find a
						 * better way, so changing the feature of the app.
						 *
						 * 3. Multitouch events
						 */
						changeBackground = true;
						draw();
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
			if (changeBackground) {
				try {
					canvas = holder.lockCanvas();
					if (canvas != null) {
						changeBackground(canvas);
					}
				} finally {
					if (canvas != null)
						holder.unlockCanvasAndPost(canvas);
				}
			}
			handler.removeCallbacks(drawRunner);
			if (visible) {
				handler.postDelayed(drawRunner, 50000);
			}
		}

		// Surface view requires that all elements are drawn completely
		private void changeBackground(Canvas canvas) {
			if (changeBackground) {
				Bitmap scaledBitmap;
				if (imageList != null && currentImageNumber != -1) {
					String imagePath = imageList.get(currentImageNumber)
							.getPath();
					getNextImageNumber();

					scaledBitmap = MultiBackgroundUtilities
							.scaleDownImageAndDecode(imagePath, screenX,
									screenY);
					if (scaledBitmap == null) {
						scaledBitmap = MultiBackgroundUtilities
								.scaleDownImageAndDecode(getResources(),
										R.drawable.default_background, screenX,
										screenY);
					}
				} else {
					/*
					 * Show some default background if there is some problem in
					 * opening database
					 */
					scaledBitmap = MultiBackgroundUtilities
							.scaleDownImageAndDecode(getResources(),
									R.drawable.default_background, screenX,
									screenY);

				}
				changeBackground = false;
				canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
				if(scaledBitmap != null) {
					canvas.drawBitmap(scaledBitmap, 0, 0, null);
				}
			}
		}

		private void getNextImageNumber() {
			if (databaseHelper.isDatabaseUpdated()) {
				imageList = databaseHelper.getAllImages();
				databaseHelper.setDatabaseUpdated(false);
			}
			if (imageList.size() == 0) {
				currentImageNumber = -1;
			} else {
				int delta = 1;
				if (actualDistanceX < 0) {
					delta = -1;
				}
				int newImageNumber = currentImageNumber + delta;
				if (newImageNumber < 0) {
					newImageNumber = imageList.size() - 1;
				}
				currentImageNumber = newImageNumber % imageList.size();
			}
		}
	}
}